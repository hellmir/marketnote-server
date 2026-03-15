package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.exception.DuplicateInventoryDeductionException;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ReduceProductInventoryUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent.OrderProductItem;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentCompletedInventoryConsumer {
    private final ObjectMapper objectMapper;
    private final ReduceProductInventoryUseCase reduceProductInventoryUseCase;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_PAYMENT_COMPLETED,
            groupId = "commerce-inventory"
    )
    public void handleOrderPaymentCompletedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.ORDER_PAYMENT_COMPLETED)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            OrderPaymentCompletedEvent payload = envelope.getPayloadAs(
                    OrderPaymentCompletedEvent.class, objectMapper
            );

            log.info("주문 결제 완료 이벤트 수신 (재고 차감). eventId={}, orderId={}, orderProducts={}건",
                    envelope.eventId(), payload.orderId(),
                    FormatValidator.hasValue(payload.orderProducts()) ? payload.orderProducts().size() : 0);

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("orderId", payload.orderId()))) {
                acknowledgment.acknowledge();
                return;
            }

            if (FormatValidator.hasNoValue(payload.orderProducts()) || payload.orderProducts().isEmpty()) {
                log.warn("주문 상품이 없는 이벤트. eventId={}, orderId={}",
                        envelope.eventId(), payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            List<OrderProduct> orderProducts = convertToOrderProducts(payload.orderProducts());
            reduceProductInventoryUseCase.reduce(orderProducts, payload.orderId(), "Kafka 결제 완료 재고 차감");

            log.info("재고 차감 완료. orderId={}, 차감 상품={}건",
                    payload.orderId(), orderProducts.size());
        } catch (DuplicateInventoryDeductionException e) {
            log.info("이미 처리된 재고 차감 이벤트 (멱등 처리). eventId={}, message={}",
                    envelope.eventId(), e.getMessage());
        } catch (Exception e) {
            log.error("재고 차감 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }

    private List<OrderProduct> convertToOrderProducts(List<OrderProductItem> items) {
        return items.stream()
                .map(item -> OrderProduct.from(
                        OrderProductSnapshotState.builder()
                                .pricePolicyId(item.pricePolicyId())
                                .quantity(item.quantity())
                                .unitAmount(item.unitAmount())
                                .sharerId(item.sharerId())
                                .build()
                ))
                .toList();
    }
}
