package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FulfillmentSupplierCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSuppliersCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSuppliersResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentSuppliersUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentSuppliersPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentSuppliersService implements GetFulfillmentSuppliersUseCase {
    private final GetFulfillmentSuppliersPort getFulfillmentSuppliersPort;

    @Override
    public GetFulfillmentSuppliersResult getSuppliers(GetFulfillmentSuppliersCommand command) {
        return getFulfillmentSuppliersPort.getSuppliers(
                FulfillmentSupplierCommandToRequestMapper.mapToSuppliersQuery(command)
        );
    }
}
