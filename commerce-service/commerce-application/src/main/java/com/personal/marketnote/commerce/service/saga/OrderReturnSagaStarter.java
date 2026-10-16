package com.personal.marketnote.commerce.service.saga;

import com.personal.marketnote.commerce.port.in.command.saga.OrderReturnSagaContext;
import com.personal.marketnote.common.saga.SagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OrderReturnSagaStarter {

    private static final String SAGA_ID_PREFIX = "ORDER_RETURN:";

    private final SagaOrchestrator sagaOrchestrator;
    private final OrderReturnSagaDefinition orderReturnSagaDefinition;

    public void start(OrderReturnSagaContext context) {
        String sagaId = SAGA_ID_PREFIX + context.orderId();

        log.info("OrderReturn SAGA 시작. sagaId={}, orderId={}, isFullReturn={}",
                sagaId, context.orderId(), context.isFullReturn()
        );

        sagaOrchestrator.start(orderReturnSagaDefinition, sagaId, context);
    }
}
