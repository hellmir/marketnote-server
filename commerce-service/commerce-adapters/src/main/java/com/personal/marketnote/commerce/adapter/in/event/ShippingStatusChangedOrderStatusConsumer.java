package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ShippingStatusChangedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingStatusChangedOrderStatusConsumer {
    private static final String SHIPPING_STATUS_SHIPPING = "SHIPPING";
    private static final String SHIPPING_STATUS_DELIVERED = "DELIVERED";
    private static final String SHIPPING_STATUS_RETURN_SHIPPING = "RETURN_SHIPPING";
    private static final String SHIPPING_STATUS_RETURN_DELIVERED = "RETURN_DELIVERED";

    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.SHIPPING_STATUS_CHANGED,
            groupId = "commerce-order-status"
    )
    public void handleShippingStatusChangedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.SHIPPING_STATUS_CHANGED)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            ShippingStatusChangedEvent payload = envelope.getPayloadAs(ShippingStatusChangedEvent.class, objectMapper);

            log.info("배송 상태 변경 이벤트 수신. eventId={}, orderId={}, shippingStatus={}",
                    envelope.eventId(), payload.orderId(), payload.shippingStatus());

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("orderId", payload.orderId()))) {
                acknowledgment.acknowledge();
                return;
            }

            OrderStatus orderStatus = resolveOrderStatus(payload.shippingStatus());
            if (FormatValidator.hasNoValue(orderStatus)) {
                log.info("주문 상태 전이 불필요한 배송 상태. eventId={}, shippingStatus={}",
                        envelope.eventId(), payload.shippingStatus());
                acknowledgment.acknowledge();
                return;
            }

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(payload.orderId())
                    .orderStatus(orderStatus)
                    .build();
            changeOrderStatusUseCase.changeOrderStatus(command);

            log.info("Kafka 이벤트로 주문 상태 {} 변경 완료. orderId={}",
                    orderStatus, payload.orderId());
        } catch (OrderStatusAlreadyChangedException e) {
            log.warn("이미 주문 상태가 변경됨. eventId={}, key={}, message={}",
                    envelope.eventId(), record.key(), e.getMessage());
        } catch (InvalidOrderStatusTransitionException e) {
            log.warn("전이 불가 상태에서 배송 상태 변경 이벤트 수신. eventId={}, key={}, message={}",
                    envelope.eventId(), record.key(), e.getMessage());
        } catch (Exception e) {
            log.error("주문 상태 변경 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }

    private OrderStatus resolveOrderStatus(String shippingStatus) {
        if (FormatValidator.hasNoValue(shippingStatus)) {
            return null;
        }
        return switch (shippingStatus) {
            case SHIPPING_STATUS_SHIPPING -> OrderStatus.SHIPPING;
            case SHIPPING_STATUS_DELIVERED -> OrderStatus.DELIVERED;
            case SHIPPING_STATUS_RETURN_SHIPPING -> OrderStatus.RETURN_IN_PROGRESS;
            case SHIPPING_STATUS_RETURN_DELIVERED -> OrderStatus.RETURNED;
            default -> null;
        };
    }
}
