package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
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
public class OrderPaymentCompletedInventoryConsumer {
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_PAYMENT_COMPLETED,
            groupId = "commerce-inventory"
    )
    public void handleOrderPaymentCompletedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        try {
            OrderPaymentCompletedEvent payload = envelope.getPayloadAs(
                    OrderPaymentCompletedEvent.class, objectMapper
            );

            log.info("주문 결제 완료 이벤트 수신 (재고 차감). eventId={}, orderId={}, orderProducts={}건",
                    envelope.eventId(), payload.orderId(),
                    FormatValidator.hasValue(payload.orderProducts()) ? payload.orderProducts().size() : 0);

            if (FormatValidator.hasNoValue(payload.orderId())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, orderId=null",
                        envelope.eventId());
                acknowledgment.acknowledge();
                return;
            }

            // FIXME: [#929] 듀얼 라이트 기간 — 동기 호출(ChangeOrderStatusService)에서 이미 재고 차감 수행.
            //  재고 차감은 멱등하지 않으므로, HTTP 제거 후 아래 코드 활성화:
            //  List<OrderProduct> orderProducts = convertToOrderProducts(payload.orderProducts());
            //  reduceProductInventoryUseCase.reduce(orderProducts, "Kafka 결제 완료 재고 차감");

            log.info("듀얼 라이트: 이벤트 수신 확인 완료. orderId={}, 재고 차감은 동기 호출에서 처리됨",
                    payload.orderId());
        } catch (Exception e) {
            log.error("재고 차감 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }
}
