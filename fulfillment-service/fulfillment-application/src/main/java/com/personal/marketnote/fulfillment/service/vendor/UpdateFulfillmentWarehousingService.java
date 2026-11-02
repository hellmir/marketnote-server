package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentWarehousingResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentWarehousingUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.UpdateFulfillmentWarehousingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class UpdateFulfillmentWarehousingService implements UpdateFulfillmentWarehousingUseCase {
    private final UpdateFulfillmentWarehousingPort updateFulfillmentWarehousingPort;

    @Override
    public UpdateFulfillmentWarehousingResult updateWarehousing(UpdateFulfillmentWarehousingCommand command) {
        return updateFulfillmentWarehousingPort.updateWarehousing(command);
    }
}
