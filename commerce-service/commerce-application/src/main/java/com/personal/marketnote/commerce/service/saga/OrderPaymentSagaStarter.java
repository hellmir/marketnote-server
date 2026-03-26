package com.personal.marketnote.commerce.service.saga;

import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentSagaContext;
import com.personal.marketnote.common.saga.SagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OrderPaymentSagaStarter {

    private static final String SAGA_ID_PREFIX = "ORDER_PAYMENT:";

    private final SagaOrchestrator sagaOrchestrator;
    private final OrderPaymentSagaDefinition orderPaymentSagaDefinition;

    public void start(OrderPaymentSagaContext context) {
        String sagaId = SAGA_ID_PREFIX + context.orderId();

        log.info("OrderPayment SAGA 시작. sagaId={}, orderId={}, paymentAmount={}",
                sagaId, context.orderId(), context.paymentAmount());

        sagaOrchestrator.start(orderPaymentSagaDefinition, sagaId, context);
    }
}
