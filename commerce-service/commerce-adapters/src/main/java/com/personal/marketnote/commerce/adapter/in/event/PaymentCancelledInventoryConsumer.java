package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.exception.DuplicateInventoryRestorationException;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RestoreProductInventoryUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent.OrderProductItem;
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
public class PaymentCancelledInventoryConsumer {
    private final ObjectMapper objectMapper;
    private final RestoreProductInventoryUseCase restoreProductInventoryUseCase;

    @KafkaListener(
            topics = KafkaTopicConstants.PAYMENT_CANCELLED,
            groupId = "commerce-inventory"
    )
    public void handlePaymentCancelledEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.PAYMENT_CANCELLED)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            PaymentCancelledEvent payload = envelope.getPayloadAs(PaymentCancelledEvent.class, objectMapper);

            log.info("결제 취소 이벤트 수신 (재고 복구). eventId={}, orderId={}, isFullCancel={}",
                    envelope.eventId(), payload.orderId(), payload.isFullCancel());

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("orderId", payload.orderId()))) {
                acknowledgment.acknowledge();
                return;
            }

            if (payload.isFullCancel()) {
                handleFullCancelInventoryRestore(envelope, payload);
            }

            if (!payload.isFullCancel()) {
                handlePartialCancelInventoryRestore(envelope, payload);
            }
        } catch (DuplicateInventoryRestorationException e) {
            log.info("이미 처리된 재고 복구 이벤트 (멱등 처리). eventId={}, message={}",
                    envelope.eventId(), e.getMessage());
        } catch (Exception e) {
            log.error("재고 복구 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }

    private void handleFullCancelInventoryRestore(EventEnvelope<?> envelope, PaymentCancelledEvent payload) {
        if (FormatValidator.hasNoValue(payload.orderProducts()) || payload.orderProducts().isEmpty()) {
            log.warn("전체 취소인데 주문 상품이 없는 이벤트. eventId={}, orderId={}",
                    envelope.eventId(), payload.orderId());
            return;
        }

        List<OrderProduct> orderProducts = convertToOrderProducts(payload.orderProducts());
        restoreProductInventoryUseCase.restore(orderProducts, payload.orderId(), "Kafka 전액 취소 재고 복구");

        log.info("전체 취소 재고 복구 완료. orderId={}, orderProducts={}건",
                payload.orderId(), payload.orderProducts().size());
    }

    private void handlePartialCancelInventoryRestore(EventEnvelope<?> envelope, PaymentCancelledEvent payload) {
        List<OrderProductItem> cancelProducts = payload.cancelProducts();

        if (FormatValidator.hasNoValue(cancelProducts) || cancelProducts.isEmpty()) {
            log.warn("부분 취소인데 cancelProducts가 없는 이벤트. eventId={}, orderId={}",
                    envelope.eventId(), payload.orderId());
            return;
        }

        List<OrderProduct> cancelOrderProducts = convertToOrderProducts(cancelProducts);
        restoreProductInventoryUseCase.restore(cancelOrderProducts, payload.orderId(), "Kafka 부분 취소 재고 복구");

        log.info("부분 취소 재고 복구 완료. orderId={}, cancelProducts={}건",
                payload.orderId(), cancelProducts.size());
    }

    private List<OrderProduct> convertToOrderProducts(List<OrderProductItem> items) {
        return items.stream()
                .map(item -> OrderProduct.from(
                        OrderProductSnapshotState.builder()
                                .pricePolicyId(item.pricePolicyId())
                                .quantity(item.quantity())
                                .unitAmount(item.unitAmount())
                                .sharerKey(item.sharerKey())
                                .build()
                ))
                .toList();
    }
}
