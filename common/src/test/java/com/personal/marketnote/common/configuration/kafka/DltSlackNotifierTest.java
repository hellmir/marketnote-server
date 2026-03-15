package com.personal.marketnote.common.configuration.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DltSlackNotifier 테스트")
class DltSlackNotifierTest {
    @InjectMocks
    private DltSlackNotifier dltSlackNotifier;

    @Mock
    private KafkaSlackProperties kafkaSlackProperties;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("webhook URL이 null이면 Slack 호출을 건너뛴다")
    void notify_nullWebhookUrl_skipsSlackCall() {
        // given
        when(kafkaSlackProperties.getWebhookUrl()).thenReturn(null);
        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
                "some.topic.dlt", 0, 0L, "key-123", "value"
        );

        // when — 예외 없이 정상 반환
        dltSlackNotifier.notify("some.topic", record, "Error", "msg");

        // then
        verify(kafkaSlackProperties).getWebhookUrl();
    }

    @Test
    @DisplayName("webhook URL이 빈 문자열이면 Slack 호출을 건너뛴다")
    void notify_emptyWebhookUrl_skipsSlackCall() {
        // given
        when(kafkaSlackProperties.getWebhookUrl()).thenReturn("");
        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
                "some.topic.dlt", 0, 0L, "key-123", "value"
        );

        // when — 예외 없이 정상 반환
        dltSlackNotifier.notify("some.topic", record, "Error", "msg");

        // then
        verify(kafkaSlackProperties).getWebhookUrl();
    }
}
