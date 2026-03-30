package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FulfillmentSupplierCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentSupplierCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentSupplierResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentSupplierUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.UpdateFulfillmentSupplierPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class UpdateFulfillmentSupplierService implements UpdateFulfillmentSupplierUseCase {
    private final UpdateFulfillmentSupplierPort updateFulfillmentSupplierPort;

    @Override
    public UpdateFulfillmentSupplierResult updateSupplier(UpdateFulfillmentSupplierCommand command) {
        return updateFulfillmentSupplierPort.updateSupplier(
                FulfillmentSupplierCommandToRequestMapper.mapToUpdateRequest(command)
        );
    }
}
