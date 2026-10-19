package com.personal.marketnote.commerce.service.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentCompletedSagaContext;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentCompletedSagaContext.OrderProductItem;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.saga.SagaDefinition;
import com.personal.marketnote.common.saga.SagaStepDefinition;
import com.personal.marketnote.common.saga.exception.SagaSerializationException;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OrderPaymentCompletedSagaDefinition implements SagaDefinition<OrderPaymentCompletedSagaContext> {

    public static final String SAGA_TYPE = "ORDER_PAYMENT_COMPLETED";
    public static final String STEP_REGISTER_FULFILLMENT_DELIVERY = "REGISTER_FULFILLMENT_DELIVERY";
    public static final String STEP_DEDUCT_ORDER_POINT = "DEDUCT_ORDER_POINT";
    public static final String STEP_ACCRUE_SHARED_POINT = "ACCRUE_SHARED_POINT";
    public static final String STEP_ACCRUE_PRODUCT_POINT = "ACCRUE_PRODUCT_POINT";
    public static final String STEP_DELETE_CART = "DELETE_CART";

    private final ObjectMapper objectMapper;

    @Override
    public String getSagaType() {
        return SAGA_TYPE;
    }

    @Override
    public Class<OrderPaymentCompletedSagaContext> getContextType() {
        return OrderPaymentCompletedSagaContext.class;
    }

    @Override
    public List<SagaStepDefinition<OrderPaymentCompletedSagaContext>> getSteps() {
        return List.of(
                new SagaStepDefinition<>(
                        STEP_REGISTER_FULFILLMENT_DELIVERY,
                        KafkaTopicConstants.SAGA_ORDER_PAYMENT_DOWNSTREAM_FULFILLMENT,
                        this::buildFulfillmentAction,
                        this::buildFulfillmentCompensation
                ),
                new SagaStepDefinition<>(
                        STEP_DEDUCT_ORDER_POINT,
                        KafkaTopicConstants.SAGA_ORDER_PAYMENT_DOWNSTREAM_ORDER_POINT,
                        this::buildOrderPointAction,
                        this::buildOrderPointCompensation
                ),
                new SagaStepDefinition<>(
                        STEP_ACCRUE_SHARED_POINT,
                        KafkaTopicConstants.SAGA_ORDER_PAYMENT_DOWNSTREAM_SHARED_POINT,
                        this::buildSharedPointAction,
                        this::buildSharedPointCompensation
                ),
                new SagaStepDefinition<>(
                        STEP_ACCRUE_PRODUCT_POINT,
                        KafkaTopicConstants.SAGA_ORDER_PAYMENT_DOWNSTREAM_PRODUCT_POINT,
                        this::buildProductPointAction,
                        this::buildProductPointCompensation
                ),
                new SagaStepDefinition<>(
                        STEP_DELETE_CART,
                        KafkaTopicConstants.SAGA_ORDER_PAYMENT_DOWNSTREAM_CART,
                        this::buildCartAction,
                        null
                )
        );
    }

    private String buildFulfillmentAction(OrderPaymentCompletedSagaContext context) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", context.orderId());
        payload.put("buyerId", context.buyerId());
        payload.put("recipientName", context.recipientName());
        payload.put("recipientPhoneNumber", context.recipientPhoneNumber());
        payload.put("zipCode", context.zipCode());
        payload.put("address", context.address());
        payload.put("addressDetail", context.addressDetail());
        payload.put("deliveryRequestMessage", context.deliveryRequestMessage());
        payload.put("orderProducts", toFulfillmentItems(context.orderProducts()));
        return serialize(payload);
    }

    private List<Map<String, Object>> toFulfillmentItems(List<OrderProductItem> items) {
        if (FormatValidator.hasNoValue(items)) {
            return List.of();
        }
        return items.stream()
                .map(item -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("pricePolicyId", item.pricePolicyId());
                    map.put("productKey", item.productKey());
                    map.put("sharerKey", item.sharerKey());
                    map.put("quantity", item.quantity());
                    map.put("unitAmount", item.unitAmount());
                    return map;
                })
                .toList();
    }

    private String buildFulfillmentCompensation(OrderPaymentCompletedSagaContext context) {
        return serialize(Map.of("orderId", context.orderId()));
    }

    private String buildOrderPointAction(OrderPaymentCompletedSagaContext context) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", context.orderId());
        payload.put("buyerId", context.buyerId());
        payload.put("pointAmount", context.pointAmount());
        return serialize(payload);
    }

    private String buildOrderPointCompensation(OrderPaymentCompletedSagaContext context) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", context.orderId());
        payload.put("buyerId", context.buyerId());
        payload.put("pointAmount", context.pointAmount());
        return serialize(payload);
    }

    private String buildSharedPointAction(OrderPaymentCompletedSagaContext context) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", context.orderId());
        payload.put("buyerId", context.buyerId());
        payload.put("totalAmount", context.totalAmount());
        payload.put("sharerKeys", extractSharerKeys(context.orderProducts()));
        return serialize(payload);
    }

    private String buildSharedPointCompensation(OrderPaymentCompletedSagaContext context) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", context.orderId());
        payload.put("buyerId", context.buyerId());
        payload.put("sharerKeys", extractSharerKeys(context.orderProducts()));
        return serialize(payload);
    }

    private String buildProductPointAction(OrderPaymentCompletedSagaContext context) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", context.orderId());
        payload.put("buyerId", context.buyerId());
        payload.put("totalAmount", context.totalAmount());
        payload.put("totalAccumulatedPoint", context.totalAccumulatedPoint());
        return serialize(payload);
    }

    private String buildProductPointCompensation(OrderPaymentCompletedSagaContext context) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", context.orderId());
        payload.put("buyerId", context.buyerId());
        return serialize(payload);
    }

    private String buildCartAction(OrderPaymentCompletedSagaContext context) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("buyerId", context.buyerId());
        payload.put("orderId", context.orderId());
        payload.put("pricePolicyIds", extractPricePolicyIds(context.orderProducts()));
        return serialize(payload);
    }

    private List<UUID> extractSharerKeys(List<OrderProductItem> orderProducts) {
        if (FormatValidator.hasNoValue(orderProducts)) {
            return List.of();
        }
        return orderProducts.stream()
                .map(OrderProductItem::sharerKey)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<Long> extractPricePolicyIds(List<OrderProductItem> orderProducts) {
        if (FormatValidator.hasNoValue(orderProducts)) {
            return List.of();
        }
        return orderProducts.stream()
                .map(OrderProductItem::pricePolicyId)
                .filter(Objects::nonNull)
                .toList();
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new SagaSerializationException("OrderPaymentCompleted SAGA 페이로드 직렬화에 실패했습니다.", e);
        }
    }
}
