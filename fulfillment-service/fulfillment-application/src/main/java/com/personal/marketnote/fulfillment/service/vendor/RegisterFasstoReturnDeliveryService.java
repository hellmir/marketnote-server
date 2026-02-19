package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FasstoReturnDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFasstoReturnDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFasstoReturnDeliveryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class RegisterFasstoReturnDeliveryService implements RegisterFasstoReturnDeliveryUseCase {
    private final RegisterFasstoReturnDeliveryPort registerFasstoReturnDeliveryPort;

    @Override
    public RegisterFasstoDeliveryResult registerReturnDelivery(RegisterFasstoReturnDeliveryCommand command) {
        return registerFasstoReturnDeliveryPort.registerReturnDelivery(
                FasstoReturnDeliveryCommandToRequestMapper.mapToRegisterRequest(command)
        );
    }
}
