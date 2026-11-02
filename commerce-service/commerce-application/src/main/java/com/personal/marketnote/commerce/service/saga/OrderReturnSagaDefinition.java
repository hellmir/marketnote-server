package com.personal.marketnote.commerce.service.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.in.command.saga.OrderReturnSagaContext;
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
public class OrderReturnSagaDefinition implements SagaDefinition<OrderReturnSagaContext> {

    public static final String SAGA_TYPE = "ORDER_RETURN";
    public static final String STEP_REFUND_PAYMENT = "REFUND_PAYMENT";
    public static final String STEP_RECORD_REVERSE_LEDGER = "RECORD_REVERSE_LEDGER";
    public static final String STEP_CANCEL_SETTLEMENT_ALLOCATION = "CANCEL_SETTLEMENT_ALLOCATION";
    public static final String STEP_RESTORE_INVENTORY = "RESTORE_INVENTORY";
    public static final String STEP_REFUND_POINT = "REFUND_POINT";
    public static final String STEP_PUBLISH_RETURN_COMPLETED = "PUBLISH_RETURN_COMPLETED";

    private final ObjectMapper objectMapper;

    @Override
    public String getSagaType() {
        return SAGA_TYPE;
    }

    @Override
    public Class<OrderReturnSagaContext> getContextType() {
        return OrderReturnSagaContext.class;
    }

    @Override
    public List<SagaStepDefinition<OrderReturnSagaContext>> getSteps() {
        return List.of(
                new SagaStepDefinition<>(
                        STEP_REFUND_PAYMENT,
                        KafkaTopicConstants.SAGA_ORDER_RETURN_REFUND,
                        this::buildRefundPaymentAction,
                        null
                ),
                new SagaStepDefinition<>(
                        STEP_RECORD_REVERSE_LEDGER,
                        KafkaTopicConstants.SAGA_ORDER_RETURN_LEDGER,
                        this::buildReverseLedgerAction,
                        this::buildReverseLedgerCompensation
                ),
                new SagaStepDefinition<>(
                        STEP_CANCEL_SETTLEMENT_ALLOCATION,
                        KafkaTopicConstants.SAGA_ORDER_RETURN_SETTLEMENT,
                        this::buildCancelSettlementAction,
                        this::buildCancelSettlementCompensation
                ),
                new SagaStepDefinition<>(
                        STEP_RESTORE_INVENTORY,
                        KafkaTopicConstants.SAGA_ORDER_RETURN_INVENTORY,
                        this::buildRestoreInventoryAction,
                        this::buildRestoreInventoryCompensation
                ),
                new SagaStepDefinition<>(
                        STEP_REFUND_POINT,
                        KafkaTopicConstants.SAGA_ORDER_RETURN_POINT_REFUND,
                        this::buildRefundPointAction,
                        this::buildRefundPointCompensation
                ),
                new SagaStepDefinition<>(
                        STEP_PUBLISH_RETURN_COMPLETED,
                        KafkaTopicConstants.SAGA_ORDER_RETURN_COMPLETED,
                        this::buildReturnCompletedAction,
                        null
                )
        );
    }

    private String buildRefundPaymentAction(OrderReturnSagaContext context) {
        return serialize(Map.ofEntries(
                entry("orderId", context.orderId()),
                entry("orderKey", context.orderKey()),
                entry("buyerId", context.buyerId()),
                entry("returnAmount", context.returnAmount()),
                entry("paymentAmount", context.paymentAmount()),
                entry("isFullReturn", context.isFullReturn()),
                entry("returnShippingFee", context.returnShippingFee())
        ));
    }

    private String buildReverseLedgerAction(OrderReturnSagaContext context) {
        return serialize(Map.of(
                "orderId", context.orderId(),
                "returnAmount", context.returnAmount(),
                "idempotencyKey", "SAGA_ORDER_RETURN:" + context.orderId()
        ));
    }

    private String buildReverseLedgerCompensation(OrderReturnSagaContext context) {
        return serialize(Map.of(
                "orderId", context.orderId(),
                "paymentAmount", context.returnAmount(),
                "idempotencyKey", "SAGA_ORDER_RETURN_COMP:" + context.orderId()
        ));
    }

    private String buildCancelSettlementAction(OrderReturnSagaContext context) {
        return serialize(Map.of(
                "orderId", context.orderId()
        ));
    }

    private String buildCancelSettlementCompensation(OrderReturnSagaContext context) {
        return serialize(Map.of(
                "orderId", context.orderId()
        ));
    }

    private String buildRestoreInventoryAction(OrderReturnSagaContext context) {
        return serialize(Map.of(
                "orderId", context.orderId(),
                "returnProducts", context.returnProducts(),
                "isFullReturn", context.isFullReturn()
        ));
    }

    private String buildRestoreInventoryCompensation(OrderReturnSagaContext context) {
        return serialize(Map.of(
                "orderId", context.orderId(),
                "returnProducts", context.returnProducts()
        ));
    }

    private String buildRefundPointAction(OrderReturnSagaContext context) {
        return serialize(Map.of(
                "orderId", context.orderId(),
                "buyerId", context.buyerId(),
                "refundPointAmount", context.refundPointAmount()
        ));
    }

    private String buildRefundPointCompensation(OrderReturnSagaContext context) {
        return serialize(Map.of(
                "orderId", context.orderId(),
                "buyerId", context.buyerId(),
                "refundPointAmount", context.refundPointAmount()
        ));
    }

    private String buildReturnCompletedAction(OrderReturnSagaContext context) {
        return serialize(Map.ofEntries(
                entry("orderId", context.orderId()),
                entry("orderKey", context.orderKey()),
                entry("buyerId", context.buyerId()),
                entry("returnAmount", context.returnAmount()),
                entry("paymentAmount", context.paymentAmount()),
                entry("pointAmount", context.pointAmount()),
                entry("shippingFee", context.shippingFee()),
                entry("isFullReturn", context.isFullReturn()),
                entry("returnShippingFee", context.returnShippingFee()),
                entry("returnProducts", context.returnProducts()),
                entry("returnId", context.returnId())
        ));
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new SagaSerializationException("OrderReturn SAGA 페이로드 직렬화에 실패했습니다.", e);
        }
    }
}
