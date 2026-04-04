package com.personal.marketnote.common.outbox.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "outbox", name = "enabled", havingValue = "true")
public class OutboxAuditLogger {

    public void logFailed(String topic, String eventId, int retryCount, String errorMessage) {
        log.error("[OUTBOX-AUDIT] action=FAILED, topic={}, eventId={}, retryCount={}, error={}",
                topic, eventId, retryCount, errorMessage);
    }

    public void logResolve(String topic, String eventId, String action, String reason) {
        log.info("[OUTBOX-AUDIT] action=RESOLVE, topic={}, eventId={}, resolution={}, reason={}",
                topic, eventId, action, reason);
    }

    public void logResolveError(String topic, String eventId, String action, Exception ex) {
        log.error("[OUTBOX-AUDIT] action=RESOLVE_ERROR, topic={}, eventId={}, resolution={}, error={}",
                topic, eventId, action, ex.getMessage(), ex);
    }
}
