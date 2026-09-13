package com.personal.marketnote.fulfillment.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.FulfillmentInventorySyncedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.fulfillment.exception.FulfillmentInventorySyncedEventSerializationException;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentInventorySyncedEventOutboxAdapter 테스트")
class FulfillmentInventorySyncedEventOutboxAdapterTest {

    @InjectMocks
    private FulfillmentInventorySyncedEventOutboxAdapter adapter;

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
        @DisplayName("이벤트를 직렬화하여 OutboxEvent로 저장한다")
        void shouldSerializeAndSaveOutboxEvent() throws JsonProcessingException {
            // given
            Clock fixedClock = Clock.fixed(Instant.parse("2026-09-03T00:00:00Z"), ZoneId.of("Asia/Seoul"));
            when(clock.instant()).thenReturn(fixedClock.instant());
            when(clock.getZone()).thenReturn(fixedClock.getZone());

            FulfillmentInventorySyncedEvent event = new FulfillmentInventorySyncedEvent(
                    List.of(
                            new FulfillmentInventorySyncedEvent.InventoryItem(1L, 100),
                            new FulfillmentInventorySyncedEvent.InventoryItem(2L, 50)
                    )
            );
            when(objectMapper.writeValueAsString(event)).thenReturn("{\"inventories\":[]}");

            // when
            adapter.publish(event);

            // then
            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(saveOutboxEventPort).save(captor.capture());

            OutboxEvent savedEvent = captor.getValue();
            assertThat(savedEvent.getTopic()).isEqualTo(KafkaTopicConstants.FULFILLMENT_INVENTORY_SYNCED);
            assertThat(savedEvent.getPartitionKey()).isEqualTo("fulfillment-inventory-sync");
            assertThat(savedEvent.getEventType()).isEqualTo("FulfillmentInventorySyncedEvent");
            assertThat(savedEvent.getSource()).isEqualTo("fulfillment-service");
            assertThat(savedEvent.getPayload()).isEqualTo("{\"inventories\":[]}");
        }
    }

    @Nested
    @DisplayName("publish 실패")
    class PublishFailure {

        @Test
        @DisplayName("직렬화 실패 시 FulfillmentInventorySyncedEventSerializationException이 발생한다")
        void shouldThrowSerializationExceptionWhenSerializationFails() throws JsonProcessingException {
            // given
            FulfillmentInventorySyncedEvent event = new FulfillmentInventorySyncedEvent(List.of());
            when(objectMapper.writeValueAsString(event))
                    .thenThrow(new JsonProcessingException("직렬화 실패") {});

            // when & then
            assertThatThrownBy(() -> adapter.publish(event))
                    .isInstanceOf(FulfillmentInventorySyncedEventSerializationException.class);
            verifyNoInteractions(saveOutboxEventPort);
        }
    }
}
