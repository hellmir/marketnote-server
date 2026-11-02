package com.personal.marketnote.fulfillment.port.out.notification;

import java.time.LocalDateTime;

public interface SendDeliveryFailureSlackAlertPort {
    void sendDeliveryFailureAlert(Long orderId, String trackingNumber, String carrierCode, LocalDateTime occurredAt);
}
