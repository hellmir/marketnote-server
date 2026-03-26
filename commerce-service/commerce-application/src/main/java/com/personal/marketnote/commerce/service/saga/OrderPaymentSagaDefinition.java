package com.personal.marketnote.commerce.service.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentSagaContext;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.saga.SagaDefinition;
import com.personal.marketnote.common.saga.SagaStepDefinition;
import com.personal.marketnote.common.saga.exception.SagaSerializationException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OrderPaymentSagaDefinition implements SagaDefinition<OrderPaymentSagaContext> {

    public static final String SAGA_TYPE = "ORDER_PAYMENT";
    public static final String STEP_DEDUCT_INVENTORY = "DEDUCT_INVENTORY";
    public static final String STEP_RECORD_LEDGER = "RECORD_LEDGER";
    public static final String STEP_PUBLISH_PAYMENT_COMPLETED = "PUBLISH_PAYMENT_COMPLETED";

    private final ObjectMapper objectMapper;

    @Override
    public String getSagaType() {
        return SAGA_TYPE;
    }

    @Override
    public Class<OrderPaymentSagaContext> getContextType() {
        return OrderPaymentSagaContext.class;
    }

    @Override
    public List<SagaStepDefinition<OrderPaymentSagaContext>> getSteps() {
        return List.of(
                new SagaStepDefinition<>(
                        STEP_DEDUCT_INVENTORY,
                        KafkaTopicConstants.SAGA_ORDER_PAYMENT_INVENTORY,
                        this::buildInventoryDeductionAction,
                        this::buildInventoryRestorationCompensation
                ),
                new SagaStepDefinition<>(
                        STEP_RECORD_LEDGER,
                        KafkaTopicConstants.SAGA_ORDER_PAYMENT_LEDGER,
                        this::buildLedgerRecordAction,
                        this::buildLedgerReverseCompensation
                ),
                new SagaStepDefinition<>(
                        STEP_PUBLISH_PAYMENT_COMPLETED,
                        KafkaTopicConstants.SAGA_ORDER_PAYMENT_COMPLETED,
                        this::buildPaymentCompletedAction,
                        null
                )
        );
    }

    private String buildInventoryDeductionAction(OrderPaymentSagaContext context) {
        return serialize(Map.of(
                "orderId", context.orderId(),
                "orderProducts", context.orderProducts()
        ));
    }

    private String buildInventoryRestorationCompensation(OrderPaymentSagaContext context) {
        return serialize(Map.of(
                "orderId", context.orderId(),
                "orderProducts", context.orderProducts()
        ));
    }

    private String buildLedgerRecordAction(OrderPaymentSagaContext context) {
        return serialize(Map.of(
                "orderId", context.orderId(),
                "paymentAmount", context.paymentAmount()
        ));
    }

    private String buildLedgerReverseCompensation(OrderPaymentSagaContext context) {
        return serialize(Map.of(
                "orderId", context.orderId(),
                "cancelAmount", context.paymentAmount(),
                "idempotencyKey", "SAGA_COMP:" + context.orderId()
        ));
    }

    private String buildPaymentCompletedAction(OrderPaymentSagaContext context) {
        return serialize(Map.of(
                "orderId", context.orderId(),
                "buyerId", context.buyerId(),
                "totalAmount", context.totalAmount(),
                "pointAmount", context.pointAmount(),
                "orderProducts", context.orderProducts(),
                "totalAccumulatedPoint", context.totalAccumulatedPoint()
        ));
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new SagaSerializationException("OrderPayment SAGA 페이로드 직렬화에 실패했습니다.", e);
        }
    }
}
