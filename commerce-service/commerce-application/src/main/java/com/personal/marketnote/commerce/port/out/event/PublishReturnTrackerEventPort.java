package com.personal.marketnote.commerce.port.out.event;

public interface PublishReturnTrackerEventPort {

    void publishReturnInspectionCompletedEvent(Long orderId);
}
