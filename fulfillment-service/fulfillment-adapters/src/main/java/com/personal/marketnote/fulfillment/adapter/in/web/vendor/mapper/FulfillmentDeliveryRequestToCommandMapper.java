package com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper;

import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.*;
import com.personal.marketnote.fulfillment.port.in.command.vendor.*;

import java.util.List;

public class FulfillmentDeliveryRequestToCommandMapper {
    public static RegisterFulfillmentDeliveryCommand mapToRegisterCommand(
            String customerCode,
            String accessToken,
            List<RegisterFulfillmentDeliveryRequest> request
    ) {
        List<RegisterFulfillmentDeliveryItemCommand> deliveryRequests = request.stream()
                .map(FulfillmentDeliveryRequestToCommandMapper::mapItem)
                .toList();

        return RegisterFulfillmentDeliveryCommand.of(customerCode, accessToken, deliveryRequests);
    }

    public static UpdateFulfillmentDeliveryCommand mapToUpdateCommand(
            String customerCode,
            String accessToken,
            List<UpdateFulfillmentDeliveryRequest> request
    ) {
        List<UpdateFulfillmentDeliveryItemCommand> deliveryRequests = request.stream()
                .map(FulfillmentDeliveryRequestToCommandMapper::mapUpdateItem)
                .toList();

        return UpdateFulfillmentDeliveryCommand.of(customerCode, accessToken, deliveryRequests);
    }

    public static RegisterFulfillmentDeliveryCarCommand mapToRegisterCarCommand(
            String customerCode,
            String accessToken,
            List<RegisterFulfillmentDeliveryCarRequest> request
    ) {
        List<RegisterFulfillmentDeliveryCarItemCommand> deliveryRequests = request.stream()
                .map(FulfillmentDeliveryRequestToCommandMapper::mapCarItem)
                .toList();

        return RegisterFulfillmentDeliveryCarCommand.of(customerCode, accessToken, deliveryRequests);
    }

    public static RegisterFulfillmentDeliveryIcsCommand mapToRegisterIcsCommand(
            String customerCode,
            String accessToken,
            List<RegisterFulfillmentDeliveryIcsRequest> request
    ) {
        List<RegisterFulfillmentDeliveryIcsItemCommand> deliveryRequests = request.stream()
                .map(FulfillmentDeliveryRequestToCommandMapper::mapIcsItem)
                .toList();

        return RegisterFulfillmentDeliveryIcsCommand.of(customerCode, accessToken, deliveryRequests);
    }

    public static UpdateFulfillmentDeliveryCarCommand mapToUpdateCarCommand(
            String customerCode,
            String accessToken,
            List<UpdateFulfillmentDeliveryCarRequest> request
    ) {
        List<UpdateFulfillmentDeliveryCarItemCommand> deliveryRequests = request.stream()
                .map(FulfillmentDeliveryRequestToCommandMapper::mapUpdateCarItem)
                .toList();

        return UpdateFulfillmentDeliveryCarCommand.of(customerCode, accessToken, deliveryRequests);
    }

    public static GetFulfillmentDeliveriesCommand mapToDeliveriesCommand(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String status,
            String outDiv,
            String ordNo
    ) {
        return GetFulfillmentDeliveriesCommand.of(
                customerCode,
                accessToken,
                startDate,
                endDate,
                status,
                outDiv,
                ordNo
        );
    }

    public static GetFulfillmentDeliveryDetailCommand mapToDeliveryDetailCommand(
            String customerCode,
            String accessToken,
            String slipNo,
            String ordNo
    ) {
        return GetFulfillmentDeliveryDetailCommand.of(customerCode, accessToken, slipNo, ordNo);
    }

    public static GetFulfillmentDeliveryStatusesCommand mapToDeliveryStatusesCommand(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String outDiv
    ) {
        return GetFulfillmentDeliveryStatusesCommand.of(customerCode, accessToken, startDate, endDate, outDiv);
    }

    public static CancelFulfillmentDeliveryCommand mapToCancelCommand(
            String customerCode,
            String accessToken,
            List<CancelFulfillmentDeliveryRequest> request
    ) {
        List<CancelFulfillmentDeliveryItemCommand> cancelRequests = request.stream()
                .map(item -> CancelFulfillmentDeliveryItemCommand.of(item.getSlipNo(), item.getOrdNo()))
                .toList();

        return CancelFulfillmentDeliveryCommand.of(customerCode, accessToken, cancelRequests);
    }

    public static GetFulfillmentDeliveryOutOrdGoodsDetailCommand mapToOutOrdGoodsDetailCommand(
            String customerCode,
            String accessToken,
            String outOrdSlipNo
    ) {
        return GetFulfillmentDeliveryOutOrdGoodsDetailCommand.of(customerCode, accessToken, outOrdSlipNo);
    }

    public static GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand mapToOutOrdGoodsByOrdNoCommand(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String ordNo
    ) {
        return GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand.of(
                customerCode,
                accessToken,
                startDate,
                endDate,
                ordNo
        );
    }

    public static GetFulfillmentDeliveryGoodDetailCommand mapToDeliveryGoodDetailCommand(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String ordNo
    ) {
        return GetFulfillmentDeliveryGoodDetailCommand.of(
                customerCode,
                accessToken,
                startDate,
                endDate,
                ordNo
        );
    }

