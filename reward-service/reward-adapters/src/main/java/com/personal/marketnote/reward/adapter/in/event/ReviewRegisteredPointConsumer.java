package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ReviewRegisteredEvent;
import com.personal.marketnote.common.utility.AccrualPointAmountConstant;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyUserPointUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewRegisteredPointConsumer {
    private final ModifyUserPointUseCase modifyUserPointUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.REVIEW_REGISTERED,
            groupId = "reward-review-point"
    )
    public void handleReviewRegisteredEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.REVIEW_REGISTERED)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            ReviewRegisteredEvent payload = envelope.getPayloadAs(
                    ReviewRegisteredEvent.class, objectMapper
            );

            log.info("리뷰 등록 이벤트 수신 (포인트 적립). eventId={}, reviewerId={}, reviewId={}",
                    envelope.eventId(), payload.reviewerId(), payload.reviewId());

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("reviewerId", payload.reviewerId()),
                    EventPayloadValidator.id("reviewId", payload.reviewId()))) {
                acknowledgment.acknowledge();
                return;
            }

            accrueReviewPointIdempotent(envelope.eventId(), payload.reviewerId(), payload.reviewId());

            log.info("리뷰 포인트 적립 완료. reviewerId={}, reviewId={}, amount={}",
                    payload.reviewerId(), payload.reviewId(), AccrualPointAmountConstant.REVIEW_POINT_AMOUNT);
        } catch (Exception e) {
            log.error("리뷰 포인트 적립 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }

    private void accrueReviewPointIdempotent(String eventId, Long reviewerId, Long reviewId) {
        try {
            ModifyUserPointCommand command = ModifyUserPointCommand.builder()
                    .userId(reviewerId)
                    .changeType(UserPointChangeType.ACCRUAL)
                    .amount((long) AccrualPointAmountConstant.REVIEW_POINT_AMOUNT)
                    .sourceType(UserPointSourceType.REVIEW)
                    .sourceId(reviewId)
                    .reason(AccrualPointAmountConstant.REVIEW_POINT_REASON)
                    .build();
            modifyUserPointUseCase.modify(command);
        } catch (DuplicateUserPointHistoryException e) {
            log.info("이미 처리된 리뷰 포인트 적립 이벤트 (멱등 처리). eventId={}, reviewerId={}, message={}",
                    eventId, reviewerId, e.getMessage());
        }
    }
}
