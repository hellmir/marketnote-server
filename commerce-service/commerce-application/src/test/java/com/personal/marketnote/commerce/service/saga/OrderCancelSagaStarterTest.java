package com.personal.marketnote.commerce.service.saga;

import com.personal.marketnote.commerce.port.in.command.saga.OrderCancelSagaContext;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentSagaContext.OrderProductItem;
import com.personal.marketnote.common.saga.SagaOrchestrator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCancelSagaStarter 테스트")
class OrderCancelSagaStarterTest {

    @InjectMocks
    private OrderCancelSagaStarter orderCancelSagaStarter;

    @Mock
    private SagaOrchestrator sagaOrchestrator;

    @Mock
    private OrderCancelSagaDefinition orderCancelSagaDefinition;

    @Test
    @DisplayName("SAGA를 시작하면 ORDER_CANCEL:{orderId} 형식의 sagaId로 SagaOrchestrator.start가 호출된다")
    void shouldStartSagaWithCorrectSagaId() {
        OrderCancelSagaContext context = new OrderCancelSagaContext(
                1L, "order-key-1", 100L,
                50000L, 50000L, 0L, 3000L,
                true, 0L,
                "PAID", "CANCEL_ORDER", "구매 의사 취소",
                List.of(new OrderProductItem(10L, null, 2, 25000L))
        );

        orderCancelSagaStarter.start(context);

        ArgumentCaptor<String> sagaIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(sagaOrchestrator).start(
                eq(orderCancelSagaDefinition),
                sagaIdCaptor.capture(),
                eq(context)
        );
        assertThat(sagaIdCaptor.getValue()).isEqualTo("ORDER_CANCEL:1");
    }

    @Test
    @DisplayName("SAGA를 시작하면 전달받은 컨텍스트가 그대로 전달된다")
    void shouldPassContextToOrchestrator() {
        OrderCancelSagaContext context = new OrderCancelSagaContext(
                42L, "order-key-42", 200L,
                100000L, 100000L, 5000L, 3000L,
                true, 0L,
                "PREPARING", "MISTAKE", "주문 실수",
                List.of(
                        new OrderProductItem(10L, null, 1, 50000L),
                        new OrderProductItem(20L, null, 2, 25000L)
                )
        );

        orderCancelSagaStarter.start(context);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<OrderCancelSagaContext> contextCaptor =
                ArgumentCaptor.forClass(OrderCancelSagaContext.class);
        verify(sagaOrchestrator).start(
                any(OrderCancelSagaDefinition.class),
                eq("ORDER_CANCEL:42"),
                contextCaptor.capture()
        );
        OrderCancelSagaContext captured = contextCaptor.getValue();
        assertThat(captured.orderId()).isEqualTo(42L);
        assertThat(captured.buyerId()).isEqualTo(200L);
        assertThat(captured.originalStatus()).isEqualTo("PREPARING");
        assertThat(captured.orderProducts()).hasSize(2);
    }
}
