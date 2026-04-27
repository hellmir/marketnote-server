package com.personal.marketnote.commerce.service.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.in.command.saga.OrderCancelSagaContext;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.saga.SagaDefinition;
import com.personal.marketnote.common.saga.SagaStepDefinition;
import com.personal.marketnote.common.saga.exception.SagaSerializationException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OrderCancelSagaDefinition implements SagaDefinition<OrderCancelSagaContext> {

    public static final String SAGA_TYPE = "ORDER_CANCEL";
    public static final String STEP_CANCEL_FULFILLMENT_RELEASE = "CANCEL_FULFILLMENT_RELEASE";
    public static final String STEP_REFUND_PAYMENT = "REFUND_PAYMENT";
    public static final String STEP_COMPLETE_CANCELLATION = "COMPLETE_CANCELLATION";

    private final ObjectMapper objectMapper;

    @Override
    public String getSagaType() {
        return SAGA_TYPE;
    }

    @Override
    public Class<OrderCancelSagaContext> getContextType() {
        return OrderCancelSagaContext.class;
    }

    @Override
    public List<SagaStepDefinition<OrderCancelSagaContext>> getSteps() {
        return List.of(
                new SagaStepDefinition<>(
                        STEP_CANCEL_FULFILLMENT_RELEASE,
                        KafkaTopicConstants.SAGA_ORDER_CANCEL_FULFILLMENT,
                        this::buildFulfillmentCancelAction,
                        null
                ),
                new SagaStepDefinition<>(
                        STEP_REFUND_PAYMENT,
                        KafkaTopicConstants.SAGA_ORDER_CANCEL_REFUND,
                        this::buildRefundPaymentAction,
                        null
                ),
                new SagaStepDefinition<>(
                        STEP_COMPLETE_CANCELLATION,
                        KafkaTopicConstants.SAGA_ORDER_CANCEL_COMPLETED,
                        this::buildCompleteCancellationAction,
                        null
                )
        );
    }

    private String buildFulfillmentCancelAction(OrderCancelSagaContext context) {
        return serialize(Map.of(
                "orderId", context.orderId(),
                "originalStatus", context.originalStatus()
        ));
    }

    private String buildRefundPaymentAction(OrderCancelSagaContext context) {
        return serialize(Map.of(
                "orderId", context.orderId(),
                "orderKey", context.orderKey(),
                "buyerId", context.buyerId(),
                "cancelAmount", context.cancelAmount(),
                "paymentAmount", context.paymentAmount(),
                "pointAmount", context.pointAmount(),
                "shippingFee", context.shippingFee(),
                "isFullCancel", context.isFullCancel(),
                "alreadyRefunded", context.alreadyRefunded()
        ));
    }

    private String buildCompleteCancellationAction(OrderCancelSagaContext context) {
        return serialize(Map.ofEntries(
                entry("orderId", context.orderId()),
                entry("orderKey", context.orderKey()),
                entry("buyerId", context.buyerId()),
                entry("cancelAmount", context.cancelAmount()),
                entry("paymentAmount", context.paymentAmount()),
                entry("pointAmount", context.pointAmount()),
                entry("shippingFee", context.shippingFee()),
                entry("isFullCancel", context.isFullCancel()),
                entry("alreadyRefunded", context.alreadyRefunded()),
                entry("reasonCategory", context.reasonCategory()),
                entry("reason", context.reason()),
                entry("orderProducts", context.orderProducts())
        ));
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new SagaSerializationException("OrderCancel SAGA 페이로드 직렬화에 실패했습니다.", e);
        }
    }
}
