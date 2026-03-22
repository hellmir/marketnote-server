package com.personal.marketnote.common.configuration.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
public class DltAuditLogger {

    public void logReprocessStart(String originalTopic, String operatorInfo) {
        log.info("[DLT-AUDIT] action=REPROCESS_START, topic={}, operator={}",
                originalTopic, operatorInfo);
    }

    public void logReprocessComplete(String originalTopic, String operatorInfo, int reprocessed, int failed) {
        log.info("[DLT-AUDIT] action=REPROCESS_COMPLETE, topic={}, operator={}, reprocessed={}, failed={}",
                originalTopic, operatorInfo, reprocessed, failed);
    }

    public void logReprocessError(String originalTopic, String operatorInfo, Exception ex) {
        log.error("[DLT-AUDIT] action=REPROCESS_ERROR, topic={}, operator={}, error={}",
                originalTopic, operatorInfo, ex.getMessage(), ex);
    }

    public void logQuery(String originalTopic, int limit, String operatorInfo) {
        log.info("[DLT-AUDIT] action=QUERY, topic={}, limit={}, operator={}",
                originalTopic, limit, operatorInfo);
    }

    public void logSummaryQuery(String operatorInfo) {
        log.info("[DLT-AUDIT] action=SUMMARY_QUERY, operator={}", operatorInfo);
    }

    public void logResolve(String originalTopic, int partition, long offset,
                            String action, String reason, String operatorInfo) {
        log.info("[DLT-AUDIT] action=RESOLVE, topic={}, partition={}, offset={}, resolution={}, reason={}, operator={}",
                originalTopic, partition, offset, action, reason, operatorInfo);
    }

    public void logResolveAlreadyResolved(String originalTopic, int partition, long offset,
                                           String existingResolution, String operatorInfo) {
        log.info("[DLT-AUDIT] action=RESOLVE_ALREADY_RESOLVED, topic={}, partition={}, offset={}, existingResolution={}, operator={}",
                originalTopic, partition, offset, existingResolution, operatorInfo);
    }

    public void logResolveError(String originalTopic, int partition, long offset,
                                 String action, String operatorInfo, Exception ex) {
        log.error("[DLT-AUDIT] action=RESOLVE_ERROR, topic={}, partition={}, offset={}, resolution={}, operator={}, error={}",
                originalTopic, partition, offset, action, operatorInfo, ex.getMessage(), ex);
    }
}
