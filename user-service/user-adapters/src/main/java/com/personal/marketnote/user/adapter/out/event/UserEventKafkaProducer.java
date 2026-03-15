package com.personal.marketnote.user.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.UserReferralCompletedEvent;
import com.personal.marketnote.common.kafka.event.UserSignupCompletedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.user.port.out.event.PublishUserEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class UserEventKafkaProducer implements PublishUserEventPort {
    private static final String SOURCE = "user-service";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publishUserSignupCompletedEvent(Long userId, String userKey) {
        UserSignupCompletedEvent payload = new UserSignupCompletedEvent(userId, userKey);
        String topic = KafkaTopicConstants.USER_SIGNUP_COMPLETED;
        EventEnvelope<UserSignupCompletedEvent> envelope = EventEnvelope.of(topic, SOURCE, payload, clock);

        saveToOutbox(envelope, topic, userId.toString());
    }

    @Override
    public void publishUserReferralCompletedEvent(Long requestUserId, Long referredUserId) {
        UserReferralCompletedEvent payload = new UserReferralCompletedEvent(requestUserId, referredUserId);
        String topic = KafkaTopicConstants.USER_REFERRAL_COMPLETED;
        EventEnvelope<UserReferralCompletedEvent> envelope = EventEnvelope.of(topic, SOURCE, payload, clock);

        saveToOutbox(envelope, topic, requestUserId.toString());
    }

    private <T> void saveToOutbox(EventEnvelope<T> envelope, String topic, String partitionKey) {
        try {
            String payloadJson = objectMapper.writeValueAsString(envelope);
            OutboxEvent outboxEvent = OutboxEvent.of(
                    envelope.eventId(), topic, partitionKey,
                    envelope.eventType(), SOURCE, payloadJson, clock
            );
            saveOutboxEventPort.save(outboxEvent);
            log.info("Outbox 이벤트 저장. topic={}, partitionKey={}, eventId={}",
                    topic, partitionKey, envelope.eventId());
        } catch (Exception e) {
            log.error("Outbox 이벤트 저장 실패. topic={}, partitionKey={}, error={}",
                    topic, partitionKey, e.getMessage(), e);
        }
    }
}
