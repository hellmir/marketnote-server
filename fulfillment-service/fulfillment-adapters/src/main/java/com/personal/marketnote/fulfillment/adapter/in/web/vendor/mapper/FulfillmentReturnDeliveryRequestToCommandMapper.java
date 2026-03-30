package com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentDeliveryGoodsRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentReturnDeliveryRequest;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryItemCommand;

import java.util.List;

public class FulfillmentReturnDeliveryRequestToCommandMapper {

    public static RegisterFulfillmentReturnDeliveryCommand mapToRegisterCommand(
            String customerCode,
            String accessToken,
            List<RegisterFulfillmentReturnDeliveryRequest> request
    ) {
        List<RegisterFulfillmentReturnDeliveryItemCommand> returnDeliveryRequests = request.stream()
                .map(FulfillmentReturnDeliveryRequestToCommandMapper::mapItem)
                .toList();

        return RegisterFulfillmentReturnDeliveryCommand.of(customerCode, accessToken, returnDeliveryRequests);
    }

    public static GetFulfillmentReturnGodDetailCommand mapToReturnGodDetailCommand(
            String customerCode,
            String accessToken,
            String strDt,
            String endDt,
            String rtnSlipNoList,
            String whCd
    ) {
        return GetFulfillmentReturnGodDetailCommand.of(
                customerCode, accessToken, strDt, endDt, rtnSlipNoList, whCd
        );
    }

    private static RegisterFulfillmentReturnDeliveryItemCommand mapItem(RegisterFulfillmentReturnDeliveryRequest item) {
        List<RegisterFulfillmentDeliveryGoodsCommand> goods = FormatValidator.hasValue(item.getGodCds())
                ? item.getGodCds().stream()
                .map(FulfillmentReturnDeliveryRequestToCommandMapper::mapGoods)
                .toList()
                : List.of();

        return RegisterFulfillmentReturnDeliveryItemCommand.builder()
                .ordDt(item.getOrdDt())
                .ordNo(item.getOrdNo())
                .parcelCd(item.getParcelCd())
                .invoiceNo(item.getInvoiceNo())
                .custNm(item.getCustNm())
                .custTelNo(item.getCustTelNo())
                .custAddr(item.getCustAddr())
                .rtnEmpNm(item.getRtnEmpNm())
                .rtnTelNo(item.getRtnTelNo())
                .rtnZipCd(item.getRtnZipCd())
                .rtnAddr1(item.getRtnAddr1())
                .rtnAddr2(item.getRtnAddr2())
                .rtnGubun(item.getRtnGubun())
                .rtnReason(item.getRtnReason())
                .getRtnDetailReason(item.getGetRtnDetailReason())
                .rtnShipReqTerm(item.getRtnShipReqTerm())
                .godCds(goods)
                .build();
    }

    private static RegisterFulfillmentDeliveryGoodsCommand mapGoods(RegisterFulfillmentDeliveryGoodsRequest item) {
        return RegisterFulfillmentDeliveryGoodsCommand.of(
                item.getCstGodCd(),
                item.getDistTermDt(),
                item.getOrdQty()
        );
    }
}
