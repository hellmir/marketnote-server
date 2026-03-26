package com.personal.marketnote.commerce.service.saga;

import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentSagaContext;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentSagaContext.OrderProductItem;
import com.personal.marketnote.common.saga.SagaInstance;
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
@DisplayName("OrderPaymentSagaStarter 테스트")
class OrderPaymentSagaStarterTest {

    @InjectMocks
    private OrderPaymentSagaStarter orderPaymentSagaStarter;

    @Mock
    private SagaOrchestrator sagaOrchestrator;

    @Mock
    private OrderPaymentSagaDefinition orderPaymentSagaDefinition;

    @Test
    @DisplayName("SAGA를 시작하면 ORDER_PAYMENT:{orderId} 형식의 sagaId로 SagaOrchestrator.start가 호출된다")
    void shouldStartSagaWithCorrectSagaId() {
        // given
        OrderPaymentSagaContext context = new OrderPaymentSagaContext(
                1L, "order-key-1", 100L, 50000L, 60000L, 10000L, 500L,
                List.of(new OrderProductItem(10L, null, 2, 25000L))
        );

        // when
        orderPaymentSagaStarter.start(context);

        // then
        ArgumentCaptor<String> sagaIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(sagaOrchestrator).start(
                eq(orderPaymentSagaDefinition),
                sagaIdCaptor.capture(),
                eq(context)
        );
        assertThat(sagaIdCaptor.getValue()).isEqualTo("ORDER_PAYMENT:1");
    }

    @Test
    @DisplayName("SAGA를 시작하면 전달받은 컨텍스트가 그대로 전달된다")
    void shouldPassContextToOrchestrator() {
        // given
        OrderPaymentSagaContext context = new OrderPaymentSagaContext(
                42L, "order-key-42", 200L, 100000L, 120000L, 20000L, 1000L,
                List.of(
                        new OrderProductItem(10L, 300L, 1, 50000L),
                        new OrderProductItem(20L, null, 2, 25000L)
                )
        );

        // when
        orderPaymentSagaStarter.start(context);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<OrderPaymentSagaContext> contextCaptor =
                ArgumentCaptor.forClass(OrderPaymentSagaContext.class);
        verify(sagaOrchestrator).start(
                any(OrderPaymentSagaDefinition.class),
                eq("ORDER_PAYMENT:42"),
                contextCaptor.capture()
        );
        OrderPaymentSagaContext captured = contextCaptor.getValue();
        assertThat(captured.orderId()).isEqualTo(42L);
        assertThat(captured.buyerId()).isEqualTo(200L);
        assertThat(captured.orderProducts()).hasSize(2);
    }
}
