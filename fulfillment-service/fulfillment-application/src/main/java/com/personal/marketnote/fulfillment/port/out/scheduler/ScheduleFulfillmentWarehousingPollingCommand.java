package com.personal.marketnote.fulfillment.port.out.scheduler;

public record ScheduleFulfillmentWarehousingPollingCommand(
        String customerCode,
        String orderNumber,
        String orderDate
) {
    public static ScheduleFulfillmentWarehousingPollingCommand of(
            String customerCode,
            String orderNumber,
            String orderDate
    ) {
        return new ScheduleFulfillmentWarehousingPollingCommand(customerCode, orderNumber, orderDate);
    }
}
