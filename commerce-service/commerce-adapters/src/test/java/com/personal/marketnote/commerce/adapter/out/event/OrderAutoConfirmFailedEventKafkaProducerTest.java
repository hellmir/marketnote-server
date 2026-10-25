package com.personal.marketnote.commerce.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.out.event.exception.OrderEventSerializationException;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderAutoConfirmFailedEventKafkaProducer 테스트")
class OrderAutoConfirmFailedEventKafkaProducerTest {

    @InjectMocks
    private OrderAutoConfirmFailedEventKafkaProducer producer;

    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-10-24T10:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Test
    @DisplayName("자동 확정 실패 이벤트 발행 시 올바른 토픽과 파티션 키로 Outbox에 저장된다")
    void publishSavesOutboxEventWithCorrectTopicAndPartitionKey() {
        // given
        LocalDateTime failedAt = LocalDateTime.of(2026, 4, 19, 19, 0);

        // when
        producer.publish(100L, 50L, "DB 오류", failedAt);

        // then
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(captor.capture());

        OutboxEvent outboxEvent = captor.getValue();
        assertThat(outboxEvent.getTopic()).isEqualTo(KafkaTopicConstants.ORDER_AUTO_CONFIRM_FAILED);
        assertThat(outboxEvent.getPartitionKey()).isEqualTo("100");
        assertThat(outboxEvent.getSource()).isEqualTo("commerce-service");
        assertThat(outboxEvent.getEventId()).isNotBlank();
    }

    @Test
    @DisplayName("Outbox 페이로드는 EventEnvelope 형태로 직렬화되며 OrderAutoConfirmFailedEvent payload를 포함한다")
    void publishWrapsPayloadInEventEnvelope() throws Exception {
        // given
        LocalDateTime failedAt = LocalDateTime.of(2026, 4, 19, 19, 0);

        // when
        producer.publish(200L, 77L, "timeout", failedAt);

        // then
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(captor.capture());

        JsonNode parsed = objectMapper.readTree(captor.getValue().getPayload());
        assertThat(parsed.get("eventType").asText()).isEqualTo(KafkaTopicConstants.ORDER_AUTO_CONFIRM_FAILED);
        assertThat(parsed.get("source").asText()).isEqualTo("commerce-service");
        assertThat(parsed.get("payload").get("orderId").asLong()).isEqualTo(200L);
        assertThat(parsed.get("payload").get("buyerId").asLong()).isEqualTo(77L);
        assertThat(parsed.get("payload").get("reason").asText()).isEqualTo("timeout");
        assertThat(parsed.get("payload").get("failedAt")).isNotNull();
    }

    @Test
    @DisplayName("직렬화 실패 시 OrderEventSerializationException을 던진다")
    void throwsOnSerializationFailure() throws Exception {
        // given
        when(objectMapper.writeValueAsString(any(EventEnvelope.class)))
                .thenThrow(new JsonProcessingException("test failure") {
                });

        // when & then
        assertThatThrownBy(() -> producer.publish(300L, 99L, "오류", LocalDateTime.now()))
                .isInstanceOf(OrderEventSerializationException.class);
        verifyNoInteractions(saveOutboxEventPort);
    }
}
