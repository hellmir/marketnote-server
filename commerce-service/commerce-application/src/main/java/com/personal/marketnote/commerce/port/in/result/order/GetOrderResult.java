package com.personal.marketnote.commerce.port.in.result.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import com.personal.marketnote.commerce.domain.order.ShippingAddress;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Builder(access = AccessLevel.PRIVATE)
public record GetOrderResult(
        Long id,
        Long buyerId,
        String orderNumber,
        OrderStatus orderStatus,
        OrderStatusReasonCategory statusChangeReasonCategory,
        String statusChangeReason,
        Long totalAmount,
        Long paidAmount,
        Long couponAmount,
        Long pointAmount,
        Long shippingFee,
        String recipientName,
        String recipientPhoneNumber,
        String zipCode,
        String address,
        String addressDetail,
        DeliveryRequestType deliveryRequestType,
        String deliveryRequestMessage,
        String pickupRecipientName,
        String pickupRecipientPhoneNumber,
        String pickupZipCode,
        String pickupAddress,
        String pickupAddressDetail,
        DeliveryRequestType pickupDeliveryRequestType,
        String pickupDeliveryRequestMessage,
        List<GetOrderProductResult> orderProducts
) {
    public static GetOrderResult from(
            Order order,
            Map<Long, ProductInfoResult> productInfoResultsByPricePolicyId
    ) {
        return GetOrderResult.builder()
                .id(order.getId())
                .buyerId(order.getBuyerId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .statusChangeReasonCategory(order.getStatusChangeReasonCategory())
                .statusChangeReason(order.getStatusChangeReason())
                .totalAmount(order.getAmount().getTotalAmount())
                .paidAmount(order.getAmount().getPaidAmount())
                .couponAmount(order.getAmount().getCouponAmount())
                .pointAmount(order.getAmount().getPointAmount())
                .shippingFee(order.getAmount().getShippingFee())
                .recipientName(order.getShippingAddress().getRecipientName())
                .recipientPhoneNumber(order.getShippingAddress().getRecipientPhoneNumber())
                .zipCode(order.getShippingAddress().getZipCode())
                .address(order.getShippingAddress().getAddress())
                .addressDetail(order.getShippingAddress().getAddressDetail())
                .deliveryRequestType(order.getShippingAddress().getDeliveryRequestType())
                .deliveryRequestMessage(order.getShippingAddress().getDeliveryRequestMessage())
                .pickupRecipientName(resolvePickupField(order, ShippingAddress::getRecipientName))
                .pickupRecipientPhoneNumber(resolvePickupField(order, ShippingAddress::getRecipientPhoneNumber))
                .pickupZipCode(resolvePickupField(order, ShippingAddress::getZipCode))
                .pickupAddress(resolvePickupField(order, ShippingAddress::getAddress))
                .pickupAddressDetail(resolvePickupField(order, ShippingAddress::getAddressDetail))
                .pickupDeliveryRequestType(resolvePickupField(order, ShippingAddress::getDeliveryRequestType))
                .pickupDeliveryRequestMessage(resolvePickupField(order, ShippingAddress::getDeliveryRequestMessage))
                .orderProducts(order.getOrderProducts().stream()
                        .map(orderProduct -> GetOrderProductResult.from(
                                        orderProduct,
                                        productInfoResultsByPricePolicyId.get(orderProduct.getPricePolicyId()),
                                        order.getOrderStatus()
                                )
                        )
                        .toList())
                .build();
    }

    private static <T> T resolvePickupField(Order order, Function<ShippingAddress, T> extractor) {
        if (FormatValidator.hasNoValue(order.getPickupAddress())) {
            return null;
        }
        return extractor.apply(order.getPickupAddress());
    }
}
