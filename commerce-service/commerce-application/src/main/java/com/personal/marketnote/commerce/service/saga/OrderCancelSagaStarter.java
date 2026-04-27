package com.personal.marketnote.commerce.service.saga;

import com.personal.marketnote.commerce.port.in.command.saga.OrderCancelSagaContext;
import com.personal.marketnote.common.saga.SagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OrderCancelSagaStarter {

    private static final String SAGA_ID_PREFIX = "ORDER_CANCEL:";

    private final SagaOrchestrator sagaOrchestrator;
    private final OrderCancelSagaDefinition orderCancelSagaDefinition;

    public void start(OrderCancelSagaContext context) {
        String sagaId = SAGA_ID_PREFIX + context.orderId();

        log.info("OrderCancel SAGA 시작. sagaId={}, orderId={}, originalStatus={}",
                sagaId, context.orderId(), context.originalStatus());

        sagaOrchestrator.start(orderCancelSagaDefinition, sagaId, context);
    }
}
