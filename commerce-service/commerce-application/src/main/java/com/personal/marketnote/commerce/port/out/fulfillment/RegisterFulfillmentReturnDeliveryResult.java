package com.personal.marketnote.commerce.port.out.fulfillment;

public record RegisterFulfillmentReturnDeliveryResult(
        Long orderId,
        String returnSlipNumber,
        boolean registered,
        String message
) {
    public static RegisterFulfillmentReturnDeliveryResult of(
            Long orderId,
            String returnSlipNumber,
            boolean registered,
            String message
    ) {
        return new RegisterFulfillmentReturnDeliveryResult(orderId, returnSlipNumber, registered, message);
    }
}
