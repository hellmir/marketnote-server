package com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFasstoDeliveryGoodsRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFasstoDirectReturnDeliveryRequest;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDirectReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDirectReturnDeliveryItemCommand;

import java.util.List;

public class FasstoDirectReturnDeliveryRequestToCommandMapper {

    public static RegisterFasstoDirectReturnDeliveryCommand mapToRegisterCommand(
            String customerCode,
            String accessToken,
            List<RegisterFasstoDirectReturnDeliveryRequest> request
    ) {
        List<RegisterFasstoDirectReturnDeliveryItemCommand> directReturnDeliveryRequests = request.stream()
                .map(FasstoDirectReturnDeliveryRequestToCommandMapper::mapItem)
                .toList();

        return RegisterFasstoDirectReturnDeliveryCommand.of(customerCode, accessToken, directReturnDeliveryRequests);
    }

    private static RegisterFasstoDirectReturnDeliveryItemCommand mapItem(RegisterFasstoDirectReturnDeliveryRequest item) {
        List<RegisterFasstoDeliveryGoodsCommand> goods = FormatValidator.hasValue(item.getGodCds())
                ? item.getGodCds().stream()
                .map(FasstoDirectReturnDeliveryRequestToCommandMapper::mapGoods)
                .toList()
                : List.of();

        return RegisterFasstoDirectReturnDeliveryItemCommand.builder()
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

    private static RegisterFasstoDeliveryGoodsCommand mapGoods(RegisterFasstoDeliveryGoodsRequest item) {
        return RegisterFasstoDeliveryGoodsCommand.of(
                item.getCstGodCd(),
                item.getDistTermDt(),
                item.getOrdQty()
        );
    }
}
