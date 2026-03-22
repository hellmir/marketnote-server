package com.personal.marketnote.commerce.adapter.in.web.order.mapper;

import com.personal.marketnote.commerce.adapter.in.web.order.request.ChangeOrderStatusRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.request.RegisterOrderRequest;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
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
                .totalAmount(request.getTotalAmount())
                .couponAmount(request.getCouponAmount())
                .pointAmount(request.getPointAmount())
                .shippingFee(request.getShippingFee())
                .recipientName(request.getRecipientName())
                .recipientPhoneNumber(request.getRecipientPhoneNumber())
                .zipCode(request.getZipCode())
                .address(request.getAddress())
                .addressDetail(request.getAddressDetail())
                .requestMessage(request.getRequestMessage())
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
                .pickupRecipientName(request.getPickupRecipientName())
                .pickupRecipientPhoneNumber(request.getPickupRecipientPhoneNumber())
                .pickupZipCode(request.getPickupZipCode())
                .pickupAddress(request.getPickupAddress())
                .pickupAddressDetail(request.getPickupAddressDetail())
                .pickupRequestMessage(request.getPickupRequestMessage())
                .build();
    }
}

