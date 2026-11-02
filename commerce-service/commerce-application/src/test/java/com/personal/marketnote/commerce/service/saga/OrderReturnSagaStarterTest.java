package com.personal.marketnote.commerce.service.saga;

import com.personal.marketnote.commerce.port.in.command.saga.OrderReturnSagaContext;
import com.personal.marketnote.commerce.port.in.command.saga.OrderReturnSagaContext.ReturnProductItem;
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
@DisplayName("OrderReturnSagaStarter 테스트")
class OrderReturnSagaStarterTest {

    @InjectMocks
    private OrderReturnSagaStarter orderReturnSagaStarter;

    @Mock
    private SagaOrchestrator sagaOrchestrator;

    @Mock
    private OrderReturnSagaDefinition orderReturnSagaDefinition;

    @Test
    @DisplayName("SAGA를 시작하면 ORDER_RETURN:{orderId} 형식의 sagaId로 SagaOrchestrator.start가 호출된다")
    void shouldStartSagaWithCorrectSagaId() {
        List<ReturnProductItem> returnProducts = List.of(
                new ReturnProductItem(10L, null, 2, 25000L)
        );
        OrderReturnSagaContext context = new OrderReturnSagaContext(
                1L, "order-key-1", 100L,
                50000L, 60000L, 5000L, 3000L,
                true, 0L, 5000L,
                returnProducts, "RET-001"
        );

        orderReturnSagaStarter.start(context);

        ArgumentCaptor<String> sagaIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(sagaOrchestrator).start(
                eq(orderReturnSagaDefinition),
                sagaIdCaptor.capture(),
                eq(context)
        );
        assertThat(sagaIdCaptor.getValue()).isEqualTo("ORDER_RETURN:1");
    }

    @Test
    @DisplayName("SAGA를 시작하면 전달받은 컨텍스트가 그대로 전달된다")
    void shouldPassContextToOrchestrator() {
        List<ReturnProductItem> returnProducts = List.of(
                new ReturnProductItem(10L, null, 1, 50000L),
                new ReturnProductItem(20L, null, 2, 25000L)
        );
        OrderReturnSagaContext context = new OrderReturnSagaContext(
                42L, "order-key-42", 200L,
                100000L, 120000L, 10000L, 3000L,
                false, 2500L, 10000L,
                returnProducts, "RET-042"
        );

        orderReturnSagaStarter.start(context);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<OrderReturnSagaContext> contextCaptor =
                ArgumentCaptor.forClass(OrderReturnSagaContext.class);
        verify(sagaOrchestrator).start(
                any(OrderReturnSagaDefinition.class),
                eq("ORDER_RETURN:42"),
                contextCaptor.capture()
        );
        OrderReturnSagaContext captured = contextCaptor.getValue();
        assertThat(captured.orderId()).isEqualTo(42L);
        assertThat(captured.buyerId()).isEqualTo(200L);
        assertThat(captured.isFullReturn()).isFalse();
        assertThat(captured.returnProducts()).hasSize(2);
        assertThat(captured.returnId()).isEqualTo("RET-042");
    }
}
