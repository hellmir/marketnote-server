package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.exception.DuplicateInventoryRestorationException;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RestoreProductInventoryUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderReturnedEvent;
import com.personal.marketnote.common.kafka.event.OrderReturnedEvent.OrderProductItem;
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
public class OrderReturnedInventoryConsumer {
    private final ObjectMapper objectMapper;
    private final RestoreProductInventoryUseCase restoreProductInventoryUseCase;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_RETURNED,
            groupId = "commerce-order-returned-inventory"
    )
    public void handleOrderReturnedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.ORDER_RETURNED)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            OrderReturnedEvent payload = envelope.getPayloadAs(OrderReturnedEvent.class, objectMapper);

            log.info("반품 완료 이벤트 수신 (재고 복구). eventId={}, orderId={}, isFullReturn={}",
                    envelope.eventId(), payload.orderId(), payload.isFullReturn());

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("orderId", payload.orderId()))) {
                acknowledgment.acknowledge();
                return;
            }

            if (FormatValidator.hasNoValue(payload.returnProducts()) || payload.returnProducts().isEmpty()) {
                log.warn("반품 완료인데 returnProducts가 없는 이벤트. eventId={}, orderId={}",
                        envelope.eventId(), payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            if (hasInvalidReturnProductItem(payload.returnProducts())) {
                log.error("유효하지 않은 returnProduct 항목. eventId={}, orderId={}",
                        envelope.eventId(), payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            List<OrderProduct> returnOrderProducts = convertToOrderProducts(payload.returnProducts());
            String reason = resolveReason(payload.isFullReturn());

            restoreProductInventoryUseCase.restore(returnOrderProducts, payload.orderId(), reason);

            log.info("반품 완료 재고 복구 완료. orderId={}, returnProducts={}건",
                    payload.orderId(), payload.returnProducts().size());
        } catch (DuplicateInventoryRestorationException e) {
            log.info("이미 처리된 재고 복구 이벤트 (멱등 처리). eventId={}, message={}",
                    envelope.eventId(), e.getMessage());
        } catch (Exception e) {
            log.error("반품 완료 재고 복구 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }

    private boolean hasInvalidReturnProductItem(List<OrderProductItem> items) {
        return items.stream()
                .anyMatch(item -> FormatValidator.hasNoValue(item.pricePolicyId())
                        || item.pricePolicyId() <= 0
                        || FormatValidator.hasNoValue(item.quantity())
                        || item.quantity() <= 0);
    }

    private String resolveReason(boolean isFullReturn) {
        if (isFullReturn) {
            return "반품 완료 전체 재고 복구";
        }
        return "반품 완료 부분 재고 복구";
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
