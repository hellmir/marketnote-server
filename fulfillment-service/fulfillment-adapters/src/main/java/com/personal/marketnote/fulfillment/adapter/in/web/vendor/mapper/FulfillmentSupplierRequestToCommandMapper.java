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
                request.getSupNm(),
                request.getCstSupCd(),
                request.getUseYn(),
                request.getDealStrDt(),
                request.getDealEndDt(),
                request.getZipNo(),
                request.getAddr1(),
                request.getAddr2(),
                request.getCeoNm(),
                request.getBusNo(),
                request.getBusSp(),
                request.getBusTp(),
                request.getTelNo(),
                request.getFaxNo(),
                request.getEmpNm1(),
                request.getEmpPosit1(),
                request.getEmpTelNo1(),
                request.getEmpEmail1(),
                request.getEmpNm2(),
                request.getEmpPosit2(),
                request.getEmpTelNo2(),
                request.getEmpEmail2()
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
                request.getSupCd(),
                request.getSupNm(),
                request.getCstSupCd(),
                request.getUseYn(),
                request.getDealStrDt(),
                request.getDealEndDt(),
                request.getZipNo(),
                request.getAddr1(),
                request.getAddr2(),
                request.getCeoNm(),
                request.getBusNo(),
                request.getBusSp(),
                request.getBusTp(),
                request.getTelNo(),
                request.getFaxNo(),
                request.getEmpNm1(),
                request.getEmpPosit1(),
                request.getEmpTelNo1(),
                request.getEmpEmail1(),
                request.getEmpNm2(),
                request.getEmpPosit2(),
                request.getEmpTelNo2(),
                request.getEmpEmail2()
        );
    }
}
