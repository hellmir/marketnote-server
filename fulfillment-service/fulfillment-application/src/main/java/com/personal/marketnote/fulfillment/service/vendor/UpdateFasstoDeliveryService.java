package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FasstoDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFasstoDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFasstoDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.UpdateFasstoDeliveryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class UpdateFasstoDeliveryService implements UpdateFasstoDeliveryUseCase {
    private final UpdateFasstoDeliveryPort updateFasstoDeliveryPort;

    @Override
    public RegisterFasstoDeliveryResult updateDelivery(UpdateFasstoDeliveryCommand command) {
        return updateFasstoDeliveryPort.updateDelivery(
                FasstoDeliveryCommandToRequestMapper.mapToUpdateRequest(command)
        );
    }
}
