package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFasstoDeliveryGoodDetailCommand(
        String customerCode,
        String accessToken,
        String startDate,
        String endDate,
        String ordNo
) {
    public static GetFasstoDeliveryGoodDetailCommand of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String ordNo
    ) {
        return new GetFasstoDeliveryGoodDetailCommand(customerCode, accessToken, startDate, endDate, ordNo);
    }
}
