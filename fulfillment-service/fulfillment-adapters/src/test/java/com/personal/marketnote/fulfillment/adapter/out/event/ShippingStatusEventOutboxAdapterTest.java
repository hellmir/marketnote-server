package com.personal.marketnote.fulfillment.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ShippingStatusChangedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.fulfillment.exception.ShippingStatusEventSerializationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShippingStatusEventOutboxAdapter 테스트")
class ShippingStatusEventOutboxAdapterTest {

    @InjectMocks
    private ShippingStatusEventOutboxAdapter adapter;

    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Clock clock;

    @Nested
    @DisplayName("publish 성공")
    class PublishSuccess {

        @Test
        @DisplayName("이벤트를 EventEnvelope으로 감싸 직렬화하여 OutboxEvent로 저장한다")
        void shouldSerializeAndSaveOutboxEvent() throws JsonProcessingException {
            // given
            Clock fixedClock = Clock.fixed(Instant.parse("2026-06-03T10:00:00Z"), ZoneId.of("Asia/Seoul"));
            when(clock.instant()).thenReturn(fixedClock.instant());
            when(clock.getZone()).thenReturn(fixedClock.getZone());

            ShippingStatusChangedEvent event = new ShippingStatusChangedEvent(
                100L,
                1L,
                "SHIPPING",
                "INV001",
                "CJ",
                LocalDateTime.of(2026, 4, 9, 10, 0),
                null,
                null
            );
            ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
            when(objectMapper.writeValueAsString(envelopeCaptor.capture()))
                .thenReturn("{\"eventType\":\"shipping.status.changed\"}");

            // when
            adapter.publish(event);

            // then
            EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
            assertThat(capturedEnvelope.eventType()).isEqualTo(KafkaTopicConstants.SHIPPING_STATUS_CHANGED);
            assertThat(capturedEnvelope.source()).isEqualTo("fulfillment-service");
            assertThat(capturedEnvelope.eventId()).isNotBlank();
            assertThat(capturedEnvelope.payload()).isEqualTo(event);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(saveOutboxEventPort).save(captor.capture());

            OutboxEvent savedEvent = captor.getValue();
            assertThat(savedEvent.getTopic()).isEqualTo(KafkaTopicConstants.SHIPPING_STATUS_CHANGED);
            assertThat(savedEvent.getPartitionKey()).isEqualTo("100");
            assertThat(savedEvent.getEventType()).isEqualTo("ShippingStatusChangedEvent");
            assertThat(savedEvent.getSource()).isEqualTo("fulfillment-service");
            assertThat(savedEvent.getEventId()).isNotBlank();
            assertThat(savedEvent.getEventId()).isEqualTo(capturedEnvelope.eventId());
            assertThat(savedEvent.getPayload()).isEqualTo("{\"eventType\":\"shipping.status.changed\"}");
        }
    }

    @Nested
    @DisplayName("publish 실패")
    class PublishFailure {

        @Test
        @DisplayName("직렬화 실패 시 ShippingStatusEventSerializationException이 발생한다")
        void shouldThrowSerializationExceptionWhenSerializationFails() throws JsonProcessingException {
            // given
            Clock fixedClock = Clock.fixed(Instant.parse("2026-06-03T14:00:00Z"), ZoneId.of("Asia/Seoul"));
            when(clock.instant()).thenReturn(fixedClock.instant());
            when(clock.getZone()).thenReturn(fixedClock.getZone());

            ShippingStatusChangedEvent event = new ShippingStatusChangedEvent(
                300L,
                3L,
                "DELIVERY_FAILED",
                "INV003",
                "CJ",
                LocalDateTime.of(2026, 4, 9, 14, 0),
                null,
                null
            );
            when(objectMapper.writeValueAsString(any(EventEnvelope.class)))
                .thenThrow(new JsonProcessingException("test failure") {
                });

            // when & then
            assertThatThrownBy(() -> adapter.publish(event))
                .isInstanceOf(ShippingStatusEventSerializationException.class)
                .hasMessageContaining("ERR_SHIPPING_STATUS_EVENT_01")
                .hasMessageContaining("배송 상태 변경 이벤트")
                .hasMessageContaining("partitionKey=300")
                .hasMessageContaining("eventId=");
            verifyNoInteractions(saveOutboxEventPort);
        }
    }
}
