package com.personal.marketnote.common.outbox.adapter;

import com.personal.marketnote.common.utility.FormatValidator;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "outbox", name = "enabled", havingValue = "true")
public class OutboxMetricsCollector {

    private final MeterRegistry meterRegistry;

    public OutboxMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementFailedCount(String topic) {
        if (FormatValidator.hasNoValue(meterRegistry)) {
            return;
        }
        Counter.builder("outbox.events.failed.total")
                .tag("topic", topic)
                .description("Outbox 이벤트 최대 재시도 초과로 FAILED 전환된 건수")
                .register(meterRegistry)
                .increment();
    }

    public void incrementResolvedCount(String topic, String action) {
        if (FormatValidator.hasNoValue(meterRegistry)) {
            return;
        }
        Counter.builder("outbox.events.resolved.total")
                .tag("topic", topic)
                .tag("action", action)
                .description("Outbox FAILED 이벤트 해결 건수")
                .register(meterRegistry)
                .increment();
    }
}
