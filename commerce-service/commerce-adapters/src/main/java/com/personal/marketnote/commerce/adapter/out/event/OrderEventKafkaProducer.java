package com.personal.marketnote.commerce.adapter.out.event;

import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent.OrderProductItem;
import com.personal.marketnote.common.kafka.event.OrderPurchaseConfirmedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Clock;
import java.util.List;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class OrderEventKafkaProducer implements PublishOrderEventPort {
    private static final String SOURCE = "commerce-service";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Clock clock;

    @Override
    public void publishOrderPaymentCompletedEvent(Long orderId, Long buyerId, Long totalAmount,
                                                  Long pointAmount, List<OrderProduct> orderProducts) {
        List<OrderProductItem> items = orderProducts.stream()
                .map(op -> new OrderProductItem(
                        op.getPricePolicyId(),
                        op.getSharerId(),
                        op.getQuantity(),
                        op.getUnitAmount()
                ))
                .toList();

        OrderPaymentCompletedEvent payload = new OrderPaymentCompletedEvent(
                orderId, buyerId, totalAmount, pointAmount, items
        );
        String topic = KafkaTopicConstants.ORDER_PAYMENT_COMPLETED;
        EventEnvelope<OrderPaymentCompletedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        kafkaTemplate.send(topic, orderId.toString(), envelope)
                .whenComplete((result, ex) -> {
                    if (FormatValidator.hasValue(ex)) {
                        log.error("Kafka 이벤트 발행 실패. topic={}, orderId={}",
                                topic, orderId, ex);
                        return;
                    }

                    log.info("Kafka 이벤트 발행 성공. topic={}, orderId={}, offset={}",
                            topic, orderId,
                            result.getRecordMetadata().offset());
                });
    }

    @Override
    public void publishOrderPurchaseConfirmedEvent(Long orderId, Long buyerId, List<Long> sharerIds) {
        OrderPurchaseConfirmedEvent payload = new OrderPurchaseConfirmedEvent(orderId, buyerId, sharerIds);
        String topic = KafkaTopicConstants.ORDER_PURCHASE_CONFIRMED;
        EventEnvelope<OrderPurchaseConfirmedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        kafkaTemplate.send(topic, orderId.toString(), envelope)
                .whenComplete((result, ex) -> {
                    if (FormatValidator.hasValue(ex)) {
                        log.error("Kafka 이벤트 발행 실패. topic={}, orderId={}, buyerId={}",
                                topic, orderId, buyerId, ex);
                        return;
                    }

                    log.info("Kafka 이벤트 발행 성공. topic={}, orderId={}, buyerId={}, offset={}",
                            topic, orderId, buyerId,
                            result.getRecordMetadata().offset());
                });
    }
}
