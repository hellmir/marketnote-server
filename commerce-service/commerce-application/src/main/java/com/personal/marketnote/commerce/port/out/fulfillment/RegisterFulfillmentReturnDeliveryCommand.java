package com.personal.marketnote.commerce.port.out.fulfillment;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.ShippingAddress;
import com.personal.marketnote.commerce.port.in.command.order.RequestReturnCommand;
import lombok.Builder;

import java.util.List;

@Builder
public record RegisterFulfillmentReturnDeliveryCommand(
        Long orderId,
        String orderDate,
        String recipientName,
        String recipientPhoneNumber,
        String recipientAddress,
        String pickupRecipientName,
        String pickupRecipientPhoneNumber,
        String pickupZipCode,
        String pickupAddress,
        String pickupAddressDetail,
        String returnReason,
        String returnDetailReason,
        String returnShippingRequest,
        List<ProductItem> products
) {
    public record ProductItem(
            String productCode,
            Integer quantity
    ) {
        public static ProductItem of(String productCode, Integer quantity) {
            return new ProductItem(productCode, quantity);
        }
    }

    public static RegisterFulfillmentReturnDeliveryCommand from(
            RequestReturnCommand command, Order order, DerivedInputs derived
    ) {
        ShippingAddress shippingAddress = order.getShippingAddress();
        return RegisterFulfillmentReturnDeliveryCommand.builder()
                .orderId(command.id())
                .orderDate(derived.orderDate())
                .recipientName(shippingAddress.getRecipientName().getValue())
                .recipientPhoneNumber(shippingAddress.getRecipientPhoneNumber().getValue())
                .recipientAddress(shippingAddress.getFullAddress())
                .pickupRecipientName(command.pickupRecipientName())
                .pickupRecipientPhoneNumber(derived.pickupPhoneCanonical())
                .pickupZipCode(command.pickupZipCode())
                .pickupAddress(command.pickupAddress())
                .pickupAddressDetail(command.pickupAddressDetail())
                .returnReason(derived.reasonCategory())
                .returnDetailReason(command.reason())
                .returnShippingRequest(command.pickupRequestMessage())
                .products(derived.products())
                .build();
    }

    public record DerivedInputs(
            List<ProductItem> products,
            String pickupPhoneCanonical,
            String reasonCategory,
            String orderDate
    ) {
        public static DerivedInputs of(
                List<ProductItem> products,
                String pickupPhoneCanonical,
                String reasonCategory,
                String orderDate
        ) {
            return new DerivedInputs(products, pickupPhoneCanonical, reasonCategory, orderDate);
        }
    }
}
