package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.InventoryAlreadyExistsException;
import com.personal.marketnote.commerce.port.in.command.inventory.RegisterInventoryCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RegisterInventoryUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductEventKafkaListenerTest {
    @InjectMocks
    private ProductEventKafkaListener productEventKafkaListener;

    @Mock
    private RegisterInventoryUseCase registerInventoryUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(
            Long productId, Long pricePolicyId, Long sellerId
    ) {
        ProductRegisteredEvent event = new ProductRegisteredEvent(
                productId, pricePolicyId, sellerId, "테스트 상품", "1"
        );
        EventEnvelope<ProductRegisteredEvent> envelope = new EventEnvelope<>(
                "test-event-id", "product.product.registered", "product-service",
                LocalDateTime.of(2026, 2, 27, 10, 0), event
        );
        return new ConsumerRecord<>("product.product.registered", 0, 0L, "key-1", envelope);
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

}
