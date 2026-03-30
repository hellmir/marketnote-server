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
                .ordDt(item.getOrdDt())
                .supCd(item.getSupCd())
                .orgParcelCd(item.getOrgParcelCd())
                .orgInvoiceNo(item.getOrgInvoiceNo())
                .inWay(item.getInWay())
                .custNm(item.getCustNm())
                .rtnParcelComp(item.getRtnParcelComp())
                .rtnInvoiceNo(item.getRtnInvoiceNo())
                .rtnGubun(item.getRtnGubun())
                .rtnReason(item.getRtnReason())
                .rtnDetailReason(item.getRtnDetailReason())
                .remark(item.getRemark())
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
