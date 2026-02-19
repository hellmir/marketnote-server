package com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFasstoDeliveryGoodsRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFasstoReturnDeliveryRequest;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoReturnDeliveryItemCommand;

import java.util.List;

public class FasstoReturnDeliveryRequestToCommandMapper {

    public static RegisterFasstoReturnDeliveryCommand mapToRegisterCommand(
            String customerCode,
            String accessToken,
            List<RegisterFasstoReturnDeliveryRequest> request
    ) {
        List<RegisterFasstoReturnDeliveryItemCommand> returnDeliveryRequests = request.stream()
                .map(FasstoReturnDeliveryRequestToCommandMapper::mapItem)
                .toList();

        return RegisterFasstoReturnDeliveryCommand.of(customerCode, accessToken, returnDeliveryRequests);
    }

    private static RegisterFasstoReturnDeliveryItemCommand mapItem(RegisterFasstoReturnDeliveryRequest item) {
        List<RegisterFasstoDeliveryGoodsCommand> goods = FormatValidator.hasValue(item.getGodCds())
                ? item.getGodCds().stream()
                .map(FasstoReturnDeliveryRequestToCommandMapper::mapGoods)
                .toList()
                : List.of();

        return RegisterFasstoReturnDeliveryItemCommand.builder()
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

    private static RegisterFasstoDeliveryGoodsCommand mapGoods(RegisterFasstoDeliveryGoodsRequest item) {
        return RegisterFasstoDeliveryGoodsCommand.of(
                item.getCstGodCd(),
                item.getDistTermDt(),
                item.getOrdQty()
        );
    }
}
