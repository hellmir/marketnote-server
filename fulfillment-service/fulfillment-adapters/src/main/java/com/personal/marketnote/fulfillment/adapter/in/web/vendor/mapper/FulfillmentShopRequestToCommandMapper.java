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
        return RegisterFulfillmentShopCommand.of(
                customerCode,
                accessToken,
                request.getShopName(),
                request.getCustomerShopCode(),
                request.getDealStartDate(),
                request.getDealEndDate(),
                request.getZipCode(),
                request.getAddress1(),
                request.getAddress2(),
                request.getCeoName(),
                request.getBusinessNumber(),
                request.getPhoneNumber(),
                request.getUnloadMethod(),
                request.getInspectionMethod(),
                request.getStandbyYn(),
                request.getFormType(),
                request.getManagerName(),
                request.getManagerPosition(),
                request.getManagerPhoneNumber(),
                request.getUseYn()
        );
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
        return UpdateFulfillmentShopCommand.of(
                customerCode,
                accessToken,
                request.getShopCode(),
                request.getShopName(),
                request.getCustomerShopCode(),
                request.getDealStartDate(),
                request.getDealEndDate(),
                request.getZipCode(),
                request.getAddress1(),
                request.getAddress2(),
                request.getCeoName(),
                request.getBusinessNumber(),
                request.getPhoneNumber(),
                request.getUnloadMethod(),
                request.getInspectionMethod(),
                request.getStandbyYn(),
                request.getFormType(),
                request.getManagerName(),
                request.getManagerPosition(),
                request.getManagerPhoneNumber(),
                request.getUseYn()
        );
    }
}
