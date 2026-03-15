package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.domain.delivery.FasstoDeliveryRegistration;
import com.personal.marketnote.fulfillment.domain.delivery.FasstoDeliveryRegistrationCreateState;
import com.personal.marketnote.fulfillment.mapper.FasstoDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFasstoDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.out.delivery.SaveFasstoDeliveryRegistrationPort;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFasstoDeliveryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class RegisterFasstoDeliveryService implements RegisterFasstoDeliveryUseCase {
    private final RegisterFasstoDeliveryPort registerFasstoDeliveryPort;
    private final SaveFasstoDeliveryRegistrationPort saveFasstoDeliveryRegistrationPort;

    @Override
    public RegisterFasstoDeliveryResult registerDelivery(RegisterFasstoDeliveryCommand command) {
        return registerFasstoDeliveryPort.registerDelivery(
                FasstoDeliveryCommandToRequestMapper.mapToRegisterRequest(command)
        );
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public RegisterFasstoDeliveryResult registerDeliveryIdempotent(RegisterFasstoDeliveryCommand command) {
        Long orderId = Long.parseLong(command.deliveryRequests().getFirst().ordNo());

        FasstoDeliveryRegistration registration = FasstoDeliveryRegistration.from(
                FasstoDeliveryRegistrationCreateState.builder()
                        .orderId(orderId)
                        .build()
        );
        saveFasstoDeliveryRegistrationPort.save(registration);

        return registerFasstoDeliveryPort.registerDelivery(
                FasstoDeliveryCommandToRequestMapper.mapToRegisterRequest(command)
        );
    }
}
