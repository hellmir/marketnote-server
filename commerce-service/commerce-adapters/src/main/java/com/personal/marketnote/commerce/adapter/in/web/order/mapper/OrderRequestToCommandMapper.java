package com.personal.marketnote.commerce.adapter.in.web.order.mapper;

import com.personal.marketnote.commerce.adapter.in.web.order.request.ChangeOrderStatusRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.request.RegisterOrderRequest;
import com.personal.marketnote.commerce.domain.order.ShippingAddress;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.command.order.OrderAmountCommand;
import com.personal.marketnote.commerce.port.in.command.order.OrderProductItemCommand;
import com.personal.marketnote.commerce.port.in.command.order.RegisterOrderCommand;

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
                        .sharerId(item.getSharerId())
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
                .shippingAddress(ShippingAddress.of(
                        request.getRecipientName(),
                        request.getRecipientPhoneNumber(),
                        request.getZipCode(),
                        request.getAddress(),
                        request.getAddressDetail(),
                        request.getRequestMessage()
                ))
                .orderProducts(orderProducts)
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
                        request.getPickupRequestMessage()
                ))
                .build();
    }
}
