package com.personal.marketnote.commerce.adapter.in.web.order.mapper;

import com.personal.marketnote.commerce.adapter.in.web.order.request.CancelOrderRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.request.ChangeOrderStatusRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.request.RegisterOrderRequest;
import com.personal.marketnote.commerce.domain.order.ShippingAddress;
import com.personal.marketnote.commerce.port.in.command.order.*;

import java.util.List;

public class OrderRequestToCommandMapper {
    public static RegisterOrderCommand mapToCommand(
            RegisterOrderRequest request,
            Long buyerId
    ) {
        List<OrderProductItemCommand> orderProducts = request.getOrderProducts().stream()
                .map(item -> OrderProductItemCommand.builder()
                        .productId(item.getProductId())
                        .sellerId(item.getSellerId())
                        .pricePolicyId(item.getPricePolicyId())
                        .sharerKey(item.getSharerKey())
                        .quantity(item.getQuantity())
                        .unitAmount(item.getUnitAmount())
                        .imageUrl(item.getImageUrl())
                        .build())
                .toList();

        return RegisterOrderCommand.builder()
                .buyerId(buyerId)
                .amount(OrderAmountCommand.builder()
                        .totalAmount(request.getTotalAmount())
                        .couponAmount(request.getCouponAmount())
                        .pointAmount(request.getPointAmount())
                        .shippingFee(request.getShippingFee())
                        .build())
                .shippingAddressId(request.getShippingAddressId())
                .deliveryRequestType(request.getDeliveryRequestType())
                .deliveryRequestMessage(request.getDeliveryRequestMessage())
                .orderProducts(orderProducts)
                .build();
    }

    public static CancelOrderCommand mapToCancelCommand(Long id, CancelOrderRequest request, Long buyerId) {
        return CancelOrderCommand.builder()
                .id(id)
                .reasonCategory(request.getReasonCategory())
                .reason(request.getReason())
                .buyerId(buyerId)
                .build();
    }

    public static ChangeOrderStatusCommand mapToCommand(Long id, ChangeOrderStatusRequest request, String role, Long buyerId) {
        return ChangeOrderStatusCommand.builder()
                .id(id)
                .pricePolicyIds(request.getPricePolicyIds())
                .orderStatus(request.getOrderStatus())
                .reasonCategory(request.getReasonCategory())
                .reason(request.getReason())
                .role(role)
                .buyerId(buyerId)
                .pickupAddress(ShippingAddress.of(
                        request.getPickupRecipientName(),
                        request.getPickupRecipientPhoneNumber(),
                        request.getPickupZipCode(),
                        request.getPickupAddress(),
                        request.getPickupAddressDetail(),
                        null,
                        request.getPickupRequestMessage()
                ))
                .build();
    }
}
