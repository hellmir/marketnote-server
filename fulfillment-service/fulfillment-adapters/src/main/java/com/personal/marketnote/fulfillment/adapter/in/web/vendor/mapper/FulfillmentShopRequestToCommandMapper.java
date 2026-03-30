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
                request.getShopNm(),
                request.getCstShopCd(),
                request.getDealStrDt(),
                request.getDealEndDt(),
                request.getZipNo(),
                request.getAddr1(),
                request.getAddr2(),
                request.getCeoNm(),
                request.getBusNo(),
                request.getTelNo(),
                request.getUnloadWay(),
                request.getCheckWay(),
                request.getStandYn(),
                request.getFormType(),
                request.getEmpNm(),
                request.getEmpPosit(),
                request.getEmpTelNo(),
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
                request.getShopCd(),
                request.getShopNm(),
                request.getCstShopCd(),
                request.getDealStrDt(),
                request.getDealEndDt(),
                request.getZipNo(),
                request.getAddr1(),
                request.getAddr2(),
                request.getCeoNm(),
                request.getBusNo(),
                request.getTelNo(),
                request.getUnloadWay(),
                request.getCheckWay(),
                request.getStandYn(),
                request.getFormType(),
                request.getEmpNm(),
                request.getEmpPosit(),
                request.getEmpTelNo(),
                request.getUseYn()
        );
    }
}
