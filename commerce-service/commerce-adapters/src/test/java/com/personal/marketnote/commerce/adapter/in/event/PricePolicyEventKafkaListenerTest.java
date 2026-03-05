package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.InventoryAlreadyExistsException;
import com.personal.marketnote.commerce.port.in.command.inventory.RegisterInventoryCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RegisterInventoryUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PricePolicyCreatedEvent;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricePolicyEventKafkaListenerTest {
    @InjectMocks
    private PricePolicyEventKafkaListener pricePolicyEventKafkaListener;

    @Mock
    private RegisterInventoryUseCase registerInventoryUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long productId, Long pricePolicyId) {
        PricePolicyCreatedEvent event = new PricePolicyCreatedEvent(productId, pricePolicyId);
        EventEnvelope<PricePolicyCreatedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "product.price-policy.created", "product-service",
                LocalDateTime.of(2026, 2, 27, 10, 0), event
        );
        return new ConsumerRecord<>("product.price-policy.created", 0, 0L, "key-1", envelope);
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 재고 등록 UseCase를 호출하고 acknowledge한다")
    void handlePricePolicyCreatedEvent_success_registersInventoryAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 2L);

        // when
        pricePolicyEventKafkaListener.handlePricePolicyCreatedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<RegisterInventoryCommand> captor = ArgumentCaptor.forClass(RegisterInventoryCommand.class);
        verify(registerInventoryUseCase).registerInventory(captor.capture());

        RegisterInventoryCommand command = captor.getValue();
        assertThat(command.productId()).isEqualTo(1L);
        assertThat(command.pricePolicyId()).isEqualTo(2L);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 null이면 재고 등록을 호출하지 않고 acknowledge한다")
    void handlePricePolicyCreatedEvent_nullProductId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 2L);

        // when
        pricePolicyEventKafkaListener.handlePricePolicyCreatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(registerInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("pricePolicyId가 null이면 재고 등록을 호출하지 않고 acknowledge한다")
    void handlePricePolicyCreatedEvent_nullPricePolicyId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, null);

        // when
        pricePolicyEventKafkaListener.handlePricePolicyCreatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(registerInventoryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이미 재고가 존재하면 예외를 무시하고 acknowledge한다")
    void handlePricePolicyCreatedEvent_inventoryAlreadyExists_acknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 2L);
        doThrow(new InventoryAlreadyExistsException(2L))
                .when(registerInventoryUseCase).registerInventory(any(RegisterInventoryCommand.class));

        // when
        pricePolicyEventKafkaListener.handlePricePolicyCreatedEvent(record, acknowledgment);

        // then
        verify(registerInventoryUseCase).registerInventory(any(RegisterInventoryCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handlePricePolicyCreatedEvent_unexpectedException_propagatesForRetry() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 2L);
        doThrow(new RuntimeException("네트워크 오류"))
                .when(registerInventoryUseCase).registerInventory(any(RegisterInventoryCommand.class));

        // when & then
        assertThatThrownBy(() -> pricePolicyEventKafkaListener.handlePricePolicyCreatedEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("네트워크 오류");

        verify(registerInventoryUseCase).registerInventory(any(RegisterInventoryCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }
}
