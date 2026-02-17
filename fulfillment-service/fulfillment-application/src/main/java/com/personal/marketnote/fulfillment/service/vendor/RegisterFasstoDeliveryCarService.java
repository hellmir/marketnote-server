package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FasstoDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDeliveryCarCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFasstoDeliveryCarUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFasstoDeliveryCarPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterFasstoDeliveryCarService implements RegisterFasstoDeliveryCarUseCase {
    private final RegisterFasstoDeliveryCarPort registerFasstoDeliveryCarPort;

    @Override
    public RegisterFasstoDeliveryResult registerDeliveryCar(RegisterFasstoDeliveryCarCommand command) {
        return registerFasstoDeliveryCarPort.registerDeliveryCar(
                FasstoDeliveryCommandToRequestMapper.mapToRegisterCarRequest(command)
        );
    }
}
