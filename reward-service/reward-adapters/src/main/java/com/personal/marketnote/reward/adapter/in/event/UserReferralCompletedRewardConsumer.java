package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.UserReferralCompletedEvent;
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

import static com.personal.marketnote.common.utility.AccrualPointAmountConstant.REFERRED_USER_POINT_AMOUNT;
import static com.personal.marketnote.common.utility.AccrualPointAmountConstant.REFERRER_USER_POINT_AMOUNT;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserReferralCompletedRewardConsumer {
    private final ModifyUserPointUseCase modifyUserPointUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.USER_REFERRAL_COMPLETED,
            groupId = "reward-service"
    )
    public void handleUserReferralCompletedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.USER_REFERRAL_COMPLETED)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            UserReferralCompletedEvent payload = envelope.getPayloadAs(
                    UserReferralCompletedEvent.class, objectMapper
            );

            log.info("추천코드 등록 완료 이벤트 수신. eventId={}, requestUserId={}, referredUserId={}",
                    envelope.eventId(), payload.requestUserId(), payload.referredUserId());

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("requestUserId", payload.requestUserId()),
                    EventPayloadValidator.id("referredUserId", payload.referredUserId()))) {
                acknowledgment.acknowledge();
                return;
            }

            accrueReferrerPointIdempotent(envelope.eventId(), payload.referredUserId(), payload.requestUserId());
            accrueReferredPointIdempotent(envelope.eventId(), payload.requestUserId(), payload.referredUserId());

            log.info("Kafka 이벤트로 추천 포인트 적립 완료. requestUserId={}, referredUserId={}",
                    payload.requestUserId(), payload.referredUserId());
        } catch (Exception e) {
            // 예외 전파 → DefaultErrorHandler가 재시도 + DLT로 처리 (acknowledge 하지 않음)
            log.error("추천 포인트 적립 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }

    private void accrueReferrerPointIdempotent(String eventId, Long referredUserId, Long requestUserId) {
        try {
            accrueReferrerPoint(referredUserId, requestUserId);
        } catch (DuplicateUserPointHistoryException e) {
            log.info("이미 처리된 추천인 포인트 적립 이벤트 (멱등 처리). eventId={}, message={}",
                    eventId, e.getMessage());
        }
    }

    private void accrueReferredPointIdempotent(String eventId, Long requestUserId, Long referredUserId) {
        try {
            accrueReferredPoint(requestUserId, referredUserId);
        } catch (DuplicateUserPointHistoryException e) {
            log.info("이미 처리된 피추천인 포인트 적립 이벤트 (멱등 처리). eventId={}, message={}",
                    eventId, e.getMessage());
        }
    }

    private void accrueReferrerPoint(Long referredUserId, Long requestUserId) {
        ModifyUserPointCommand referrerCommand = ModifyUserPointCommand.builder()
                .userId(referredUserId)
                .changeType(UserPointChangeType.ACCRUAL)
                .amount((long) REFERRER_USER_POINT_AMOUNT)
                .sourceType(UserPointSourceType.USER)
                .sourceId(requestUserId)
                .reason("추천인 코드 등록 적립")
                .build();
        modifyUserPointUseCase.modify(referrerCommand);
    }

    private void accrueReferredPoint(Long requestUserId, Long referredUserId) {
        ModifyUserPointCommand referredCommand = ModifyUserPointCommand.builder()
                .userId(requestUserId)
                .changeType(UserPointChangeType.ACCRUAL)
                .amount((long) REFERRED_USER_POINT_AMOUNT)
                .sourceType(UserPointSourceType.USER)
                .sourceId(referredUserId)
                .reason("신규 회원 초대 적립")
                .build();
        modifyUserPointUseCase.modify(referredCommand);
    }
}
