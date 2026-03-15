package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
public class DeadLetterAlertConsumer {
    private static final String HEADER_ORIGINAL_TOPIC = "kafka_dlt-original-topic";
    private static final String HEADER_EXCEPTION_FQCN = "kafka_dlt-exception-fqcn";
    private static final String HEADER_EXCEPTION_MESSAGE = "kafka_dlt-exception-message";

    private final DltSlackNotifier dltSlackNotifier;

    @KafkaListener(topicPattern = ".*\\.dlt", groupId = "dlt-alert")
    public void handleDeadLetterMessage(
            ConsumerRecord<String, Object> record,
            Acknowledgment acknowledgment
    ) {
        try {
            String originalTopic = extractHeader(record, HEADER_ORIGINAL_TOPIC);
            String errorFqcn = extractHeader(record, HEADER_EXCEPTION_FQCN);
            String errorMessage = extractHeader(record, HEADER_EXCEPTION_MESSAGE);

            log.error("DLT 메시지 도착. originalTopic={}, partition={}, offset={}, key={}, error={}: {}",
                    originalTopic, record.partition(), record.offset(), record.key(), errorFqcn, errorMessage);

            dltSlackNotifier.notify(originalTopic, record, errorFqcn, errorMessage);
        } catch (Exception e) {
            log.error("DLT 알림 처리 실패. topic={}, key={}", record.topic(), record.key(), e);
        }
        acknowledgment.acknowledge();
    }

    private String extractHeader(ConsumerRecord<String, Object> record, String headerKey) {
        Header header = record.headers().lastHeader(headerKey);
        if (FormatValidator.hasNoValue(header)) {
            return "UNKNOWN";
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
