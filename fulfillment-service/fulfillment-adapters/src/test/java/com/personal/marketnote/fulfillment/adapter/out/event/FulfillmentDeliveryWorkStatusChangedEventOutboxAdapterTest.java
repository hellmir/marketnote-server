package com.personal.marketnote.fulfillment.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.FulfillmentDeliveryWorkStatusChangedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryWorkStatusChangedEventSerializationException;
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
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentDeliveryWorkStatusChangedEventOutboxAdapter 테스트")
class FulfillmentDeliveryWorkStatusChangedEventOutboxAdapterTest {

    @InjectMocks
    private FulfillmentDeliveryWorkStatusChangedEventOutboxAdapter adapter;

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
            Clock fixedClock = Clock.fixed(Instant.parse("2026-09-28T00:00:00Z"), ZoneId.of("Asia/Seoul"));
            when(clock.instant()).thenReturn(fixedClock.instant());
            when(clock.getZone()).thenReturn(fixedClock.getZone());

            FulfillmentDeliveryWorkStatusChangedEvent event = new FulfillmentDeliveryWorkStatusChangedEvent(
                    100L, "DELIVERED"
            );
            ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
            when(objectMapper.writeValueAsString(envelopeCaptor.capture()))
                    .thenReturn("{\"eventType\":\"fulfillment.delivery.work.status.changed\"}");

            // when
            adapter.publish(event);

            // then
            EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
            assertThat(capturedEnvelope.eventType()).isEqualTo(KafkaTopicConstants.FULFILLMENT_DELIVERY_WORK_STATUS_CHANGED);
            assertThat(capturedEnvelope.source()).isEqualTo("fulfillment-service");
            assertThat(capturedEnvelope.eventId()).isNotBlank();
            assertThat(capturedEnvelope.payload()).isEqualTo(event);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(saveOutboxEventPort).save(captor.capture());

            OutboxEvent savedEvent = captor.getValue();
            assertThat(savedEvent.getTopic()).isEqualTo(KafkaTopicConstants.FULFILLMENT_DELIVERY_WORK_STATUS_CHANGED);
            assertThat(savedEvent.getPartitionKey()).isEqualTo("100");
            assertThat(savedEvent.getEventType()).isEqualTo("FulfillmentDeliveryWorkStatusChangedEvent");
            assertThat(savedEvent.getSource()).isEqualTo("fulfillment-service");
            assertThat(savedEvent.getPayload()).isEqualTo("{\"eventType\":\"fulfillment.delivery.work.status.changed\"}");
        }
    }

    @Nested
    @DisplayName("publish 실패")
    class PublishFailure {

        @Test
        @DisplayName("직렬화 실패 시 FulfillmentDeliveryWorkStatusChangedEventSerializationException이 발생한다")
        void shouldThrowSerializationExceptionWhenSerializationFails() throws JsonProcessingException {
            // given
            Clock fixedClock = Clock.fixed(Instant.parse("2026-09-28T00:00:00Z"), ZoneId.of("Asia/Seoul"));
            when(clock.instant()).thenReturn(fixedClock.instant());
            when(clock.getZone()).thenReturn(fixedClock.getZone());

            FulfillmentDeliveryWorkStatusChangedEvent event = new FulfillmentDeliveryWorkStatusChangedEvent(
                    100L, "DELIVERED"
            );
            when(objectMapper.writeValueAsString(any(EventEnvelope.class)))
                    .thenThrow(new JsonProcessingException("직렬화 실패") {
                    });

            // when & then
            assertThatThrownBy(() -> adapter.publish(event))
                    .isInstanceOf(FulfillmentDeliveryWorkStatusChangedEventSerializationException.class);
            verifyNoInteractions(saveOutboxEventPort);
        }
    }
}
