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
        return RegisterFulfillmentSupplierCommand.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .supplierName(request.getSupplierName())
                .customerSupplierCode(request.getCustomerSupplierCode())
                .useYn(request.getUseYn())
                .dealStartDate(request.getDealStartDate())
                .dealEndDate(request.getDealEndDate())
                .zipCode(request.getZipCode())
                .address1(request.getAddress1())
                .address2(request.getAddress2())
                .ceoName(request.getCeoName())
                .businessNumber(request.getBusinessNumber())
                .businessCategory(request.getBusinessCategory())
                .businessType(request.getBusinessType())
                .phoneNumber(request.getPhoneNumber())
                .faxNumber(request.getFaxNumber())
                .primaryManagerName(request.getPrimaryManagerName())
                .primaryManagerPosition(request.getPrimaryManagerPosition())
                .primaryManagerPhoneNumber(request.getPrimaryManagerPhoneNumber())
                .primaryManagerEmail(request.getPrimaryManagerEmail())
                .secondaryManagerName(request.getSecondaryManagerName())
                .secondaryManagerPosition(request.getSecondaryManagerPosition())
                .secondaryManagerPhoneNumber(request.getSecondaryManagerPhoneNumber())
                .secondaryManagerEmail(request.getSecondaryManagerEmail())
                .build();
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
        return UpdateFulfillmentSupplierCommand.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .supplierCode(request.getSupplierCode())
                .supplierName(request.getSupplierName())
                .customerSupplierCode(request.getCustomerSupplierCode())
                .useYn(request.getUseYn())
                .dealStartDate(request.getDealStartDate())
                .dealEndDate(request.getDealEndDate())
                .zipCode(request.getZipCode())
                .address1(request.getAddress1())
                .address2(request.getAddress2())
                .ceoName(request.getCeoName())
                .businessNumber(request.getBusinessNumber())
                .businessCategory(request.getBusinessCategory())
                .businessType(request.getBusinessType())
                .phoneNumber(request.getPhoneNumber())
                .faxNumber(request.getFaxNumber())
                .primaryManagerName(request.getPrimaryManagerName())
                .primaryManagerPosition(request.getPrimaryManagerPosition())
                .primaryManagerPhoneNumber(request.getPrimaryManagerPhoneNumber())
                .primaryManagerEmail(request.getPrimaryManagerEmail())
                .secondaryManagerName(request.getSecondaryManagerName())
                .secondaryManagerPosition(request.getSecondaryManagerPosition())
                .secondaryManagerPhoneNumber(request.getSecondaryManagerPhoneNumber())
                .secondaryManagerEmail(request.getSecondaryManagerEmail())
                .build();
    }
}
