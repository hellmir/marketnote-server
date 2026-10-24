package com.personal.marketnote.fulfillment.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.fulfillment.exception.OrderCancelFailedEventSerializationException;
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
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCancelFailedEventOutboxAdapter 테스트")
class OrderCancelFailedEventOutboxAdapterTest {

    @InjectMocks
    private OrderCancelFailedEventOutboxAdapter adapter;

    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-10-24T10:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Test
    @DisplayName("OrderCancelFailedEvent를 올바른 토픽과 파티션 키로 Outbox에 저장한다")
    void publishSavesOutboxEventWithCorrectTopicAndPartitionKey() {
        // when
        adapter.publish(100L, 50L);

        // then
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(captor.capture());

        OutboxEvent outboxEvent = captor.getValue();
        assertThat(outboxEvent.getTopic()).isEqualTo(KafkaTopicConstants.ORDER_CANCEL_FAILED);
        assertThat(outboxEvent.getPartitionKey()).isEqualTo("100");
        assertThat(outboxEvent.getSource()).isEqualTo("fulfillment-service");
        assertThat(outboxEvent.getEventId()).isNotBlank();
    }

    @Test
    @DisplayName("Outbox 페이로드는 EventEnvelope 형태로 직렬화되며 OrderCancelFailedEvent payload를 포함한다")
    void publishWrapsPayloadInEventEnvelope() throws Exception {
        // when
        adapter.publish(200L, 77L);

        // then
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(captor.capture());

        JsonNode parsed = objectMapper.readTree(captor.getValue().getPayload());
        assertThat(parsed.get("eventType").asText()).isEqualTo(KafkaTopicConstants.ORDER_CANCEL_FAILED);
        assertThat(parsed.get("source").asText()).isEqualTo("fulfillment-service");
        assertThat(parsed.get("payload").get("orderId").asLong()).isEqualTo(200L);
        assertThat(parsed.get("payload").get("buyerId").asLong()).isEqualTo(77L);
    }

    @Test
    @DisplayName("직렬화 실패 시 OrderCancelFailedEventSerializationException을 던진다")
    void throwsOnSerializationFailure() throws Exception {
        // given
        when(objectMapper.writeValueAsString(any(EventEnvelope.class)))
                .thenThrow(new JsonProcessingException("test failure") {
                });

        // when & then
        assertThatThrownBy(() -> adapter.publish(300L, 99L))
                .isInstanceOf(OrderCancelFailedEventSerializationException.class)
                .hasMessageContaining("orderId=300");
        verifyNoInteractions(saveOutboxEventPort);
    }
}
