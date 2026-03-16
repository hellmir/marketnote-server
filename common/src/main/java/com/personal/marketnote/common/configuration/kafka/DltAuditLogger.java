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
}
