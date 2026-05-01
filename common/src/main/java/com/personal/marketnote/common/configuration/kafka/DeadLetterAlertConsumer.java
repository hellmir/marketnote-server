package com.personal.marketnote.common.configuration.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
public class DeadLetterAlertConsumer {

    private final DltSlackNotifier dltSlackNotifier;
    private final DltMetricsCollector dltMetricsCollector;

    @KafkaListener(topicPattern = ".*\\.dlt", groupId = "dlt-alert", containerFactory = "dltKafkaListenerContainerFactory")
    public void handleDeadLetterMessage(
            ConsumerRecord<String, Object> record,
            Acknowledgment acknowledgment
    ) {
        try {
            String originalTopic = DltHeaderExtractor.extractOriginalTopic(record);
            String errorFqcn = DltHeaderExtractor.extractExceptionFqcn(record);
            String errorMessage = DltHeaderExtractor.extractExceptionMessage(record);

            log.error("DLT 메시지 도착. originalTopic={}, partition={}, offset={}, key={}, error={}: {}",
                    originalTopic, record.partition(), record.offset(), record.key(), errorFqcn, errorMessage);

            dltSlackNotifier.notify(originalTopic, record, errorFqcn, errorMessage);
            dltMetricsCollector.incrementDltMessageCount(originalTopic);
        } catch (Exception e) {
            log.error("DLT 알림 처리 실패. topic={}, key={}", record.topic(), record.key(), e);
        }
        acknowledgment.acknowledge();
    }
}
