package com.personal.marketnote.fulfillment.port.out.scheduler;

public record ScheduleFulfillmentWarehousingPollingCommand(
        String customerCode,
        String ordNo,
        String ordDt
) {
    public static ScheduleFulfillmentWarehousingPollingCommand of(
            String customerCode,
            String ordNo,
            String ordDt
    ) {
        return new ScheduleFulfillmentWarehousingPollingCommand(customerCode, ordNo, ordDt);
    }
}
