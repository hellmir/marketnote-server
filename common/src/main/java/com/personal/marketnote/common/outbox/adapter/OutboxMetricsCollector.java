package com.personal.marketnote.common.outbox.adapter;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.metrics.CounterRegistryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "outbox", name = "enabled", havingValue = "true")
public class OutboxMetricsCollector {

    private final CounterRegistryHelper counterRegistryHelper;

    public OutboxMetricsCollector(@Autowired(required = false) @Nullable CounterRegistryHelper counterRegistryHelper) {
        this.counterRegistryHelper = counterRegistryHelper;
    }

    public void incrementFailedCount(String topic) {
        if (FormatValidator.hasNoValue(counterRegistryHelper)) {
            return;
        }
        counterRegistryHelper.increment(
                "outbox.events.failed.total",
                "Outbox 이벤트 최대 재시도 초과로 FAILED 전환된 건수",
                "topic", topic
        );
    }

    public void incrementResolvedCount(String topic, String action) {
        if (FormatValidator.hasNoValue(counterRegistryHelper)) {
            return;
        }
        counterRegistryHelper.increment(
                "outbox.events.resolved.total",
                "Outbox FAILED 이벤트 해결 건수",
                "topic", topic,
                "action", action
        );
    }
}
