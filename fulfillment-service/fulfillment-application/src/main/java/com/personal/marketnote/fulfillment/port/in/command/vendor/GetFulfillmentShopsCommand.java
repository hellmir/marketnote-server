package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentShopsCommand(
        String customerCode,
        String accessToken
) {
    public static GetFulfillmentShopsCommand of(String customerCode, String accessToken) {
        return new GetFulfillmentShopsCommand(customerCode, accessToken);
    }
}
