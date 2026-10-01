package com.personal.marketnote.commerce.port.in.result.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import com.personal.marketnote.commerce.domain.order.ShippingAddress;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.common.domain.deliveryrequestmessage.DeliveryRequestMessage;
import com.personal.marketnote.common.domain.phonenumber.PhoneNumber;
import com.personal.marketnote.common.domain.recipientname.RecipientName;
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
                .orderNumber(order.getOrderNumber().getValue())
                .orderStatus(order.getOrderStatus())
                .statusChangeReasonCategory(order.getStatusChangeReasonCategory())
                .statusChangeReason(order.getStatusChangeReason())
                .totalAmount(order.getAmount().getTotalAmount().getValue())
                .paidAmount(order.getAmount().getPaidAmount())
                .couponAmount(order.getAmount().getCouponAmount().getValue())
                .pointAmount(order.getAmount().getPointAmount().getValue())
                .shippingFee(order.getAmount().getShippingFee().getValue())
                .recipientName(toRecipientNameValue(order.getShippingAddress().getRecipientName()))
                .recipientPhoneNumber(toPhoneNumberValue(order.getShippingAddress().getRecipientPhoneNumber()))
                .zipCode(order.getShippingAddress().getZipCode())
                .address(order.getShippingAddress().getAddress())
                .addressDetail(order.getShippingAddress().getAddressDetail())
                .deliveryRequestType(order.getShippingAddress().getDeliveryRequestType())
                .deliveryRequestMessage(toDeliveryRequestMessageValue(
                        order.getShippingAddress().getDeliveryRequestMessage()
                ))
                .pickupRecipientName(toRecipientNameValue(resolvePickupField(order, ShippingAddress::getRecipientName)))
                .pickupRecipientPhoneNumber(toPhoneNumberValue(
                        resolvePickupField(order, ShippingAddress::getRecipientPhoneNumber)
                ))
                .pickupZipCode(resolvePickupField(order, ShippingAddress::getZipCode))
                .pickupAddress(resolvePickupField(order, ShippingAddress::getAddress))
                .pickupAddressDetail(resolvePickupField(order, ShippingAddress::getAddressDetail))
                .pickupDeliveryRequestType(resolvePickupField(order, ShippingAddress::getDeliveryRequestType))
                .pickupDeliveryRequestMessage(toDeliveryRequestMessageValue(
                        resolvePickupField(order, ShippingAddress::getDeliveryRequestMessage)
                ))
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

    private static String toPhoneNumberValue(PhoneNumber phoneNumber) {
        if (FormatValidator.hasNoValue(phoneNumber)) {
            return null;
        }
        return phoneNumber.getValue();
    }

    private static String toDeliveryRequestMessageValue(DeliveryRequestMessage deliveryRequestMessage) {
        if (FormatValidator.hasNoValue(deliveryRequestMessage)) {
            return null;
        }
        return deliveryRequestMessage.getValue();
    }

    private static String toRecipientNameValue(RecipientName recipientName) {
        if (FormatValidator.hasNoValue(recipientName)) {
            return null;
        }
        return recipientName.getValue();
    }
}
