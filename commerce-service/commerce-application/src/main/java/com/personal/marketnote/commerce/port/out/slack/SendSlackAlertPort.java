package com.personal.marketnote.commerce.port.out.slack;

import java.time.LocalDateTime;

public interface SendSlackAlertPort {
    void sendInspectionFailedOrHoldAlert(Long orderId, String inspectionStatus, LocalDateTime inspectedAt);
}
