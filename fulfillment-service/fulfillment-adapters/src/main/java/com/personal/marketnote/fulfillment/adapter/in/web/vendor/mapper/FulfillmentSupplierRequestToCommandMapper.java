package com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper;

import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentSupplierRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFulfillmentSupplierRequest;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSuppliersCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentSupplierCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentSupplierCommand;

public class FulfillmentSupplierRequestToCommandMapper {
    public static RegisterFulfillmentSupplierCommand mapToRegisterCommand(
            String customerCode,
            String accessToken,
            RegisterFulfillmentSupplierRequest request
    ) {
        return RegisterFulfillmentSupplierCommand.of(
                customerCode,
                accessToken,
                request.getSupplierName(),
                request.getCustomerSupplierCode(),
                request.getUseYn(),
                request.getDealStartDate(),
                request.getDealEndDate(),
                request.getZipCode(),
                request.getAddress1(),
                request.getAddress2(),
                request.getCeoName(),
                request.getBusinessNumber(),
                request.getBusinessCategory(),
                request.getBusinessType(),
                request.getPhoneNumber(),
                request.getFaxNumber(),
                request.getPrimaryManagerName(),
                request.getPrimaryManagerPosition(),
                request.getPrimaryManagerPhoneNumber(),
                request.getPrimaryManagerEmail(),
                request.getSecondaryManagerName(),
                request.getSecondaryManagerPosition(),
                request.getSecondaryManagerPhoneNumber(),
                request.getSecondaryManagerEmail()
        );
    }

    public static GetFulfillmentSuppliersCommand mapToSuppliersCommand(
            String customerCode,
            String accessToken
    ) {
        return GetFulfillmentSuppliersCommand.of(customerCode, accessToken);
    }

    public static UpdateFulfillmentSupplierCommand mapToUpdateCommand(
            String customerCode,
            String accessToken,
            UpdateFulfillmentSupplierRequest request
    ) {
        return UpdateFulfillmentSupplierCommand.of(
                customerCode,
                accessToken,
                request.getSupplierCode(),
                request.getSupplierName(),
                request.getCustomerSupplierCode(),
                request.getUseYn(),
                request.getDealStartDate(),
                request.getDealEndDate(),
                request.getZipCode(),
                request.getAddress1(),
                request.getAddress2(),
                request.getCeoName(),
                request.getBusinessNumber(),
                request.getBusinessCategory(),
                request.getBusinessType(),
                request.getPhoneNumber(),
                request.getFaxNumber(),
                request.getPrimaryManagerName(),
                request.getPrimaryManagerPosition(),
                request.getPrimaryManagerPhoneNumber(),
                request.getPrimaryManagerEmail(),
                request.getSecondaryManagerName(),
                request.getSecondaryManagerPosition(),
                request.getSecondaryManagerPhoneNumber(),
                request.getSecondaryManagerEmail()
        );
    }
}
