package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.InventoryAlreadyExistsException;
import com.personal.marketnote.commerce.port.in.command.inventory.BackfillInventoryProductKeyCommand;
import com.personal.marketnote.commerce.port.in.command.inventory.RegisterInventoryCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.BackfillInventoryProductKeyUseCase;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RegisterInventoryUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
import com.personal.marketnote.common.kafka.event.ProductUpdatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductEventKafkaListenerTest {
    @InjectMocks
    private ProductEventKafkaListener productEventKafkaListener;

    @Mock
    private RegisterInventoryUseCase registerInventoryUseCase;

    @Mock
    private BackfillInventoryProductKeyUseCase backfillInventoryProductKeyUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(
            Long productId, Long pricePolicyId, Long sellerId
    ) {
        return buildRecord(productId, null, pricePolicyId, sellerId);
    }

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(
            Long productId, UUID productKey, Long pricePolicyId, Long sellerId
    ) {
        ProductRegisteredEvent event = new ProductRegisteredEvent(
                productId, productKey, pricePolicyId, sellerId, "테스트 상품", "1", "테스트 브랜드", 10000L, 8000L, 100L, 0L, null, null);
        EventEnvelope<ProductRegisteredEvent> envelope = new EventEnvelope<>(
                "test-event-id", "product.product.registered", "product-service",
                LocalDateTime.of(2026, 2, 27, 10, 0), event
        );
        return new ConsumerRecord<>("product.product.registered", 0, 0L, "key-1", envelope);
    }

    private ConsumerRecord<String, EventEnvelope<?>> buildUpdatedRecord(
            Long productId, UUID productKey
    ) {
        ProductUpdatedEvent event = new ProductUpdatedEvent(
                productId, productKey, "이름", null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null
        );
        EventEnvelope<ProductUpdatedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "product.product.updated", "product-service",
                LocalDateTime.of(2026, 2, 27, 10, 0), event
        );
        return new ConsumerRecord<>("product.product.updated", 0, 0L, "key-1", envelope);
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 재고 등록 UseCase를 호출하고 acknowledge한다")
    void handleProductRegisteredEvent_success_registersInventoryAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 2L, 3L);

        // when
        productEventKafkaListener.handleProductRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<RegisterInventoryCommand> captor = ArgumentCaptor.forClass(RegisterInventoryCommand.class);
        verify(registerInventoryUseCase).registerInventory(captor.capture());

        RegisterInventoryCommand command = captor.getValue();
        assertThat(command.productId()).isEqualTo(1L);
        assertThat(command.pricePolicyId()).isEqualTo(2L);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productKey가 있으면 Command에 productKey를 포함하여 재고 등록 UseCase를 호출한다")
    void handleProductRegisteredEvent_withProductKey_passesProductKeyToCommand() {
        // given
        UUID productKey = UUID.randomUUID();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, productKey, 2L, 3L);

        // when
        productEventKafkaListener.handleProductRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<RegisterInventoryCommand> captor = ArgumentCaptor.forClass(RegisterInventoryCommand.class);
        verify(registerInventoryUseCase).registerInventory(captor.capture());
        assertThat(captor.getValue().productKey()).isEqualTo(productKey);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("상품 수정 이벤트 수신 시 productKey 백필 UseCase를 호출하고 acknowledge한다")
    void handleProductUpdatedEvent_withProductKey_triggersBackfill() {
        // given
        UUID productKey = UUID.randomUUID();
        ConsumerRecord<String, EventEnvelope<?>> record = buildUpdatedRecord(10L, productKey);

        // when
        productEventKafkaListener.handleProductUpdatedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<BackfillInventoryProductKeyCommand> captor =
                ArgumentCaptor.forClass(BackfillInventoryProductKeyCommand.class);
        verify(backfillInventoryProductKeyUseCase).backfillProductKey(captor.capture());
        assertThat(captor.getValue().productId()).isEqualTo(10L);
        assertThat(captor.getValue().productKey()).isEqualTo(productKey);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("상품 수정 이벤트 productKey가 null이면 백필을 호출하지 않고 acknowledge한다")
    void handleProductUpdatedEvent_nullProductKey_skipsBackfill() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildUpdatedRecord(10L, null);

        // when
        productEventKafkaListener.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(backfillInventoryProductKeyUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("상품 수정 이벤트 productId가 null이면 백필을 호출하지 않고 acknowledge한다")
    void handleProductUpdatedEvent_nullProductId_skipsBackfill() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildUpdatedRecord(null, UUID.randomUUID());

        // when
        productEventKafkaListener.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(backfillInventoryProductKeyUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 null이면 재고 등록을 호출하지 않고 acknowledge한다")
    void handleProductRegisteredEvent_nullProductId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 2L, 3L);

        // when
        productEventKafkaListener.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(registerInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("pricePolicyId가 null이면 재고 등록을 호출하지 않고 acknowledge한다")
    void handleProductRegisteredEvent_nullPricePolicyId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, null, 3L);

        // when
        productEventKafkaListener.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(registerInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleProductRegisteredEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "product.product.registered", 0, 0L, "key-1", null
        );

        // when
        productEventKafkaListener.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(registerInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handleProductRegisteredEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        ProductRegisteredEvent event = new ProductRegisteredEvent(
                1L, null, 2L, 3L, "테스트 상품", "1", "테스트 브랜드", 10000L, 8000L, 100L, 0L, null, null);
        EventEnvelope<ProductRegisteredEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "product-service",
                LocalDateTime.of(2026, 2, 27, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "product.product.registered", 0, 0L, "key-1", envelope
        );

        // when
        productEventKafkaListener.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(registerInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleProductRegisteredEvent_zeroProductId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, 2L, 3L);

        // when
        productEventKafkaListener.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(registerInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleProductRegisteredEvent_negativeProductId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, 2L, 3L);

        // when
        productEventKafkaListener.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(registerInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("pricePolicyId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleProductRegisteredEvent_zeroPricePolicyId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 0L, 3L);

        // when
        productEventKafkaListener.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(registerInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("pricePolicyId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleProductRegisteredEvent_negativePricePolicyId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, -1L, 3L);

        // when
        productEventKafkaListener.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(registerInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이미 재고가 존재하면 예외를 무시하고 acknowledge한다")
    void handleProductRegisteredEvent_inventoryAlreadyExists_acknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 2L, 3L);
        doThrow(new InventoryAlreadyExistsException(2L))
                .when(registerInventoryUseCase).registerInventory(any(RegisterInventoryCommand.class));

        // when
        productEventKafkaListener.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verify(registerInventoryUseCase).registerInventory(any(RegisterInventoryCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handleProductRegisteredEvent_unexpectedException_propagatesForRetry() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 2L, 3L);
        doThrow(new RuntimeException("네트워크 오류"))
                .when(registerInventoryUseCase).registerInventory(any(RegisterInventoryCommand.class));

        // when & then
        assertThatThrownBy(() -> productEventKafkaListener.handleProductRegisteredEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("네트워크 오류");

        verify(registerInventoryUseCase).registerInventory(any(RegisterInventoryCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }

}
