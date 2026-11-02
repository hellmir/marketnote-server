package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentSupplierCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentSupplierResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentSupplierUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentSupplierPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class RegisterFulfillmentSupplierService implements RegisterFulfillmentSupplierUseCase {
    private final RegisterFulfillmentSupplierPort registerFulfillmentSupplierPort;

    @Override
    public RegisterFulfillmentSupplierResult registerSupplier(RegisterFulfillmentSupplierCommand command) {
        return registerFulfillmentSupplierPort.registerSupplier(command);
    }
}
