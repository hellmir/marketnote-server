package com.personal.marketnote.commerce.adapter.out.event;

import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.port.out.event.PublishPaymentEventPort;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PaymentApprovedEvent;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent;
import com.personal.marketnote.common.kafka.event.PaymentFailedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Clock;
import java.util.List;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class PaymentEventKafkaProducer implements PublishPaymentEventPort {
    private static final String SOURCE = "commerce-service";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Clock clock;

    @Override
    public void publishPaymentApprovedEvent(Long orderId, String orderKey, Long paymentAmount) {
        PaymentApprovedEvent payload = new PaymentApprovedEvent(orderId, orderKey, paymentAmount);
        String topic = KafkaTopicConstants.PAYMENT_APPROVED;
        EventEnvelope<PaymentApprovedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        kafkaTemplate.send(topic, orderId.toString(), envelope)
                .whenComplete((result, ex) -> {
                    if (FormatValidator.hasValue(ex)) {
                        log.error("Kafka 이벤트 발행 실패. topic={}, orderId={}, orderKey={}",
                                topic, orderId, orderKey, ex);
                        return;
                    }

                    log.info("Kafka 이벤트 발행 성공. topic={}, orderId={}, orderKey={}, offset={}",
                            topic, orderId, orderKey,
                            result.getRecordMetadata().offset());
                });
    }

    @Override
    public void publishPaymentFailedEvent(Long orderId, String orderKey, String resultCode, String resultMessage) {
        PaymentFailedEvent payload = new PaymentFailedEvent(orderId, orderKey, resultCode, resultMessage);
        String topic = KafkaTopicConstants.PAYMENT_FAILED;
        EventEnvelope<PaymentFailedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        kafkaTemplate.send(topic, orderId.toString(), envelope)
                .whenComplete((result, ex) -> {
                    if (FormatValidator.hasValue(ex)) {
                        log.error("Kafka 이벤트 발행 실패. topic={}, orderId={}, orderKey={}",
                                topic, orderId, orderKey, ex);
                        return;
                    }

                    log.info("Kafka 이벤트 발행 성공. topic={}, orderId={}, orderKey={}, offset={}",
                            topic, orderId, orderKey,
                            result.getRecordMetadata().offset());
                });
    }

    @Override
    public void publishPaymentCancelledEvent(Long orderId, String orderKey, Long buyerId,
                                             Long cancelAmount, Long paymentAmount, Long pointAmount,
                                             boolean isFullCancel, Long alreadyRefunded,
                                             List<OrderProduct> orderProducts,
                                             List<OrderProduct> cancelProducts) {
        List<PaymentCancelledEvent.OrderProductItem> items = orderProducts.stream()
                .map(op -> new PaymentCancelledEvent.OrderProductItem(
                        op.getPricePolicyId(),
                        op.getSharerId(),
                        op.getQuantity(),
                        op.getUnitAmount()
                ))
                .toList();

        List<PaymentCancelledEvent.OrderProductItem> cancelItems = FormatValidator.hasValue(cancelProducts)
                ? cancelProducts.stream()
                .map(op -> new PaymentCancelledEvent.OrderProductItem(
                        op.getPricePolicyId(),
                        op.getSharerId(),
                        op.getQuantity(),
                        op.getUnitAmount()
                ))
                .toList()
                : null;

        PaymentCancelledEvent payload = new PaymentCancelledEvent(
                orderId, orderKey, buyerId, cancelAmount, paymentAmount,
                pointAmount, isFullCancel, alreadyRefunded, items, cancelItems
        );
        String topic = KafkaTopicConstants.PAYMENT_CANCELLED;
        EventEnvelope<PaymentCancelledEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        kafkaTemplate.send(topic, orderId.toString(), envelope)
                .whenComplete((result, ex) -> {
                    if (FormatValidator.hasValue(ex)) {
                        log.error("Kafka 이벤트 발행 실패. topic={}, orderId={}, orderKey={}",
                                topic, orderId, orderKey, ex);
                        return;
                    }

                    log.info("Kafka 이벤트 발행 성공. topic={}, orderId={}, orderKey={}, offset={}",
                            topic, orderId, orderKey,
                            result.getRecordMetadata().offset());
                });
    }
}
