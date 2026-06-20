package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.UserSignupCompletedEvent;
import com.personal.marketnote.notification.domain.preference.DuplicateNotificationPreferenceException;
import com.personal.marketnote.notification.port.in.command.InitializeNotificationPreferenceCommand;
import com.personal.marketnote.notification.port.in.usecase.preference.InitializeNotificationPreferenceUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSignupCompletedNotificationPreferenceConsumer {

    private final InitializeNotificationPreferenceUseCase initializeNotificationPreferenceUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.USER_SIGNUP_COMPLETED,
            groupId = "notification-preference"
    )
    public void handleUserSignupCompletedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.USER_SIGNUP_COMPLETED)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            UserSignupCompletedEvent payload = envelope.getPayloadAs(UserSignupCompletedEvent.class, objectMapper);

            log.info("회원가입 완료 이벤트 수신 (알림 수신 설정 초기화). eventId={}, userId={}",
                    envelope.eventId(), payload.userId());

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("userId", payload.userId()))) {
                acknowledgment.acknowledge();
                return;
            }

            InitializeNotificationPreferenceCommand command = new InitializeNotificationPreferenceCommand(payload.userId());
            initializeNotificationPreferenceUseCase.initializeNotificationPreference(command);

            log.info("Kafka 이벤트로 알림 수신 설정 초기화 완료. userId={}", payload.userId());
        } catch (DuplicateNotificationPreferenceException e) {
            log.info("알림 수신 설정이 이미 존재합니다 (멱등 처리). eventId={}, key={}",
                    envelope.eventId(), record.key());
        }

        acknowledgment.acknowledge();
    }
}
