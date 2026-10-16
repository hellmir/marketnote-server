package com.personal.marketnote.commerce.service.saga;

import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentCompletedSagaContext;
import com.personal.marketnote.common.saga.SagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OrderPaymentCompletedSagaStarter {

    private static final String SAGA_ID_PREFIX = "ORDER_PAYMENT_COMPLETED:";

    private final SagaOrchestrator sagaOrchestrator;
    private final OrderPaymentCompletedSagaDefinition definition;

    public void start(OrderPaymentCompletedSagaContext context) {
        String sagaId = SAGA_ID_PREFIX + context.orderId();

        log.info("OrderPaymentCompleted SAGA 시작. sagaId={}, orderId={}, buyerId={}",
                sagaId, context.orderId(), context.buyerId()
        );

        sagaOrchestrator.start(definition, sagaId, context);
    }
}
