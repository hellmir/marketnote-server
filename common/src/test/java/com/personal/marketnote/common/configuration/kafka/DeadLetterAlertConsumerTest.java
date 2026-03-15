package com.personal.marketnote.common.configuration.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeadLetterAlertConsumer 테스트")
class DeadLetterAlertConsumerTest {
    @InjectMocks
    private DeadLetterAlertConsumer consumer;

    @Mock
    private DltSlackNotifier dltSlackNotifier;

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, Object> buildDltRecord(
            String originalTopic,
            String errorFqcn,
            String errorMessage
    ) {
        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
                originalTopic + ".dlt", 0, 0L, "key-123", "value"
        );
        record.headers()
                .add("kafka_dlt-original-topic", originalTopic.getBytes(StandardCharsets.UTF_8))
                .add("kafka_dlt-exception-fqcn", errorFqcn.getBytes(StandardCharsets.UTF_8))
                .add("kafka_dlt-exception-message", errorMessage.getBytes(StandardCharsets.UTF_8));
        return record;
    }

    @Test
    @DisplayName("DLT 메시지 수신 시 원본 토픽 정보를 추출하여 Slack 알림을 전송하고 acknowledge한다")
    void handleDeadLetterMessage_extractsHeadersAndNotifiesSlackAndAcknowledges() {
        // given
        String originalTopic = "commerce.order.payment-completed";
        ConsumerRecord<String, Object> record = buildDltRecord(
                originalTopic, "java.lang.RuntimeException", "DB 연결 오류"
        );

        // when
        consumer.handleDeadLetterMessage(record, acknowledgment);

        // then
        verify(dltSlackNotifier).notify(
                eq(originalTopic),
                eq(record),
                eq("java.lang.RuntimeException"),
                eq("DB 연결 오류")
        );
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("DLT 메시지 처리 중 예외가 발생해도 acknowledge한다")
    void handleDeadLetterMessage_exceptionDuringProcessing_stillAcknowledges() {
        // given
        ConsumerRecord<String, Object> record = buildDltRecord("some.topic", "Error", "msg");
        doThrow(new RuntimeException("Slack API 오류"))
                .when(dltSlackNotifier).notify(anyString(), any(), anyString(), anyString());

        // when
        consumer.handleDeadLetterMessage(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("DLT 헤더가 없는 메시지도 UNKNOWN으로 처리하고 acknowledge한다")
    void handleDeadLetterMessage_noHeaders_handlesGracefullyAndAcknowledges() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
                "some.topic.dlt", 0, 0L, "key-123", "value"
        );

        // when
        consumer.handleDeadLetterMessage(record, acknowledgment);

        // then
        verify(dltSlackNotifier).notify(
                eq("UNKNOWN"),
                eq(record),
                eq("UNKNOWN"),
                eq("UNKNOWN")
        );
        verify(acknowledgment).acknowledge();
    }
}
