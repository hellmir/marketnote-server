package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery.*;
import com.personal.marketnote.fulfillment.port.in.command.vendor.*;

import java.util.List;

public class FasstoDeliveryCommandToRequestMapper {
    public static FasstoDeliveryQuery mapToQuery(GetFasstoDeliveriesCommand command) {
        return FasstoDeliveryQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.startDate(),
                command.endDate(),
                command.status(),
                command.outDiv(),
                command.ordNo()
        );
    }

    public static FasstoDeliveryDetailQuery mapToDetailQuery(GetFasstoDeliveryDetailCommand command) {
        return FasstoDeliveryDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.slipNo(),
                command.ordNo()
        );
    }

    public static FasstoDeliveryStatusQuery mapToDeliveryStatusQuery(GetFasstoDeliveryStatusesCommand command) {
        return FasstoDeliveryStatusQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.startDate(),
                command.endDate(),
                command.outDiv()
        );
    }

    public static FasstoDeliveryOutOrdGoodsDetailQuery mapToOutOrdGoodsDetailQuery(
            GetFasstoDeliveryOutOrdGoodsDetailCommand command
    ) {
        return FasstoDeliveryOutOrdGoodsDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.outOrdSlipNo()
        );
    }

    public static FasstoDeliveryOutOrdGoodsByOrdNoQuery mapToOutOrdGoodsByOrdNoQuery(
            GetFasstoDeliveryOutOrdGoodsByOrdNoCommand command
    ) {
        return FasstoDeliveryOutOrdGoodsByOrdNoQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.startDate(),
                command.endDate(),
                command.ordNo()
        );
    }

    public static FasstoDeliveryCancelMapper mapToCancelRequest(CancelFasstoDeliveryCommand command) {
        List<FasstoDeliveryCancelItemMapper> cancelRequests = command.deliveries().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapCancelItem)
                .toList();

        return FasstoDeliveryCancelMapper.of(
                command.customerCode(),
                command.accessToken(),
                cancelRequests
        );
    }

    public static FasstoDeliveryMapper mapToRegisterRequest(RegisterFasstoDeliveryCommand command) {
        List<FasstoDeliveryItemMapper> deliveryRequests = command.deliveryRequests().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapItem)
                .toList();

        return FasstoDeliveryMapper.register(
                command.customerCode(),
                command.accessToken(),
                deliveryRequests
        );
    }

    public static FasstoDeliveryMapper mapToUpdateRequest(UpdateFasstoDeliveryCommand command) {
        List<FasstoDeliveryItemMapper> deliveryRequests = command.deliveryRequests().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapUpdateItem)
                .toList();

        return FasstoDeliveryMapper.update(
                command.customerCode(),
                command.accessToken(),
                deliveryRequests
        );
    }

    public static FasstoDeliveryCarMapper mapToRegisterCarRequest(RegisterFasstoDeliveryCarCommand command) {
        List<FasstoDeliveryCarItemMapper> deliveryRequests = command.deliveryRequests().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapCarItem)
                .toList();

        return FasstoDeliveryCarMapper.register(
                command.customerCode(),
                command.accessToken(),
                deliveryRequests
        );
    }

    public static FasstoDeliveryCarMapper mapToUpdateCarRequest(UpdateFasstoDeliveryCarCommand command) {
        List<FasstoDeliveryCarItemMapper> deliveryRequests = command.deliveryRequests().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapUpdateCarItem)
                .toList();

        return FasstoDeliveryCarMapper.update(
                command.customerCode(),
                command.accessToken(),
                deliveryRequests
        );
    }

    public static FasstoDeliveryIcsMapper mapToRegisterIcsRequest(RegisterFasstoDeliveryIcsCommand command) {
        List<FasstoDeliveryIcsItemMapper> deliveryRequests = command.deliveryRequests().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapIcsItem)
                .toList();

        return FasstoDeliveryIcsMapper.register(
                command.customerCode(),
                command.accessToken(),
                deliveryRequests
        );
    }

    private static FasstoDeliveryItemMapper mapItem(RegisterFasstoDeliveryItemCommand item) {
        List<FasstoDeliveryGoodsMapper> goods = item.godCds().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapGoods)
                .toList();

        return FasstoDeliveryItemMapper.of(
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

    private static FasstoDeliveryItemMapper mapUpdateItem(UpdateFasstoDeliveryItemCommand item) {
        List<FasstoDeliveryGoodsMapper> goods = item.godCds().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapGoods)
                .toList();

        return FasstoDeliveryItemMapper.update(
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

    private static FasstoDeliveryCarItemMapper mapCarItem(RegisterFasstoDeliveryCarItemCommand item) {
        List<FasstoDeliveryGoodsMapper> goods = item.godCds().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapGoods)
                .toList();

        return FasstoDeliveryCarItemMapper.of(
                item.ordDt(),
                item.ordNo(),
                item.slipNo(),
                item.outWay(),
                item.cstShopCd(),
                goods,
                item.remark()
        );
    }

    private static FasstoDeliveryCarItemMapper mapUpdateCarItem(UpdateFasstoDeliveryCarItemCommand item) {
        List<FasstoDeliveryGoodsMapper> goods = item.godCds().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapGoods)
                .toList();

        return FasstoDeliveryCarItemMapper.update(
                item.ordDt(),
                item.ordNo(),
                item.slipNo(),
                item.outWay(),
                item.cstShopCd(),
                goods,
                item.remark()
        );
    }

    private static FasstoDeliveryGoodsMapper mapGoods(RegisterFasstoDeliveryGoodsCommand item) {
        return FasstoDeliveryGoodsMapper.of(
                item.cstGodCd(),
                item.distTermDt(),
                item.ordQty()
        );
    }

    private static FasstoDeliveryIcsItemMapper mapIcsItem(RegisterFasstoDeliveryIcsItemCommand item) {
        List<FasstoDeliveryGoodsMapper> goods = item.godCds().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapGoods)
                .toList();

        return FasstoDeliveryIcsItemMapper.of(
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

    private static FasstoDeliveryCancelItemMapper mapCancelItem(CancelFasstoDeliveryItemCommand item) {
        return FasstoDeliveryCancelItemMapper.of(
                item.slipNo(),
                item.ordNo()
        );
    }
}
