package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.metrics.CounterRegistryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
public class DltMetricsCollector {

    private final CounterRegistryHelper counterRegistryHelper;

    public DltMetricsCollector(@Autowired(required = false) @Nullable CounterRegistryHelper counterRegistryHelper) {
        this.counterRegistryHelper = counterRegistryHelper;
    }

    public void incrementDltMessageCount(String originalTopic) {
        if (FormatValidator.hasNoValue(counterRegistryHelper)) {
            return;
        }
        counterRegistryHelper.increment(
                "kafka.dlt.messages.total",
                "DLT에 적재된 메시지 총 건수",
                "original_topic", originalTopic
        );
    }

    public void incrementDltReprocessCount(String originalTopic, String outcome) {
        if (FormatValidator.hasNoValue(counterRegistryHelper)) {
            return;
        }
        counterRegistryHelper.increment(
                "kafka.dlt.reprocess.total",
                "DLT 메시지 재처리 건수",
                "original_topic", originalTopic,
                "outcome", outcome
        );
    }

    public void incrementDltResolveCount(String originalTopic, String action) {
        if (FormatValidator.hasNoValue(counterRegistryHelper)) {
            return;
        }
        counterRegistryHelper.increment(
                "kafka.dlt.resolve.total",
                "DLT 메시지 해결 건수",
                "original_topic", originalTopic,
                "action", action
        );
    }
}
