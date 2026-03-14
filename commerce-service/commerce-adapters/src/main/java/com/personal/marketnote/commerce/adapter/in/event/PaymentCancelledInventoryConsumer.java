package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
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

    @KafkaListener(
            topics = KafkaTopicConstants.PAYMENT_CANCELLED,
            groupId = "commerce-inventory"
    )
    public void handlePaymentCancelledEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        try {
            PaymentCancelledEvent payload = envelope.getPayloadAs(PaymentCancelledEvent.class, objectMapper);

            log.info("결제 취소 이벤트 수신 (재고 복구). eventId={}, orderId={}, isFullCancel={}",
                    envelope.eventId(), payload.orderId(), payload.isFullCancel());

            if (FormatValidator.hasNoValue(payload.orderId())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, orderId=null",
                        envelope.eventId());
                acknowledgment.acknowledge();
                return;
            }

            if (payload.isFullCancel()) {
                handleFullCancelInventoryRestore(envelope, payload);
            } else {
                handlePartialCancelInventoryRestore(envelope, payload);
            }
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

        // FIXME: [#929][#1101] HTTP 제거 후 RestoreProductInventoryUseCase.restore() 활성화
        //  현재 듀얼 라이트 기간: CancelPaymentService.restoreInventory()에서 동기로 재고 복구 처리 중
        //  멱등성 보강(#1210) 완료 후 활성화
        //  List<OrderProduct> orderProducts = convertToOrderProducts(payload.orderProducts());
        //  restoreProductInventoryUseCase.restore(orderProducts, "Kafka 전액 취소 재고 복구");

        log.info("전체 취소 재고 복구 이벤트 검증 완료 (듀얼 라이트). orderId={}, orderProducts={}건",
                payload.orderId(), payload.orderProducts().size());
    }

    private void handlePartialCancelInventoryRestore(EventEnvelope<?> envelope, PaymentCancelledEvent payload) {
        List<OrderProductItem> cancelProducts = payload.cancelProducts();

        if (FormatValidator.hasNoValue(cancelProducts) || cancelProducts.isEmpty()) {
            log.warn("부분 취소인데 cancelProducts가 없는 이벤트. eventId={}, orderId={}",
                    envelope.eventId(), payload.orderId());
            return;
        }

        // FIXME: [#929][#1101] HTTP 제거 후 RestoreProductInventoryUseCase.restore() 활성화
        //  현재 듀얼 라이트 기간: CancelPaymentService.restorePartialCancelInventory()에서 동기로 부분 재고 복구 처리 중
        //  멱등성 보강(#1210) 완료 후 활성화
        //  List<OrderProduct> cancelOrderProducts = convertToOrderProducts(cancelProducts);
        //  restoreProductInventoryUseCase.restore(cancelOrderProducts, "Kafka 부분 취소 재고 복구");

        log.info("부분 취소 재고 복구 이벤트 검증 완료 (듀얼 라이트). orderId={}, cancelProducts={}건",
                payload.orderId(), cancelProducts.size());
    }
}
