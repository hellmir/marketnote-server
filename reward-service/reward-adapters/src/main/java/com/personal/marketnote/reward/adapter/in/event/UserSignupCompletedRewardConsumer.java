package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.UserSignupCompletedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.exception.DuplicateUserPointException;
import com.personal.marketnote.reward.port.in.command.point.RegisterUserPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.RegisterUserPointUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSignupCompletedRewardConsumer {
    private final RegisterUserPointUseCase registerUserPointUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.USER_SIGNUP_COMPLETED,
            groupId = "reward-service"
    )
    public void handleUserSignupCompletedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        try {
            UserSignupCompletedEvent payload = envelope.getPayloadAs(UserSignupCompletedEvent.class, objectMapper);

            log.info("회원가입 완료 이벤트 수신. eventId={}, userId={}, userKey={}",
                    envelope.eventId(), payload.userId(), payload.userKey());

            if (FormatValidator.hasNoValue(payload.userId()) || FormatValidator.hasNoValue(payload.userKey())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, userId={}, userKey={}",
                        envelope.eventId(), payload.userId(), payload.userKey());
                acknowledgment.acknowledge();
                return;
            }

            RegisterUserPointCommand command = RegisterUserPointCommand.of(
                    payload.userId(), payload.userKey()
            );
            registerUserPointUseCase.register(command);

            log.info("Kafka 이벤트로 포인트 초기화 완료. userId={}", payload.userId());
        } catch (DuplicateUserPointException e) {
            log.info("포인트가 이미 존재합니다 (듀얼 라이트 중복). eventId={}, key={}",
                    envelope.eventId(), record.key());
        }
        // 그 외 예외는 DefaultErrorHandler가 재시도 + DLT로 처리

        acknowledgment.acknowledge();
    }
}
