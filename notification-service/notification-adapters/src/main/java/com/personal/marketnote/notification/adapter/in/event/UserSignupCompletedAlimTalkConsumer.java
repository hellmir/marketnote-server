package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.UserSignupCompletedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateCodes;
import com.personal.marketnote.notification.port.in.command.AligoAlimTalkRecipientCommand;
import com.personal.marketnote.notification.port.in.command.SendAligoAlimTalkCommand;
import com.personal.marketnote.notification.port.in.usecase.aligo.SendAligoAlimTalkUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSignupCompletedAlimTalkConsumer {

    private final SendAligoAlimTalkUseCase sendAligoAlimTalkUseCase;
    private final ObjectMapper objectMapper;
    private final AligoProperties aligoProperties;

    @KafkaListener(
            topics = KafkaTopicConstants.USER_SIGNUP_COMPLETED,
            groupId = "notification-alimtalk-user"
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

        UserSignupCompletedEvent payload = envelope.getPayloadAs(UserSignupCompletedEvent.class, objectMapper);

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("userId", payload.userId()))) {
            acknowledgment.acknowledge();
            return;
        }

        if (FormatValidator.hasNoValue(payload.phoneNumber())) {
            log.warn("회원가입 완료 알림톡 발송 생략: 전화번호 없음. eventId={}, userId={}",
                    envelope.eventId(), payload.userId());
            acknowledgment.acknowledge();
            return;
        }

        log.info("회원가입 완료 이벤트 수신 (알림톡 발송). eventId={}, userId={}",
                envelope.eventId(), payload.userId());

        try {
            SendAligoAlimTalkCommand command = new SendAligoAlimTalkCommand(
                    AligoTemplateCodes.USER_SIGNUP,
                    aligoProperties.getSender(),
                    Map.of("user_name", FormatValidator.hasValue(payload.userName())
                            ? payload.userName() : ""),
                    List.of(new AligoAlimTalkRecipientCommand(
                            payload.phoneNumber(), payload.userName()
                    )),
                    null
            );
            sendAligoAlimTalkUseCase.sendAlimTalk(command);

            log.info("회원가입 완료 알림톡 발송 완료. userId={}", payload.userId());
        } catch (Exception e) {
            log.error("회원가입 완료 알림톡 발송 실패. eventId={}, userId={}, error={}",
                    envelope.eventId(), payload.userId(), e.getMessage(), e);
        }

        acknowledgment.acknowledge();
    }
}
