package com.personal.marketnote.user.service.shippingaddress;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;
import com.personal.marketnote.common.kafka.event.ShippingAddressChangeAction;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressCreateState;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import com.personal.marketnote.user.exception.TooManyOtherAddressesException;
import com.personal.marketnote.user.mapper.ShippingAddressCommandToStateMapper;
import com.personal.marketnote.user.port.in.command.shippingaddress.RegisterShippingAddressCommand;
import com.personal.marketnote.user.port.in.result.shippingaddress.RegisterShippingAddressResult;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.RegisterShippingAddressUseCase;
import com.personal.marketnote.user.port.out.event.PublishShippingAddressEventPort;
import com.personal.marketnote.user.port.out.shippingaddress.ClassifyShippingAddressRegionPort;
import com.personal.marketnote.user.port.out.shippingaddress.FindShippingAddressPort;
import com.personal.marketnote.user.port.out.shippingaddress.SaveShippingAddressPort;
import com.personal.marketnote.user.port.out.shippingaddress.UpdateShippingAddressPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static com.personal.marketnote.common.domain.exception.ExceptionCode.SECOND_ERROR_CODE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED)
public class RegisterShippingAddressService implements RegisterShippingAddressUseCase {
    private static final long MAX_OTHER_ADDRESS_COUNT = 10L;

    private final FindShippingAddressPort findShippingAddressPort;
    private final SaveShippingAddressPort saveShippingAddressPort;
    private final UpdateShippingAddressPort updateShippingAddressPort;
    private final PublishShippingAddressEventPort publishShippingAddressEventPort;
    private final ClassifyShippingAddressRegionPort classifyShippingAddressRegionPort;

    @Override
    public RegisterShippingAddressResult registerShippingAddress(RegisterShippingAddressCommand command) {
        ShippingAddressType addressType = command.addressType();
        Long userId = command.userId();

        validateAddressTypeLimit(userId, addressType);

        boolean isFirstAddress = !findShippingAddressPort.existsByUserId(userId);
        boolean isDefault = isFirstAddress || Boolean.TRUE.equals(command.isDefault());

        if (isDefault && !isFirstAddress) {
            findShippingAddressPort.findDefaultsByUserId(userId)
                    .forEach(currentDefault -> {
                        currentDefault.unsetAsDefault();
                        updateShippingAddressPort.update(currentDefault);
                    });
        }

        ShippingAddressRegionType regionType = classifyShippingAddressRegionPort.classify(command.address());
        ShippingAddressCreateState createState = ShippingAddressCommandToStateMapper.mapToCreateState(command, isDefault, regionType);
        ShippingAddress shippingAddress = ShippingAddress.from(createState);

        ShippingAddress savedShippingAddress = saveShippingAddressPort.save(shippingAddress);

        publishShippingAddressEventPort.publishShippingAddressChangedEvent(
                savedShippingAddress.getId(), savedShippingAddress.getUserId(),
                savedShippingAddress.getRecipientName(), savedShippingAddress.getRecipientPhoneNumber(),
                savedShippingAddress.getAddress(), savedShippingAddress.getAddressDetail(),
                regionType.name(),
                ShippingAddressChangeAction.CREATED
        );

        return RegisterShippingAddressResult.from(savedShippingAddress);
    }

    private void validateAddressTypeLimit(Long userId, ShippingAddressType addressType) {
        switch (addressType) {
            case HOME -> {
                if (findShippingAddressPort.existsByUserIdAndAddressType(userId, ShippingAddressType.HOME)) {
                    throw new DomainAlreadyExistsException(
                            String.format("%s:: 이미 등록된 집 배송지가 존재합니다.", FIRST_ERROR_CODE)
                    );
                }
            }
            case COMPANY -> {
                if (findShippingAddressPort.existsByUserIdAndAddressType(userId, ShippingAddressType.COMPANY)) {
                    throw new DomainAlreadyExistsException(
                            String.format("%s:: 이미 등록된 회사 배송지가 존재합니다.", FIRST_ERROR_CODE)
                    );
                }
            }
            case OTHER -> {
                long count = findShippingAddressPort.countByUserIdAndAddressType(userId, ShippingAddressType.OTHER);
                if (count >= MAX_OTHER_ADDRESS_COUNT) {
                    throw new TooManyOtherAddressesException(SECOND_ERROR_CODE, MAX_OTHER_ADDRESS_COUNT);
                }
            }
        }
    }
}
