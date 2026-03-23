package com.personal.marketnote.product.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.port.in.command.DeleteCartProductCommand;
import com.personal.marketnote.product.port.in.usecase.cart.DeleteCartProductUseCase;
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
public class OrderPaymentCompletedCartConsumer {
    private final ObjectMapper objectMapper;
    private final DeleteCartProductUseCase deleteCartProductUseCase;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_PAYMENT_COMPLETED,
            groupId = "product-cart"
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

        OrderPaymentCompletedEvent payload = envelope.getPayloadAs(
                OrderPaymentCompletedEvent.class, objectMapper
        );

        log.info("주문 결제 완료 이벤트 수신 (장바구니 삭제). eventId={}, orderId={}, buyerId={}, orderProducts={}건",
                envelope.eventId(), payload.orderId(), payload.buyerId(),
                FormatValidator.hasValue(payload.orderProducts()) ? payload.orderProducts().size() : 0);

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("buyerId", payload.buyerId()))) {
            acknowledgment.acknowledge();
            return;
        }

        if (FormatValidator.hasNoValue(payload.orderProducts()) || payload.orderProducts().isEmpty()) {
            log.error("유효하지 않은 이벤트 페이로드: orderProducts 누락. eventId={}", envelope.eventId());
            acknowledgment.acknowledge();
            return;
        }

        List<Long> pricePolicyIds = payload.orderProducts().stream()
                .map(OrderPaymentCompletedEvent.OrderProductItem::pricePolicyId)
                .toList();

        deleteCartProductUseCase.deleteCartProducts(
                DeleteCartProductCommand.of(payload.buyerId(), pricePolicyIds)
        );

        log.info("장바구니 삭제 완료. orderId={}, buyerId={}, pricePolicyIds={}",
                payload.orderId(), payload.buyerId(), pricePolicyIds);
        // 예외는 DefaultErrorHandler가 재시도 + DLT로 처리

        acknowledgment.acknowledge();
    }
}
