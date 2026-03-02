package com.personal.marketnote.commerce.adapter.in.web.order.mapper;

import com.personal.marketnote.commerce.adapter.in.web.order.request.ChangeOrderStatusRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.request.RegisterOrderRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.request.RegisterTrackingInfoRequest;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.command.order.OrderProductItemCommand;
import com.personal.marketnote.commerce.port.in.command.order.RegisterOrderCommand;
import com.personal.marketnote.commerce.port.in.command.order.RegisterTrackingInfoCommand;

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
                .recipientName(request.getRecipientName())
                .address(request.getAddress())
                .addressDetail(request.getAddressDetail())
                .zipCode(request.getZipCode())
                .phoneNumber(request.getPhoneNumber())
                .orderProducts(orderProducts)
                .build();
    }

    public static RegisterTrackingInfoCommand mapToTrackingInfoCommand(
            Long orderId, RegisterTrackingInfoRequest request
    ) {
        return RegisterTrackingInfoCommand.builder()
                .orderId(orderId)
                .courierCompany(request.getCourierCompany())
                .trackingNumber(request.getTrackingNumber())
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
                .build();
    }
}

