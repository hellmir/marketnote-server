package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.UserReferralCompletedEvent;
import com.personal.marketnote.reward.domain.point.ReferralBonusTier;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.GetReferralStatusUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyUserPointUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.personal.marketnote.common.utility.AccrualPointAmountConstant.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserReferralCompletedRewardConsumer {
    private final ModifyUserPointUseCase modifyUserPointUseCase;
    private final GetReferralStatusUseCase getReferralStatusUseCase;
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

            Long referrerId = payload.referredUserId();
            Long referredId = payload.requestUserId();

            accrueReferrerPointIfEligible(envelope.eventId(), referrerId, referredId);
            accrueReferredPointIdempotent(envelope.eventId(), referredId, referrerId);

            log.info("Kafka 이벤트로 추천 포인트 적립 완료. requestUserId={}, referredUserId={}",
                    payload.requestUserId(), payload.referredUserId());
        } catch (Exception e) {
            log.error("추천 포인트 적립 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }

    private void accrueReferrerPointIfEligible(String eventId, Long referrerId, Long referredId) {
        long referralCount = getReferralStatusUseCase.countCompletedReferrals(referrerId);

        if (ReferralBonusTier.isMaxReached(referralCount)) {
            log.info("최대 초대 수 도달, 추천인 포인트 적립 생략. eventId={}, referrerId={}, count={}",
                    eventId, referrerId, referralCount);
            return;
        }

        accrueReferrerPointIdempotent(eventId, referrerId, referredId);
    }

    private void accrueReferrerPointIdempotent(String eventId, Long referrerId, Long referredId) {
        try {
            accrueReferrerPoint(referrerId, referredId);
        } catch (DuplicateUserPointHistoryException e) {
            log.info("이미 처리된 추천인 포인트 적립 이벤트 (멱등 처리). eventId={}, message={}",
                    eventId, e.getMessage());
        }
    }

    private void accrueReferredPointIdempotent(String eventId, Long referredId, Long referrerId) {
        try {
            accrueReferredPoint(referredId, referrerId);
        } catch (DuplicateUserPointHistoryException e) {
            log.info("이미 처리된 피추천인 포인트 적립 이벤트 (멱등 처리). eventId={}, message={}",
                    eventId, e.getMessage());
        }
    }

    private void accrueReferrerPoint(Long referrerId, Long referredId) {
        ModifyUserPointCommand referrerCommand = ModifyUserPointCommand.builder()
                .userId(referrerId)
                .changeType(UserPointChangeType.ACCRUAL)
                .amount((long) REFERRER_USER_POINT_AMOUNT)
                .sourceType(UserPointSourceType.USER)
                .sourceId(referredId)
                .reason(REFERRER_POINT_REASON)
                .build();
        modifyUserPointUseCase.modify(referrerCommand);
    }

    private void accrueReferredPoint(Long referredId, Long referrerId) {
        ModifyUserPointCommand referredCommand = ModifyUserPointCommand.builder()
                .userId(referredId)
                .changeType(UserPointChangeType.ACCRUAL)
                .amount((long) REFERRED_USER_POINT_AMOUNT)
                .sourceType(UserPointSourceType.USER)
                .sourceId(referrerId)
                .reason(REFERRED_POINT_REASON)
                .build();
        modifyUserPointUseCase.modify(referredCommand);
    }
}
