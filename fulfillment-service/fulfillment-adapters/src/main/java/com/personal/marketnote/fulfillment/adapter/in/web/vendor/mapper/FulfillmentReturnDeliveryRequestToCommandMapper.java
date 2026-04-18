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
                .orderDate(item.getOrdDt())
                .orderNumber(item.getOrdNo())
                .courierCode(item.getParcelCd())
                .invoiceNumber(item.getInvoiceNo())
                .recipientName(item.getCustNm())
                .recipientPhoneNumber(item.getCustTelNo())
                .recipientAddress(item.getCustAddr())
                .returnReceiverName(item.getRtnEmpNm())
                .returnReceiverPhoneNumber(item.getRtnTelNo())
                .returnZipCode(item.getRtnZipCd())
                .returnAddress1(item.getRtnAddr1())
                .returnAddress2(item.getRtnAddr2())
                .returnType(item.getRtnGubun())
                .returnReason(item.getRtnReason())
                .returnDetailReason(item.getGetRtnDetailReason())
                .returnShippingRequest(item.getRtnShipReqTerm())
                .products(goods)
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
