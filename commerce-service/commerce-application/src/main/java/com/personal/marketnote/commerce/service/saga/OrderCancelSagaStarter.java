package com.personal.marketnote.commerce.service.saga;

import com.personal.marketnote.commerce.port.in.command.saga.OrderCancelSagaContext;
import com.personal.marketnote.common.saga.SagaOrchestrator;
import com.personal.marketnote.common.saga.log.SagaContext;
import com.personal.marketnote.common.saga.log.SagaMdcKeys;
import com.personal.marketnote.common.saga.log.SagaStepLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
            sagaId, context.orderId(), context.originalStatus()
        );

        SagaContext sagaContext = SagaContext.restore(
            sagaId,
            OrderCancelSagaDefinition.SAGA_TYPE,
            OrderCancelSagaDefinition.STEP_CANCEL_FULFILLMENT_RELEASE
        );
        // MDC enrich 만 수행. step started/completed 로깅은 SagaOrchestrator.processStep 책임이므로 중복 방지를 위해 호출하지 않는다.
        // 호출 종료 != step 완료 (start()는 첫 step publish 까지만 동기) 이므로 try-finally + clear 로 누수만 차단.
        MDC.put(SagaMdcKeys.SAGA_ID, sagaContext.sagaId());
        MDC.put(SagaMdcKeys.SAGA_TYPE, sagaContext.sagaType());
        MDC.put(SagaMdcKeys.SAGA_STEP, sagaContext.sagaStep());
        try {
            sagaOrchestrator.start(orderCancelSagaDefinition, sagaId, context);
        } finally {
            SagaStepLogger.clear();
        }
    }
}
