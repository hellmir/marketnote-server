package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.InvalidFulfillmentSyncCommandException;
import com.personal.marketnote.commerce.exception.InventoryProductNotFoundException;
import com.personal.marketnote.commerce.port.in.command.inventory.SyncFulfillmentVendorInventoryCommand;
import com.personal.marketnote.commerce.port.in.command.inventory.SyncFulfillmentVendorInventoryItemCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.SyncFulfillmentVendorInventoryUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.FulfillmentInventorySyncedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentInventorySyncedConsumer 테스트")
class FulfillmentInventorySyncedConsumerTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-09-03T00:00:00Z"), ZoneId.of("Asia/Seoul")
    );

    @InjectMocks
    private FulfillmentInventorySyncedConsumer consumer;

    @Mock
    private SyncFulfillmentVendorInventoryUseCase syncFulfillmentVendorInventoryUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private EventEnvelope<FulfillmentInventorySyncedEvent> createEnvelope(FulfillmentInventorySyncedEvent payload) {
        return EventEnvelope.of(
                KafkaTopicConstants.FULFILLMENT_INVENTORY_SYNCED,
                "fulfillment-service",
                payload,
                FIXED_CLOCK
        );
    }

    @Nested
    @DisplayName("이벤트 수신 성공")
    class HandleSuccess {

        @Test
        @DisplayName("유효한 이벤트를 수신하면 재고 동기화를 수행한다")
        void shouldSyncInventoriesWhenValidEventReceived() {
            // given
            FulfillmentInventorySyncedEvent payload = new FulfillmentInventorySyncedEvent(
                    List.of(
                            new FulfillmentInventorySyncedEvent.InventoryItem(1L, null, 100),
                            new FulfillmentInventorySyncedEvent.InventoryItem(2L, null, 50)
                    )
            );
            EventEnvelope<FulfillmentInventorySyncedEvent> envelope = createEnvelope(payload);

            ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                    KafkaTopicConstants.FULFILLMENT_INVENTORY_SYNCED, 0, 0,
                    "fulfillment-inventory-sync", envelope
            );

            // when
            consumer.handleFulfillmentInventorySyncedEvent(record, acknowledgment);

            // then
            verify(syncFulfillmentVendorInventoryUseCase).syncInventories(
                    any(SyncFulfillmentVendorInventoryCommand.class)
            );
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("이벤트 수신 - productKey 전달")
    class HandleProductKey {

        @Test
        @DisplayName("InventoryItem에 productKey가 있으면 Command에 productKey가 전달된다")
        void shouldForwardProductKeyToCommandWhenPresent() {
            // given
            UUID productKey = UUID.randomUUID();
            FulfillmentInventorySyncedEvent payload = new FulfillmentInventorySyncedEvent(
                    List.of(new FulfillmentInventorySyncedEvent.InventoryItem(1L, productKey, 100))
            );
            EventEnvelope<FulfillmentInventorySyncedEvent> envelope = createEnvelope(payload);

            ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                    KafkaTopicConstants.FULFILLMENT_INVENTORY_SYNCED, 0, 0,
                    "fulfillment-inventory-sync", envelope
            );

            // when
            consumer.handleFulfillmentInventorySyncedEvent(record, acknowledgment);

            // then
            ArgumentCaptor<SyncFulfillmentVendorInventoryCommand> captor =
                    ArgumentCaptor.forClass(SyncFulfillmentVendorInventoryCommand.class);
            verify(syncFulfillmentVendorInventoryUseCase).syncInventories(captor.capture());
            List<SyncFulfillmentVendorInventoryItemCommand> items = captor.getValue().inventories();
            assertThat(items).hasSize(1);
            assertThat(items.get(0).productId()).isEqualTo(1L);
            assertThat(items.get(0).productKey()).isEqualTo(productKey);
            assertThat(items.get(0).stock()).isEqualTo(100);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("InventoryItem에 productKey가 없으면 Command의 productKey는 null이다")
        void shouldPassNullProductKeyWhenAbsent() {
            // given
            FulfillmentInventorySyncedEvent payload = new FulfillmentInventorySyncedEvent(
                    List.of(new FulfillmentInventorySyncedEvent.InventoryItem(1L, null, 100))
            );
            EventEnvelope<FulfillmentInventorySyncedEvent> envelope = createEnvelope(payload);

            ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                    KafkaTopicConstants.FULFILLMENT_INVENTORY_SYNCED, 0, 0,
                    "fulfillment-inventory-sync", envelope
            );

            // when
            consumer.handleFulfillmentInventorySyncedEvent(record, acknowledgment);

            // then
            ArgumentCaptor<SyncFulfillmentVendorInventoryCommand> captor =
                    ArgumentCaptor.forClass(SyncFulfillmentVendorInventoryCommand.class);
            verify(syncFulfillmentVendorInventoryUseCase).syncInventories(captor.capture());
            List<SyncFulfillmentVendorInventoryItemCommand> items = captor.getValue().inventories();
            assertThat(items).hasSize(1);
            assertThat(items.get(0).productKey()).isNull();
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("이벤트 수신 - 유효하지 않은 envelope")
    class HandleInvalidEnvelope {

        @Test
        @DisplayName("envelope이 null이면 acknowledge만 수행한다")
        void shouldAcknowledgeWhenEnvelopeIsNull() {
            // given
            ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                    KafkaTopicConstants.FULFILLMENT_INVENTORY_SYNCED, 0, 0,
                    "key", null
            );

            // when
            consumer.handleFulfillmentInventorySyncedEvent(record, acknowledgment);

            // then
            verifyNoInteractions(syncFulfillmentVendorInventoryUseCase);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("이벤트 타입이 불일치하면 acknowledge만 수행한다")
        void shouldAcknowledgeWhenEventTypeMismatch() {
            // given
            EventEnvelope<FulfillmentInventorySyncedEvent> envelope = EventEnvelope.of(
                    "wrong.event.type",
                    "fulfillment-service",
                    new FulfillmentInventorySyncedEvent(List.of()),
                    FIXED_CLOCK
            );

            ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                    KafkaTopicConstants.FULFILLMENT_INVENTORY_SYNCED, 0, 0,
                    "key", envelope
            );

            // when
            consumer.handleFulfillmentInventorySyncedEvent(record, acknowledgment);

            // then
            verifyNoInteractions(syncFulfillmentVendorInventoryUseCase);
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("이벤트 수신 - 비즈니스 예외")
    class HandleBusinessException {

        @Test
        @DisplayName("InventoryProductNotFoundException 발생 시 acknowledge하고 재시도하지 않는다")
        void shouldAcknowledgeWhenInventoryProductNotFound() {
            // given
            FulfillmentInventorySyncedEvent payload = new FulfillmentInventorySyncedEvent(
                    List.of(new FulfillmentInventorySyncedEvent.InventoryItem(999L, null, 100))
            );
            EventEnvelope<FulfillmentInventorySyncedEvent> envelope = createEnvelope(payload);

            ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                    KafkaTopicConstants.FULFILLMENT_INVENTORY_SYNCED, 0, 0,
                    "key", envelope
            );

            doThrow(new InventoryProductNotFoundException(999L))
                    .when(syncFulfillmentVendorInventoryUseCase).syncInventories(any());

            // when
            consumer.handleFulfillmentInventorySyncedEvent(record, acknowledgment);

            // then
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("InvalidFulfillmentSyncCommandException 발생 시 acknowledge하고 재시도하지 않는다")
        void shouldAcknowledgeWhenInvalidSyncCommand() {
            // given
            FulfillmentInventorySyncedEvent payload = new FulfillmentInventorySyncedEvent(List.of());
            EventEnvelope<FulfillmentInventorySyncedEvent> envelope = createEnvelope(payload);

            ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                    KafkaTopicConstants.FULFILLMENT_INVENTORY_SYNCED, 0, 0,
                    "key", envelope
            );

            doThrow(new InvalidFulfillmentSyncCommandException())
                    .when(syncFulfillmentVendorInventoryUseCase).syncInventories(any());

            // when
            consumer.handleFulfillmentInventorySyncedEvent(record, acknowledgment);

            // then
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("이벤트 수신 - 시스템 예외")
    class HandleSystemException {

        @Test
        @DisplayName("예상치 못한 예외 발생 시 예외를 다시 던진다")
        void shouldRethrowUnexpectedException() {
            // given
            FulfillmentInventorySyncedEvent payload = new FulfillmentInventorySyncedEvent(
                    List.of(new FulfillmentInventorySyncedEvent.InventoryItem(1L, null, 100))
            );
            EventEnvelope<FulfillmentInventorySyncedEvent> envelope = createEnvelope(payload);

            ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                    KafkaTopicConstants.FULFILLMENT_INVENTORY_SYNCED, 0, 0,
                    "key", envelope
            );

            doThrow(new RuntimeException("DB 연결 실패"))
                    .when(syncFulfillmentVendorInventoryUseCase).syncInventories(any());

            // when & then
            assertThatThrownBy(() -> consumer.handleFulfillmentInventorySyncedEvent(record, acknowledgment))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("DB 연결 실패");
        }
    }
}
