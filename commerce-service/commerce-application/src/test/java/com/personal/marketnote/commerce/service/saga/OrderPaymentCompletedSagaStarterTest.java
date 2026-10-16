package com.personal.marketnote.commerce.service.saga;

import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentCompletedSagaContext;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentCompletedSagaContext.OrderProductItem;
import com.personal.marketnote.common.saga.SagaOrchestrator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentCompletedSagaStarter 테스트")
class OrderPaymentCompletedSagaStarterTest {

    @InjectMocks
    private OrderPaymentCompletedSagaStarter starter;

    @Mock
    private SagaOrchestrator sagaOrchestrator;

    @Mock
    private OrderPaymentCompletedSagaDefinition definition;

    @Test
    @DisplayName("SAGA를 시작하면 ORDER_PAYMENT_COMPLETED:{orderId} 형식의 sagaId로 start가 호출된다")
    void shouldStartSagaWithCorrectSagaId() {
        OrderPaymentCompletedSagaContext context = OrderPaymentCompletedSagaContext.builder()
                .orderId(1L)
                .buyerId(100L)
                .totalAmount(60000L)
                .pointAmount(10000L)
                .totalAccumulatedPoint(500L)
                .orderProducts(List.of(
                        new OrderProductItem(10L, 1000L, null, 2, 25000L)
                ))
                .build();

        starter.start(context);

        ArgumentCaptor<String> sagaIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(sagaOrchestrator).start(
                eq(definition),
                sagaIdCaptor.capture(),
                eq(context)
        );
        assertThat(sagaIdCaptor.getValue()).isEqualTo("ORDER_PAYMENT_COMPLETED:1");
    }

    @Test
    @DisplayName("SAGA를 시작하면 전달받은 컨텍스트가 그대로 전달된다")
    void shouldPassContextToOrchestrator() {
        OrderPaymentCompletedSagaContext context = OrderPaymentCompletedSagaContext.builder()
                .orderId(42L)
                .buyerId(200L)
                .totalAmount(120000L)
                .pointAmount(20000L)
                .totalAccumulatedPoint(1000L)
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .orderProducts(List.of(
                        new OrderProductItem(
                                10L, 1000L, UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), 1, 50000L
                        )
                ))
                .build();

        starter.start(context);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<OrderPaymentCompletedSagaContext> contextCaptor =
                ArgumentCaptor.forClass(OrderPaymentCompletedSagaContext.class);
        verify(sagaOrchestrator).start(
                any(OrderPaymentCompletedSagaDefinition.class),
                eq("ORDER_PAYMENT_COMPLETED:42"),
                contextCaptor.capture()
        );
        OrderPaymentCompletedSagaContext captured = contextCaptor.getValue();
        assertThat(captured.orderId()).isEqualTo(42L);
        assertThat(captured.buyerId()).isEqualTo(200L);
        assertThat(captured.recipientName()).isEqualTo("홍길동");
    }
}
