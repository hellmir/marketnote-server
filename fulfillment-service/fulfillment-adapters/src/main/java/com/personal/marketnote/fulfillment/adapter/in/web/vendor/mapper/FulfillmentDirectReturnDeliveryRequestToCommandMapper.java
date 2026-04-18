package com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentDeliveryGoodsRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentDirectReturnDeliveryRequest;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDirectReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDirectReturnDeliveryItemCommand;

import java.util.List;

public class FulfillmentDirectReturnDeliveryRequestToCommandMapper {

    public static RegisterFulfillmentDirectReturnDeliveryCommand mapToRegisterCommand(
            String customerCode,
            String accessToken,
            List<RegisterFulfillmentDirectReturnDeliveryRequest> request
    ) {
        List<RegisterFulfillmentDirectReturnDeliveryItemCommand> directReturnDeliveryRequests = request.stream()
                .map(FulfillmentDirectReturnDeliveryRequestToCommandMapper::mapItem)
                .toList();

        return RegisterFulfillmentDirectReturnDeliveryCommand.of(customerCode, accessToken, directReturnDeliveryRequests);
    }

    private static RegisterFulfillmentDirectReturnDeliveryItemCommand mapItem(RegisterFulfillmentDirectReturnDeliveryRequest item) {
        List<RegisterFulfillmentDeliveryGoodsCommand> goods = FormatValidator.hasValue(item.getGodCds())
                ? item.getGodCds().stream()
                .map(FulfillmentDirectReturnDeliveryRequestToCommandMapper::mapGoods)
                .toList()
                : List.of();

        return RegisterFulfillmentDirectReturnDeliveryItemCommand.builder()
                .orderDate(item.getOrdDt())
                .supplierCode(item.getSupCd())
                .originalCourierCode(item.getOrgParcelCd())
                .originalInvoiceNumber(item.getOrgInvoiceNo())
                .returnReceiveMethod(item.getInWay())
                .recipientName(item.getCustNm())
                .returnCourierCompany(item.getRtnParcelComp())
                .returnInvoiceNumber(item.getRtnInvoiceNo())
                .returnType(item.getRtnGubun())
                .returnReason(item.getRtnReason())
                .returnDetailReason(item.getRtnDetailReason())
                .remark(item.getRemark())
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