    public static CompleteFulfillmentDeliveryIcsCommand mapToIcsCompletionCommand(
            String customerCode,
            String accessToken,
            CompleteFulfillmentDeliveryIcsRequest request
    ) {
        CompleteFulfillmentDeliveryIcsItemCommand itemCommand = CompleteFulfillmentDeliveryIcsItemCommand.of(request.getOrdNoList());
        return CompleteFulfillmentDeliveryIcsCommand.of(customerCode, accessToken, List.of(itemCommand));
    }

    private static RegisterFulfillmentDeliveryItemCommand mapItem(RegisterFulfillmentDeliveryRequest item) {
        List<RegisterFulfillmentDeliveryGoodsCommand> goods = item.getGodCds().stream()
                .map(FulfillmentDeliveryRequestToCommandMapper::mapGoods)
                .toList();

        return RegisterFulfillmentDeliveryItemCommand.builder()
                .ordDt(item.getOrdDt())
                .ordNo(item.getOrdNo())
                .ordSeq(item.getOrdSeq())
                .slipNo(item.getSlipNo())
                .custNm(item.getCustNm())
                .custTelNo(item.getCustTelNo())
                .custAddr(item.getCustAddr())
                .outWay(item.getOutWay())
                .sendNm(item.getSendNm())
                .sendTelNo(item.getSendTelNo())
                .salChanel(item.getSalChanel())
                .shipReqTerm(item.getShipReqTerm())
                .godCds(goods)
                .oneDayDeliveryCd(item.getOneDayDeliveryCd())
                .remark(item.getRemark())
                .build();
    }

    private static UpdateFulfillmentDeliveryItemCommand mapUpdateItem(UpdateFulfillmentDeliveryRequest item) {
        List<RegisterFulfillmentDeliveryGoodsCommand> goods = item.getGodCds().stream()
                .map(FulfillmentDeliveryRequestToCommandMapper::mapGoods)
                .toList();

        return UpdateFulfillmentDeliveryItemCommand.builder()
                .ordDt(item.getOrdDt())
                .ordNo(item.getOrdNo())
                .ordSeq(item.getOrdSeq())
                .slipNo(item.getSlipNo())
                .custNm(item.getCustNm())
                .custTelNo(item.getCustTelNo())
                .custAddr(item.getCustAddr())
                .outWay(item.getOutWay())
                .sendNm(item.getSendNm())
                .sendTelNo(item.getSendTelNo())
                .salChanel(item.getSalChanel())
                .shipReqTerm(item.getShipReqTerm())
                .godCds(goods)
                .oneDayDeliveryCd(item.getOneDayDeliveryCd())
                .remark(item.getRemark())
                .build();
    }

    private static RegisterFulfillmentDeliveryCarItemCommand mapCarItem(RegisterFulfillmentDeliveryCarRequest item) {
        List<RegisterFulfillmentDeliveryGoodsCommand> goods = item.getGodCds().stream()
                .map(FulfillmentDeliveryRequestToCommandMapper::mapGoods)
                .toList();

        return RegisterFulfillmentDeliveryCarItemCommand.builder()
                .ordDt(item.getOrdDt())
                .ordNo(item.getOrdNo())
                .slipNo(item.getSlipNo())
                .outWay(item.getOutWay())
                .cstShopCd(item.getCstShopCd())
                .godCds(goods)
                .remark(item.getRemark())
                .build();
    }

    private static UpdateFulfillmentDeliveryCarItemCommand mapUpdateCarItem(UpdateFulfillmentDeliveryCarRequest item) {
        List<RegisterFulfillmentDeliveryGoodsCommand> goods = item.getGodCds().stream()
                .map(FulfillmentDeliveryRequestToCommandMapper::mapGoods)
                .toList();

        return UpdateFulfillmentDeliveryCarItemCommand.builder()
                .ordDt(item.getOrdDt())
                .ordNo(item.getOrdNo())
                .slipNo(item.getSlipNo())
                .outWay(item.getOutWay())
                .cstShopCd(item.getCstShopCd())
                .godCds(goods)
                .remark(item.getRemark())
                .build();
    }

    private static RegisterFulfillmentDeliveryIcsItemCommand mapIcsItem(RegisterFulfillmentDeliveryIcsRequest item) {
        List<RegisterFulfillmentDeliveryGoodsCommand> goods = item.getGodCds().stream()
                .map(FulfillmentDeliveryRequestToCommandMapper::mapIcsGoods)
                .toList();

        return RegisterFulfillmentDeliveryIcsItemCommand.builder()
                .ordDt(item.getOrdDt())
                .ordNo(item.getOrdNo())
                .platform(item.getPlatform())
                .logiCenter(item.getLogiCenter())
                .invoiceNo(item.getInvoiceNo())
                .custNm(item.getCustNm())
                .custTelNo(item.getCustTelNo())
                .custAddr(item.getCustAddr())
                .sendNm(item.getSendNm())
                .sendTelNo(item.getSendTelNo())
                .salChanel(item.getSalChanel())
                .shipReqTerm(item.getShipReqTerm())
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

    private static RegisterFulfillmentDeliveryGoodsCommand mapIcsGoods(RegisterFulfillmentDeliveryIcsGoodsRequest item) {
        return RegisterFulfillmentDeliveryGoodsCommand.of(
                item.getCstGodCd(),
                null,
                item.getOrdQty()
        );
    }
}
