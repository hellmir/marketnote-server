package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFasstoDeliveryOutOrdGoodsByOrdNoCommand(
        String customerCode,
        String accessToken,
        String startDate,
        String endDate,
        String ordNo
) {
    public static GetFasstoDeliveryOutOrdGoodsByOrdNoCommand of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String ordNo
    ) {
        return new GetFasstoDeliveryOutOrdGoodsByOrdNoCommand(customerCode, accessToken, startDate, endDate, ordNo);
    }
}
