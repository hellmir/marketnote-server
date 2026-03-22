package com.personal.marketnote.commerce.adapter.out.persistence.order.entity;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.common.utility.FormatValidator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;

@Entity
@Table(name = "orders",
        indexes = @Index(name = "idx_orders_buyer_id_created_at", columnList = "buyer_id, created_at"))
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class OrderJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "order_key", nullable = false, unique = true)
    private UUID orderKey;

    @Column(name = "order_number", nullable = false, length = 31, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 31)
    private OrderStatus orderStatus;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "paid_amount")
    private Long paidAmount;

    @Column(name = "coupon_amount")
    private Long couponAmount;

    @Column(name = "point_amount")
    private Long pointAmount;

    @Column(name = "shipping_fee")
    private Long shippingFee;

    @Column(name = "delivery_recipient_name", length = 50)
    private String recipientName;

    @Column(name = "delivery_recipient_phone_number", length = 20)
    private String recipientPhoneNumber;

    @Column(name = "delivery_zip_code", length = 10)
    private String zipCode;

    @Column(name = "delivery_address", length = 255)
    private String address;

    @Column(name = "delivery_address_detail", length = 255)
    private String addressDetail;

    @Column(name = "delivery_request_message", length = 100)
    private String requestMessage;

    @Column(name = "pickup_recipient_name", length = 50)
    private String pickupRecipientName;

    @Column(name = "pickup_recipient_phone_number", length = 20)
    private String pickupRecipientPhoneNumber;

    @Column(name = "pickup_zip_code", length = 10)
    private String pickupZipCode;

    @Column(name = "pickup_address", length = 255)
    private String pickupAddress;

    @Column(name = "pickup_address_detail", length = 255)
    private String pickupAddressDetail;

    @Column(name = "pickup_request_message", length = 60)
    private String pickupRequestMessage;

    @OneToMany(mappedBy = "orderJpaEntity", cascade = {PERSIST, MERGE}, orphanRemoval = true)
    @Builder.Default
    private List<OrderProductJpaEntity> orderProductJpaEntities = new ArrayList<>();

    public static OrderJpaEntity from(Order order) {
        return OrderJpaEntity.builder()
                .buyerId(order.getBuyerId())
                .orderKey(order.getOrderKey())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .paidAmount(order.getPaidAmount())
                .couponAmount(order.getCouponAmount())
                .pointAmount(order.getPointAmount())
                .shippingFee(order.getShippingFee())
                .recipientName(order.getRecipientName())
                .recipientPhoneNumber(order.getRecipientPhoneNumber())
                .zipCode(order.getZipCode())
                .address(order.getAddress())
                .addressDetail(order.getAddressDetail())
                .requestMessage(order.getRequestMessage())
                .pickupRecipientName(order.getPickupRecipientName())
                .pickupRecipientPhoneNumber(order.getPickupRecipientPhoneNumber())
                .pickupZipCode(order.getPickupZipCode())
                .pickupAddress(order.getPickupAddress())
                .pickupAddressDetail(order.getPickupAddressDetail())
                .pickupRequestMessage(order.getPickupRequestMessage())
                .build();
    }

    public void addOrderProduct(OrderProductJpaEntity orderProductJpaEntity) {
        orderProductJpaEntities.add(orderProductJpaEntity);
    }

    public void updateFrom(Order order) {
        orderStatus = order.getOrderStatus();
        totalAmount = order.getTotalAmount();
        paidAmount = order.getPaidAmount();
        couponAmount = order.getCouponAmount();
        pointAmount = order.getPointAmount();
        shippingFee = order.getShippingFee();
        pickupRecipientName = order.getPickupRecipientName();
        pickupRecipientPhoneNumber = order.getPickupRecipientPhoneNumber();
        pickupZipCode = order.getPickupZipCode();
        pickupAddress = order.getPickupAddress();
        pickupAddressDetail = order.getPickupAddressDetail();
        pickupRequestMessage = order.getPickupRequestMessage();

        Map<Long, OrderProduct> orderProductsByPricePolicyId = order.getOrderProducts()
                .stream()
                .collect(Collectors.toMap(
                        OrderProduct::getPricePolicyId,
                        op -> op
                ));

        orderProductJpaEntities.forEach(entity -> {
            OrderProduct orderProduct = orderProductsByPricePolicyId.get(entity.getPricePolicyId());
            if (FormatValidator.hasValue(orderProduct)) {
                entity.updateFrom(orderProduct);
            }
        });
    }
}
