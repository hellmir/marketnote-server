package com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper;

import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentShopRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFulfillmentShopRequest;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentShopsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentShopCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentShopCommand;

public class FulfillmentShopRequestToCommandMapper {
    public static RegisterFulfillmentShopCommand mapToRegisterCommand(
            String customerCode,
            String accessToken,
            RegisterFulfillmentShopRequest request
    ) {
        return RegisterFulfillmentShopCommand.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .shopName(request.getShopName())
                .customerShopCode(request.getCustomerShopCode())
                .dealStartDate(request.getDealStartDate())
                .dealEndDate(request.getDealEndDate())
                .zipCode(request.getZipCode())
                .address1(request.getAddress1())
                .address2(request.getAddress2())
                .ceoName(request.getCeoName())
                .businessNumber(request.getBusinessNumber())
                .phoneNumber(request.getPhoneNumber())
                .unloadMethod(request.getUnloadMethod())
                .inspectionMethod(request.getInspectionMethod())
                .standbyYn(request.getStandbyYn())
                .formType(request.getFormType())
                .managerName(request.getManagerName())
                .managerPosition(request.getManagerPosition())
                .managerPhoneNumber(request.getManagerPhoneNumber())
                .useYn(request.getUseYn())
                .build();
    }

    public static GetFulfillmentShopsCommand mapToShopsCommand(
            String customerCode,
            String accessToken
    ) {
        return GetFulfillmentShopsCommand.of(customerCode, accessToken);
    }

    public static UpdateFulfillmentShopCommand mapToUpdateCommand(
            String customerCode,
            String accessToken,
            UpdateFulfillmentShopRequest request
    ) {
        return UpdateFulfillmentShopCommand.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .shopCode(request.getShopCode())
                .shopName(request.getShopName())
                .customerShopCode(request.getCustomerShopCode())
                .dealStartDate(request.getDealStartDate())
                .dealEndDate(request.getDealEndDate())
                .zipCode(request.getZipCode())
                .address1(request.getAddress1())
                .address2(request.getAddress2())
                .ceoName(request.getCeoName())
                .businessNumber(request.getBusinessNumber())
                .phoneNumber(request.getPhoneNumber())
                .unloadMethod(request.getUnloadMethod())
                .inspectionMethod(request.getInspectionMethod())
                .standbyYn(request.getStandbyYn())
                .formType(request.getFormType())
                .managerName(request.getManagerName())
                .managerPosition(request.getManagerPosition())
                .managerPhoneNumber(request.getManagerPhoneNumber())
                .useYn(request.getUseYn())
                .build();
    }
}
