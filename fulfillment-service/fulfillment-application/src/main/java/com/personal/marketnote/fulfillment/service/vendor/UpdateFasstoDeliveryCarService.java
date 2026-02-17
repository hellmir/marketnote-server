package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FasstoDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFasstoDeliveryCarCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFasstoDeliveryCarUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.UpdateFasstoDeliveryCarPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class UpdateFasstoDeliveryCarService implements UpdateFasstoDeliveryCarUseCase {
    private final UpdateFasstoDeliveryCarPort updateFasstoDeliveryCarPort;

    @Override
    public RegisterFasstoDeliveryResult updateDeliveryCar(UpdateFasstoDeliveryCarCommand command) {
        return updateFasstoDeliveryCarPort.updateDeliveryCar(
                FasstoDeliveryCommandToRequestMapper.mapToUpdateCarRequest(command)
        );
    }
}
