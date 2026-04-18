package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryGoodsMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.returndelivery.FulfillmentDirectReturnDeliveryItemMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.returndelivery.FulfillmentDirectReturnDeliveryMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDirectReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDirectReturnDeliveryItemCommand;

import java.util.List;

public class FasstoDirectReturnDeliveryCommandToRequestMapper {

    public static FulfillmentDirectReturnDeliveryMapper mapToRegisterRequest(RegisterFulfillmentDirectReturnDeliveryCommand command) {
        List<FulfillmentDirectReturnDeliveryItemMapper> directReturnDeliveryRequests = command.directReturnDeliveryRequests().stream()
                .map(FasstoDirectReturnDeliveryCommandToRequestMapper::mapItem)
                .toList();

        return FulfillmentDirectReturnDeliveryMapper.register(
                command.customerCode(),
                command.accessToken(),
                directReturnDeliveryRequests
        );
    }

    private static FulfillmentDirectReturnDeliveryItemMapper mapItem(RegisterFulfillmentDirectReturnDeliveryItemCommand item) {
        List<FulfillmentDeliveryGoodsMapper> goods = FormatValidator.hasValue(item.products())
                ? item.products().stream()
                .map(FasstoDirectReturnDeliveryCommandToRequestMapper::mapGoods)
                .toList()
                : List.of();

        return FulfillmentDirectReturnDeliveryItemMapper.of(
                item.orderDate(),
                item.supplierCode(),
                item.originalCourierCode(),
                item.originalInvoiceNumber(),
                item.returnReceiveMethod(),
                item.recipientName(),
                item.returnCourierCompany(),
                item.returnInvoiceNumber(),
                item.returnType(),
                item.returnReason(),
                item.returnDetailReason(),
                item.remark(),
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
