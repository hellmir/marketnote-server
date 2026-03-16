package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.utility.FormatValidator;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
public class DltMetricsCollector {

    private final MeterRegistry meterRegistry;

    public DltMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementDltMessageCount(String originalTopic) {
        if (FormatValidator.hasNoValue(meterRegistry)) {
            return;
        }
        Counter.builder("kafka.dlt.messages.total")
                .tag("original_topic", originalTopic)
                .description("DLT에 적재된 메시지 총 건수")
                .register(meterRegistry)
                .increment();
    }

    public void incrementDltReprocessCount(String originalTopic, String outcome) {
        if (FormatValidator.hasNoValue(meterRegistry)) {
            return;
        }
        Counter.builder("kafka.dlt.reprocess.total")
                .tag("original_topic", originalTopic)
                .tag("outcome", outcome)
                .description("DLT 메시지 재처리 건수")
                .register(meterRegistry)
                .increment();
    }
}
