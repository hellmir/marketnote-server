package com.personal.marketnote.commerce.adapter.out.mapper;

import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderProductJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderStatusHistoryJpaEntity;
import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.common.utility.FormatValidator;

import java.util.List;
import java.util.Optional;

public class OrderJpaEntityToDomainMapper {
    public static Optional<Order> mapToDomain(OrderJpaEntity orderJpaEntity) {
        return Optional.ofNullable(orderJpaEntity)
                .map(entity -> {
                    List<OrderProductSnapshotState> productStates = entity.getOrderProductJpaEntities().stream()
                            .map(OrderJpaEntityToDomainMapper::mapToOrderProductSnapshotState)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .toList();

                    return Order.from(
                            OrderSnapshotState.builder()
                                    .id(entity.getId())
                                    .buyerId(entity.getBuyerId())
                                    .orderKey(entity.getOrderKey())
                                    .orderNumber(entity.getOrderNumber())
                                    .orderStatus(entity.getOrderStatus())
                                    .totalAmount(entity.getTotalAmount())
                                    .paidAmount(entity.getPaidAmount())
                                    .couponAmount(entity.getCouponAmount())
                                    .pointAmount(entity.getPointAmount())
                                    .shippingFee(entity.getShippingFee())
                                    .shippingAddress(mapToShippingAddress(entity))
                                    .pickupAddress(mapToPickupAddress(entity))
                                    .orderProductStates(productStates)
                                    .createdAt(entity.getCreatedAt())
                                    .modifiedAt(entity.getModifiedAt())
                                    .build()
                    );
                });
    }

    public static Optional<Order> mapToDomainWithStatusInfo(
            OrderJpaEntity orderJpaEntity, OrderStatusHistoryJpaEntity orderStatusInfo
    ) {
        return Optional.ofNullable(orderJpaEntity)
                .map(entity -> {
                    List<OrderProductSnapshotState> productStates = entity.getOrderProductJpaEntities().stream()
                            .map(OrderJpaEntityToDomainMapper::mapToOrderProductSnapshotState)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .toList();

                    return Order.from(
                            OrderSnapshotState.builder()
                                    .id(entity.getId())
                                    .buyerId(entity.getBuyerId())
                                    .orderKey(entity.getOrderKey())
                                    .orderNumber(entity.getOrderNumber())
                                    .orderStatus(entity.getOrderStatus())
                                    .statusChangeReasonCategory(orderStatusInfo.getReasonCategory())
                                    .statusChangeReason(orderStatusInfo.getReason())
                                    .totalAmount(entity.getTotalAmount())
                                    .paidAmount(entity.getPaidAmount())
                                    .couponAmount(entity.getCouponAmount())
                                    .pointAmount(entity.getPointAmount())
                                    .shippingFee(entity.getShippingFee())
                                    .shippingAddress(mapToShippingAddress(entity))
                                    .pickupAddress(mapToPickupAddress(entity))
                                    .orderProductStates(productStates)
                                    .createdAt(entity.getCreatedAt())
                                    .modifiedAt(entity.getModifiedAt())
                                    .build()
                    );
                });
    }

    private static Optional<OrderProductSnapshotState> mapToOrderProductSnapshotState(
            OrderProductJpaEntity orderProductJpaEntity
    ) {
        return Optional.ofNullable(orderProductJpaEntity)
                .map(entity -> OrderProductSnapshotState.builder()
                        .orderId(entity.getId().getOrderId())
                        .sellerId(entity.getSellerId())
                        .pricePolicyId(entity.getId().getPricePolicyId())
                        .sharerId(entity.getSharerId())
                        .quantity(entity.getQuantity())
                        .unitAmount(entity.getUnitAmount())
                        .imageUrl(entity.getImageUrl())
                        .orderStatus(entity.getOrderStatus())
                        .isReviewed(entity.getIsReviewed())
                        .confirmedAt(entity.getConfirmedAt())
                        .build());
    }

    public static OrderStatusHistory mapToOrderStatusHistoryDomain(
            OrderStatusHistoryJpaEntity entity
    ) {
        return OrderStatusHistory.from(
                OrderStatusHistorySnapshotState.builder()
                        .id(entity.getId())
                        .orderId(entity.getOrderJpaEntity().getId())
                        .orderStatus(entity.getOrderStatus())
                        .reasonCategory(entity.getReasonCategory())
                        .reason(entity.getReason())
                        .createdAt(entity.getCreatedAt())
                        .build()
        );
    }

    private static ShippingAddress mapToShippingAddress(OrderJpaEntity entity) {
        return ShippingAddress.of(
                entity.getRecipientName(),
                entity.getRecipientPhoneNumber(),
                entity.getZipCode(),
                entity.getAddress(),
                entity.getAddressDetail(),
                entity.getRequestMessage()
        );
    }

    private static ShippingAddress mapToPickupAddress(OrderJpaEntity entity) {
        if (FormatValidator.hasNoValue(entity.getPickupRecipientName())) {
            return null;
        }

        return ShippingAddress.of(
                entity.getPickupRecipientName(),
                entity.getPickupRecipientPhoneNumber(),
                entity.getPickupZipCode(),
                entity.getPickupAddress(),
                entity.getPickupAddressDetail(),
                entity.getPickupRequestMessage()
        );
    }

    public static Optional<OrderProduct> mapToOrderProductDomain(OrderProductJpaEntity orderProductJpaEntity) {
        return Optional.ofNullable(orderProductJpaEntity)
                .map(entity -> OrderProduct.from(
                        OrderProductSnapshotState.builder()
                                .orderId(entity.getId().getOrderId())
                                .sellerId(entity.getSellerId())
                                .pricePolicyId(entity.getId().getPricePolicyId())
                                .sharerId(entity.getSharerId())
                                .quantity(entity.getQuantity())
                                .unitAmount(entity.getUnitAmount())
                                .imageUrl(entity.getImageUrl())
                                .orderStatus(entity.getOrderStatus())
                                .isReviewed(entity.getIsReviewed())
                                .confirmedAt(entity.getConfirmedAt())
                                .build()
                ));
    }
}
