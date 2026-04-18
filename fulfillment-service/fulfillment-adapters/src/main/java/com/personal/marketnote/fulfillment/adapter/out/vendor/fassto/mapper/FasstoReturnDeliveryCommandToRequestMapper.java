package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryGoodsMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.returndelivery.FulfillmentReturnDeliveryItemMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.returndelivery.FulfillmentReturnDeliveryMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.returndelivery.FulfillmentReturnGodDetailQuery;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryItemCommand;

import java.util.List;

public class FasstoReturnDeliveryCommandToRequestMapper {

    public static FulfillmentReturnGodDetailQuery mapToReturnGodDetailQuery(GetFulfillmentReturnGodDetailCommand command) {
        return FulfillmentReturnGodDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.startDate(),
                command.endDate(),
                command.returnSlipNumbers(),
                command.warehouseCode()
        );
    }

    public static FulfillmentReturnDeliveryMapper mapToRegisterRequest(RegisterFulfillmentReturnDeliveryCommand command) {
        List<FulfillmentReturnDeliveryItemMapper> returnDeliveryRequests = command.returnDeliveryRequests().stream()
                .map(FasstoReturnDeliveryCommandToRequestMapper::mapItem)
                .toList();

        return FulfillmentReturnDeliveryMapper.register(
                command.customerCode(),
                command.accessToken(),
                returnDeliveryRequests
        );
    }

    private static FulfillmentReturnDeliveryItemMapper mapItem(RegisterFulfillmentReturnDeliveryItemCommand item) {
        List<FulfillmentDeliveryGoodsMapper> goods = FormatValidator.hasValue(item.products())
                ? item.products().stream()
                .map(FasstoReturnDeliveryCommandToRequestMapper::mapGoods)
                .toList()
                : List.of();

        return FulfillmentReturnDeliveryItemMapper.of(
                item.orderDate(),
                item.orderNumber(),
                item.courierCode(),
                item.invoiceNumber(),
                item.recipientName(),
                item.recipientPhoneNumber(),
                item.recipientAddress(),
                item.returnReceiverName(),
                item.returnReceiverPhoneNumber(),
                item.returnZipCode(),
                item.returnAddress1(),
                item.returnAddress2(),
                item.returnType(),
                item.returnReason(),
                item.returnDetailReason(),
                item.returnShippingRequest(),
                goods
        );
    }

    private static FulfillmentDeliveryGoodsMapper mapGoods(RegisterFulfillmentDeliveryGoodsCommand item) {
        return FulfillmentDeliveryGoodsMapper.of(
                item.productCode(),
                item.expirationDate(),
                item.orderQuantity()
        );
    }
}
