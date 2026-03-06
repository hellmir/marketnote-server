package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.in.command.order.UpdateOrderProductReviewStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.UpdateOrderProductUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ReviewRegisteredEvent;
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
public class ReviewRegisteredCommerceConsumer {
    private final UpdateOrderProductUseCase updateOrderProductUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.REVIEW_REGISTERED,
            groupId = "commerce-service"
    )
    public void handleReviewRegisteredEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        try {
            ReviewRegisteredEvent payload = envelope.getPayloadAs(ReviewRegisteredEvent.class, objectMapper);

            log.info("리뷰 등록 이벤트 수신. eventId={}, orderId={}, pricePolicyId={}",
                    envelope.eventId(), payload.orderId(), payload.pricePolicyId());

            if (FormatValidator.hasNoValue(payload.orderId()) || FormatValidator.hasNoValue(payload.pricePolicyId())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, orderId={}, pricePolicyId={}",
                        envelope.eventId(), payload.orderId(), payload.pricePolicyId());
                acknowledgment.acknowledge();
                return;
            }

            UpdateOrderProductReviewStatusCommand command = UpdateOrderProductReviewStatusCommand.of(
                    payload.orderId(), payload.pricePolicyId(), true
            );
            updateOrderProductUseCase.updateReviewStatus(command);

            log.info("Kafka 이벤트로 주문상품 리뷰 상태 업데이트 완료. orderId={}, pricePolicyId={}",
                    payload.orderId(), payload.pricePolicyId());
        } catch (Exception e) {
            log.error("주문상품 리뷰 상태 업데이트 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }
}
