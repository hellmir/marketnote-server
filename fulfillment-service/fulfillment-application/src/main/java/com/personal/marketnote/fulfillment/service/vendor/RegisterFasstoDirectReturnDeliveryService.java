package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FasstoDirectReturnDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDirectReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFasstoDirectReturnDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFasstoDirectReturnDeliveryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class RegisterFasstoDirectReturnDeliveryService implements RegisterFasstoDirectReturnDeliveryUseCase {
    private final RegisterFasstoDirectReturnDeliveryPort registerFasstoDirectReturnDeliveryPort;

    @Override
    public RegisterFasstoDeliveryResult registerDirectReturnDelivery(RegisterFasstoDirectReturnDeliveryCommand command) {
        return registerFasstoDirectReturnDeliveryPort.registerDirectReturnDelivery(
                FasstoDirectReturnDeliveryCommandToRequestMapper.mapToRegisterRequest(command)
        );
    }
}
