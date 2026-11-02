package com.personal.marketnote.fulfillment.port.in.result;

public record RegisterInternalReturnDeliveryResult(
        Long orderId,
        String returnSlipNumber,
        boolean registered,
        String message
) {
    public static RegisterInternalReturnDeliveryResult of(
            Long orderId,
            String returnSlipNumber,
            boolean registered,
            String message
    ) {
        return new RegisterInternalReturnDeliveryResult(orderId, returnSlipNumber, registered, message);
    }
}
