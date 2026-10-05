package com.personal.marketnote.fulfillment.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.FulfillmentGoodsSyncedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.fulfillment.exception.FulfillmentGoodsSyncedEventSerializationException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentGoodsSyncedEventOutboxAdapter 테스트")
class FulfillmentGoodsSyncedEventOutboxAdapterTest {

    @InjectMocks
    private FulfillmentGoodsSyncedEventOutboxAdapter adapter;

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
            Clock fixedClock = Clock.fixed(Instant.parse("2026-09-28T00:00:00Z"), ZoneId.of("Asia/Seoul"));
            when(clock.instant()).thenReturn(fixedClock.instant());
            when(clock.getZone()).thenReturn(fixedClock.getZone());

            FulfillmentGoodsSyncedEvent event = new FulfillmentGoodsSyncedEvent(
                    "CGC001", "GC001", "테스트 상품", "1", "일반", "Y",
                    null, null, "CUST001", "테스트 고객사",
                    "SUP001", "테스트 공급사", null, null, null, null,
                    "10000", "8000", "12000", null, null, null, null,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null,
                    null, null,
                    List.of()
            );
            when(objectMapper.writeValueAsString(event)).thenReturn("{\"customerGoodsCode\":\"CGC001\"}");

            // when
            adapter.publish(event);

            // then
            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(saveOutboxEventPort).save(captor.capture());

            OutboxEvent savedEvent = captor.getValue();
            assertThat(savedEvent.getTopic()).isEqualTo(KafkaTopicConstants.FULFILLMENT_GOODS_SYNCED);
            assertThat(savedEvent.getPartitionKey()).isEqualTo("CGC001");
            assertThat(savedEvent.getEventType()).isEqualTo("FulfillmentGoodsSyncedEvent");
            assertThat(savedEvent.getSource()).isEqualTo("fulfillment-service");
            assertThat(savedEvent.getPayload()).isEqualTo("{\"customerGoodsCode\":\"CGC001\"}");
        }
    }

    @Nested
    @DisplayName("publish 실패")
    class PublishFailure {

        @Test
        @DisplayName("직렬화 실패 시 FulfillmentGoodsSyncedEventSerializationException이 발생한다")
        void shouldThrowSerializationExceptionWhenSerializationFails() throws JsonProcessingException {
            // given
            FulfillmentGoodsSyncedEvent event = new FulfillmentGoodsSyncedEvent(
                    "CGC001", "GC001", "테스트 상품", "1", "일반", "Y",
                    null, null, "CUST001", "테스트 고객사",
                    "SUP001", "테스트 공급사", null, null, null, null,
                    null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null,
                    null, null,
                    List.of()
            );
            when(objectMapper.writeValueAsString(event))
                    .thenThrow(new JsonProcessingException("직렬화 실패") {
                    });

            // when & then
            assertThatThrownBy(() -> adapter.publish(event))
                    .isInstanceOf(FulfillmentGoodsSyncedEventSerializationException.class);
            verifyNoInteractions(saveOutboxEventPort);
        }
    }
}
