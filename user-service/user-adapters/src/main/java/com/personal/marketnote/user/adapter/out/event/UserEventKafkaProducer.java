package com.personal.marketnote.user.adapter.out.event;

import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.UserReferralCompletedEvent;
import com.personal.marketnote.common.kafka.event.UserSignupCompletedEvent;
import com.personal.marketnote.user.port.out.event.PublishUserEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Clock;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class UserEventKafkaProducer implements PublishUserEventPort {
    private static final String SOURCE = "user-service";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Clock clock;

    @Override
    public void publishUserSignupCompletedEvent(Long userId, String userKey) {
        UserSignupCompletedEvent payload = new UserSignupCompletedEvent(userId, userKey);
        EventEnvelope<UserSignupCompletedEvent> envelope = EventEnvelope.of(
                KafkaTopicConstants.USER_SIGNUP_COMPLETED,
                SOURCE,
                payload,
                clock
        );

        // TODO: Kafka 단독 전환 시 발행 실패 처리 보강 필요 (Outbox 패턴 또는 동기 전환)
        kafkaTemplate.send(KafkaTopicConstants.USER_SIGNUP_COMPLETED, userId.toString(), envelope)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka 이벤트 발행 실패. topic={}, userId={}, userKey={}",
                                KafkaTopicConstants.USER_SIGNUP_COMPLETED, userId, userKey, ex);
                    } else {
                        log.info("Kafka 이벤트 발행 성공. topic={}, userId={}, offset={}",
                                KafkaTopicConstants.USER_SIGNUP_COMPLETED, userId,
                                result.getRecordMetadata().offset());
                    }
                });
    }

    @Override
    public void publishUserReferralCompletedEvent(Long requestUserId, Long referredUserId) {
        UserReferralCompletedEvent payload = new UserReferralCompletedEvent(requestUserId, referredUserId);
        EventEnvelope<UserReferralCompletedEvent> envelope = EventEnvelope.of(
                KafkaTopicConstants.USER_REFERRAL_COMPLETED,
                SOURCE,
                payload,
                clock
        );

        // TODO: Kafka 단독 전환 시 발행 실패 처리 보강 필요 (Outbox 패턴 또는 동기 전환)
        kafkaTemplate.send(KafkaTopicConstants.USER_REFERRAL_COMPLETED, requestUserId.toString(), envelope)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka 이벤트 발행 실패. topic={}, requestUserId={}, referredUserId={}",
                                KafkaTopicConstants.USER_REFERRAL_COMPLETED, requestUserId, referredUserId, ex);
                    } else {
                        log.info("Kafka 이벤트 발행 성공. topic={}, requestUserId={}, offset={}",
                                KafkaTopicConstants.USER_REFERRAL_COMPLETED, requestUserId,
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
