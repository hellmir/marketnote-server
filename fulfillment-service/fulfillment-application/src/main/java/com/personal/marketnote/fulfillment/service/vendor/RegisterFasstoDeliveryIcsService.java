package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FasstoDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDeliveryIcsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFasstoDeliveryIcsUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFasstoDeliveryIcsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterFasstoDeliveryIcsService implements RegisterFasstoDeliveryIcsUseCase {
    private final RegisterFasstoDeliveryIcsPort registerFasstoDeliveryIcsPort;

    @Override
    public RegisterFasstoDeliveryResult registerDeliveryIcs(RegisterFasstoDeliveryIcsCommand command) {
        return registerFasstoDeliveryIcsPort.registerDeliveryIcs(
                FasstoDeliveryCommandToRequestMapper.mapToRegisterIcsRequest(command)
        );
    }
}
