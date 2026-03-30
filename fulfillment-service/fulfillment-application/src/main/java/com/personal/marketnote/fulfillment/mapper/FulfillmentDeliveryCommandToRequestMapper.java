package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.fulfillment.domain.vendor.delivery.*;
import com.personal.marketnote.fulfillment.port.in.command.vendor.*;

import java.util.List;

public class FulfillmentDeliveryCommandToRequestMapper {
    public static FulfillmentDeliveryQuery mapToQuery(GetFulfillmentDeliveriesCommand command) {
        return FulfillmentDeliveryQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.startDate(),
                command.endDate(),
                command.status(),
                command.outDiv(),
                command.ordNo()
        );
    }

    public static FulfillmentDeliveryDetailQuery mapToDetailQuery(GetFulfillmentDeliveryDetailCommand command) {
        return FulfillmentDeliveryDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.slipNo(),
                command.ordNo()
        );
    }

    public static FulfillmentDeliveryStatusQuery mapToDeliveryStatusQuery(GetFulfillmentDeliveryStatusesCommand command) {
        return FulfillmentDeliveryStatusQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.startDate(),
                command.endDate(),
                command.outDiv()
        );
    }

    public static FulfillmentDeliveryOutOrdGoodsDetailQuery mapToOutOrdGoodsDetailQuery(
            GetFulfillmentDeliveryOutOrdGoodsDetailCommand command
    ) {
        return FulfillmentDeliveryOutOrdGoodsDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.outOrdSlipNo()
        );
    }

    public static FulfillmentDeliveryOutOrdGoodsByOrdNoQuery mapToOutOrdGoodsByOrdNoQuery(
            GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand command
    ) {
        return FulfillmentDeliveryOutOrdGoodsByOrdNoQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.startDate(),
                command.endDate(),
                command.ordNo()
        );
    }

    public static FulfillmentDeliveryGoodDetailQuery mapToDeliveryGoodDetailQuery(
            GetFulfillmentDeliveryGoodDetailCommand command
    ) {
        return FulfillmentDeliveryGoodDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.startDate(),
                command.endDate(),
                command.ordNo()
        );
    }

    public static FulfillmentDeliveryCancelMapper mapToCancelRequest(CancelFulfillmentDeliveryCommand command) {
        List<FulfillmentDeliveryCancelItemMapper> cancelRequests = command.deliveries().stream()
                .map(FulfillmentDeliveryCommandToRequestMapper::mapCancelItem)
                .toList();

        return FulfillmentDeliveryCancelMapper.of(
                command.customerCode(),
                command.accessToken(),
                cancelRequests
        );
    }

    public static FulfillmentDeliveryMapper mapToRegisterRequest(RegisterFulfillmentDeliveryCommand command) {
        List<FulfillmentDeliveryItemMapper> deliveryRequests = command.deliveryRequests().stream()
                .map(FulfillmentDeliveryCommandToRequestMapper::mapItem)
                .toList();

        return FulfillmentDeliveryMapper.register(
                command.customerCode(),
                command.accessToken(),
                deliveryRequests
        );
    }

    public static FulfillmentDeliveryMapper mapToUpdateRequest(UpdateFulfillmentDeliveryCommand command) {
        List<FulfillmentDeliveryItemMapper> deliveryRequests = command.deliveryRequests().stream()
                .map(FulfillmentDeliveryCommandToRequestMapper::mapUpdateItem)
                .toList();

        return FulfillmentDeliveryMapper.update(
                command.customerCode(),
                command.accessToken(),
                deliveryRequests
        );
    }

    public static FulfillmentDeliveryCarMapper mapToRegisterCarRequest(RegisterFulfillmentDeliveryCarCommand command) {
        List<FulfillmentDeliveryCarItemMapper> deliveryRequests = command.deliveryRequests().stream()
                .map(FulfillmentDeliveryCommandToRequestMapper::mapCarItem)
                .toList();

        return FulfillmentDeliveryCarMapper.register(
                command.customerCode(),
                command.accessToken(),
                deliveryRequests
        );
    }

    public static FulfillmentDeliveryCarMapper mapToUpdateCarRequest(UpdateFulfillmentDeliveryCarCommand command) {
        List<FulfillmentDeliveryCarItemMapper> deliveryRequests = command.deliveryRequests().stream()
                .map(FulfillmentDeliveryCommandToRequestMapper::mapUpdateCarItem)
                .toList();

        return FulfillmentDeliveryCarMapper.update(
                command.customerCode(),
                command.accessToken(),
                deliveryRequests
        );
    }

    public static FulfillmentDeliveryIcsMapper mapToRegisterIcsRequest(RegisterFulfillmentDeliveryIcsCommand command) {
        List<FulfillmentDeliveryIcsItemMapper> deliveryRequests = command.deliveryRequests().stream()
                .map(FulfillmentDeliveryCommandToRequestMapper::mapIcsItem)
                .toList();

        return FulfillmentDeliveryIcsMapper.register(
                command.customerCode(),
                command.accessToken(),
                deliveryRequests
        );
    }

    private static FulfillmentDeliveryItemMapper mapItem(RegisterFulfillmentDeliveryItemCommand item) {
        List<FulfillmentDeliveryGoodsMapper> goods = item.godCds().stream()
                .map(FulfillmentDeliveryCommandToRequestMapper::mapGoods)
                .toList();

        return FulfillmentDeliveryItemMapper.of(
                item.ordDt(),
                item.ordNo(),
                item.ordSeq(),
                item.slipNo(),
                item.custNm(),
                item.custTelNo(),
                item.custAddr(),
                item.outWay(),
                item.sendNm(),
                item.sendTelNo(),
                item.salChanel(),
                item.shipReqTerm(),
                goods,
                item.oneDayDeliveryCd(),
                item.remark()
        );
    }

    private static FulfillmentDeliveryItemMapper mapUpdateItem(UpdateFulfillmentDeliveryItemCommand item) {
        List<FulfillmentDeliveryGoodsMapper> goods = item.godCds().stream()
                .map(FulfillmentDeliveryCommandToRequestMapper::mapGoods)
                .toList();

        return FulfillmentDeliveryItemMapper.update(
                item.ordDt(),
                item.ordNo(),
                item.ordSeq(),
                item.slipNo(),
                item.custNm(),
                item.custTelNo(),
                item.custAddr(),
                item.outWay(),
                item.sendNm(),
                item.sendTelNo(),
                item.salChanel(),
                item.shipReqTerm(),
                goods,
                item.oneDayDeliveryCd(),
                item.remark()
        );
    }

    private static FulfillmentDeliveryCarItemMapper mapCarItem(RegisterFulfillmentDeliveryCarItemCommand item) {
        List<FulfillmentDeliveryGoodsMapper> goods = item.godCds().stream()
                .map(FulfillmentDeliveryCommandToRequestMapper::mapGoods)
                .toList();

        return FulfillmentDeliveryCarItemMapper.of(
                item.ordDt(),
                item.ordNo(),
                item.slipNo(),
                item.outWay(),
                item.cstShopCd(),
                goods,
                item.remark()
        );
    }

    private static FulfillmentDeliveryCarItemMapper mapUpdateCarItem(UpdateFulfillmentDeliveryCarItemCommand item) {
        List<FulfillmentDeliveryGoodsMapper> goods = item.godCds().stream()
                .map(FulfillmentDeliveryCommandToRequestMapper::mapGoods)
                .toList();

        return FulfillmentDeliveryCarItemMapper.update(
                item.ordDt(),
                item.ordNo(),
                item.slipNo(),
                item.outWay(),
                item.cstShopCd(),
                goods,
                item.remark()
        );
    }

    private static FulfillmentDeliveryGoodsMapper mapGoods(RegisterFulfillmentDeliveryGoodsCommand item) {
        return FulfillmentDeliveryGoodsMapper.of(
                item.cstGodCd(),
                item.distTermDt(),
                item.ordQty()
        );
    }

    private static FulfillmentDeliveryIcsItemMapper mapIcsItem(RegisterFulfillmentDeliveryIcsItemCommand item) {
        List<FulfillmentDeliveryGoodsMapper> goods = item.godCds().stream()
                .map(FulfillmentDeliveryCommandToRequestMapper::mapGoods)
                .toList();

        return FulfillmentDeliveryIcsItemMapper.of(
                item.ordDt(),
                item.ordNo(),
                item.platform(),
                item.logiCenter(),
                item.invoiceNo(),
                item.custNm(),
                item.custTelNo(),
                item.custAddr(),
                item.sendNm(),
                item.sendTelNo(),
                item.salChanel(),
                item.shipReqTerm(),
                item.remark(),
                goods
        );
    }

    public static FulfillmentDeliveryIcsCompletionMapper mapToIcsCompletionRequest(
            CompleteFulfillmentDeliveryIcsCommand command
    ) {
        List<FulfillmentDeliveryIcsCompletionItemMapper> completionRequests = command.completionRequests().stream()
                .map(FulfillmentDeliveryCommandToRequestMapper::mapIcsCompletionItem)
                .toList();

        return FulfillmentDeliveryIcsCompletionMapper.of(
                command.customerCode(),
                command.accessToken(),
                completionRequests
        );
    }

    private static FulfillmentDeliveryIcsCompletionItemMapper mapIcsCompletionItem(
            CompleteFulfillmentDeliveryIcsItemCommand item
    ) {
        return FulfillmentDeliveryIcsCompletionItemMapper.of(item.ordNoList());
    }

    private static FulfillmentDeliveryCancelItemMapper mapCancelItem(CancelFulfillmentDeliveryItemCommand item) {
        return FulfillmentDeliveryCancelItemMapper.of(
                item.slipNo(),
                item.ordNo()
        );
    }
}
