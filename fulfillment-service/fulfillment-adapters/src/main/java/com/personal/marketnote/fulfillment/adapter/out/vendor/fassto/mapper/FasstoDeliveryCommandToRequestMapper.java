package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.*;
import com.personal.marketnote.fulfillment.port.in.command.vendor.*;

import java.util.List;

public class FasstoDeliveryCommandToRequestMapper {
    public static FulfillmentDeliveryQuery mapToQuery(GetFulfillmentDeliveriesCommand command) {
        return FulfillmentDeliveryQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.startDate(),
                command.endDate(),
                command.status(),
                command.releaseType(),
                command.orderNumber()
        );
    }

    public static FulfillmentDeliveryDetailQuery mapToDetailQuery(GetFulfillmentDeliveryDetailCommand command) {
        return FulfillmentDeliveryDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.slipNumber(),
                command.orderNumber()
        );
    }

    public static FulfillmentDeliveryStatusQuery mapToDeliveryStatusQuery(GetFulfillmentDeliveryStatusesCommand command) {
        return FulfillmentDeliveryStatusQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.startDate(),
                command.endDate(),
                command.releaseType()
        );
    }

    public static FulfillmentDeliveryOutOrdGoodsDetailQuery mapToOutOrdGoodsDetailQuery(
            GetFulfillmentDeliveryOutOrdGoodsDetailCommand command
    ) {
        return FulfillmentDeliveryOutOrdGoodsDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.releaseOrderSlipNumber()
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
                command.orderNumber()
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
                command.orderNumber()
        );
    }

    public static FulfillmentDeliveryCancelMapper mapToCancelRequest(CancelFulfillmentDeliveryCommand command) {
        List<FulfillmentDeliveryCancelItemMapper> cancelRequests = command.deliveries().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapCancelItem)
                .toList();

        return FulfillmentDeliveryCancelMapper.of(
                command.customerCode(),
                command.accessToken(),
                cancelRequests
        );
    }

    public static FulfillmentDeliveryMapper mapToRegisterRequest(RegisterFulfillmentDeliveryCommand command) {
        List<FulfillmentDeliveryItemMapper> deliveryRequests = command.deliveryRequests().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapItem)
                .toList();

        return FulfillmentDeliveryMapper.register(
                command.customerCode(),
                command.accessToken(),
                deliveryRequests
        );
    }

    public static FulfillmentDeliveryMapper mapToUpdateRequest(UpdateFulfillmentDeliveryCommand command) {
        List<FulfillmentDeliveryItemMapper> deliveryRequests = command.deliveryRequests().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapUpdateItem)
                .toList();

        return FulfillmentDeliveryMapper.update(
                command.customerCode(),
                command.accessToken(),
                deliveryRequests
        );
    }

    public static FulfillmentDeliveryCarMapper mapToRegisterCarRequest(RegisterFulfillmentDeliveryCarCommand command) {
        List<FulfillmentDeliveryCarItemMapper> deliveryRequests = command.deliveryRequests().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapCarItem)
                .toList();

        return FulfillmentDeliveryCarMapper.register(
                command.customerCode(),
                command.accessToken(),
                deliveryRequests
        );
    }

    public static FulfillmentDeliveryCarMapper mapToUpdateCarRequest(UpdateFulfillmentDeliveryCarCommand command) {
        List<FulfillmentDeliveryCarItemMapper> deliveryRequests = command.deliveryRequests().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapUpdateCarItem)
                .toList();

        return FulfillmentDeliveryCarMapper.update(
                command.customerCode(),
                command.accessToken(),
                deliveryRequests
        );
    }

    public static FulfillmentDeliveryIcsMapper mapToRegisterIcsRequest(RegisterFulfillmentDeliveryIcsCommand command) {
        List<FulfillmentDeliveryIcsItemMapper> deliveryRequests = command.deliveryRequests().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapIcsItem)
                .toList();

        return FulfillmentDeliveryIcsMapper.register(
                command.customerCode(),
                command.accessToken(),
                deliveryRequests
        );
    }

    private static FulfillmentDeliveryItemMapper mapItem(RegisterFulfillmentDeliveryItemCommand item) {
        List<FulfillmentDeliveryGoodsMapper> goods = item.products().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapGoods)
                .toList();

        return FulfillmentDeliveryItemMapper.of(
                item.orderDate(),
                item.orderNumber(),
                item.orderSequence(),
                item.slipNumber(),
                item.recipientName(),
                item.recipientPhoneNumber(),
                item.recipientAddress(),
                item.releaseMethod(),
                item.senderName(),
                item.senderPhoneNumber(),
                item.salesChannel(),
                item.shippingRequest(),
                goods,
                item.sameDayDeliveryCode(),
                item.remark()
        );
    }

    private static FulfillmentDeliveryItemMapper mapUpdateItem(UpdateFulfillmentDeliveryItemCommand item) {
        List<FulfillmentDeliveryGoodsMapper> goods = item.products().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapGoods)
                .toList();

        return FulfillmentDeliveryItemMapper.update(
                item.orderDate(),
                item.orderNumber(),
                item.orderSequence(),
                item.slipNumber(),
                item.recipientName(),
                item.recipientPhoneNumber(),
                item.recipientAddress(),
                item.releaseMethod(),
                item.senderName(),
                item.senderPhoneNumber(),
                item.salesChannel(),
                item.shippingRequest(),
                goods,
                item.sameDayDeliveryCode(),
                item.remark()
        );
    }

    private static FulfillmentDeliveryCarItemMapper mapCarItem(RegisterFulfillmentDeliveryCarItemCommand item) {
        List<FulfillmentDeliveryGoodsMapper> goods = item.products().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapGoods)
                .toList();

        return FulfillmentDeliveryCarItemMapper.of(
                item.orderDate(),
                item.orderNumber(),
                item.slipNumber(),
                item.releaseMethod(),
                item.shopCode(),
                goods,
                item.remark()
        );
    }

    private static FulfillmentDeliveryCarItemMapper mapUpdateCarItem(UpdateFulfillmentDeliveryCarItemCommand item) {
        List<FulfillmentDeliveryGoodsMapper> goods = item.products().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapGoods)
                .toList();

        return FulfillmentDeliveryCarItemMapper.update(
                item.orderDate(),
                item.orderNumber(),
                item.slipNumber(),
                item.releaseMethod(),
                item.shopCode(),
                goods,
                item.remark()
        );
    }

    private static FulfillmentDeliveryGoodsMapper mapGoods(RegisterFulfillmentDeliveryGoodsCommand item) {
        return FulfillmentDeliveryGoodsMapper.of(
                item.productCode(),
                item.expirationDate(),
                item.orderQuantity()
        );
    }

    private static FulfillmentDeliveryIcsItemMapper mapIcsItem(RegisterFulfillmentDeliveryIcsItemCommand item) {
        List<FulfillmentDeliveryGoodsMapper> goods = item.products().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapGoods)
                .toList();

        return FulfillmentDeliveryIcsItemMapper.of(
                item.orderDate(),
                item.orderNumber(),
                item.platform(),
                item.logisticsCenter(),
                item.invoiceNumber(),
                item.recipientName(),
                item.recipientPhoneNumber(),
                item.recipientAddress(),
                item.senderName(),
                item.senderPhoneNumber(),
                item.salesChannel(),
                item.shippingRequest(),
                item.remark(),
                goods
        );
    }

    public static FulfillmentDeliveryIcsCompletionMapper mapToIcsCompletionRequest(
            CompleteFulfillmentDeliveryIcsCommand command
    ) {
        List<FulfillmentDeliveryIcsCompletionItemMapper> completionRequests = command.completionRequests().stream()
                .map(FasstoDeliveryCommandToRequestMapper::mapIcsCompletionItem)
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
        return FulfillmentDeliveryIcsCompletionItemMapper.of(item.orderNumbers());
    }

    private static FulfillmentDeliveryCancelItemMapper mapCancelItem(CancelFulfillmentDeliveryItemCommand item) {
        return FulfillmentDeliveryCancelItemMapper.of(
                item.slipNumber(),
                item.orderNumber()
        );
    }
}
