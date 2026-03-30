package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentSuppliersCommand(
        String customerCode,
        String accessToken
) {
    public static GetFulfillmentSuppliersCommand of(String customerCode, String accessToken) {
        return new GetFulfillmentSuppliersCommand(customerCode, accessToken);
    }
}
