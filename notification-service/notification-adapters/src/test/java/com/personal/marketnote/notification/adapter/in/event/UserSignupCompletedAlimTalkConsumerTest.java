package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.UserSignupCompletedEvent;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateCodes;
import com.personal.marketnote.notification.port.in.command.SendAligoAlimTalkCommand;
import com.personal.marketnote.notification.port.in.usecase.aligo.SendAligoAlimTalkUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UserSignupCompletedAlimTalkConsumerTest {

    @InjectMocks
    private UserSignupCompletedAlimTalkConsumer consumer;

    @Mock
    private SendAligoAlimTalkUseCase sendAligoAlimTalkUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AligoProperties aligoProperties;

    @Mock
    private Acknowledgment acknowledgment;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(aligoProperties.getSender()).thenReturn("0212345678");
    }

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(
            Long userId, String userKey, String phoneNumber, String userName
    ) {
        UserSignupCompletedEvent event = new UserSignupCompletedEvent(
                userId, userKey, phoneNumber, userName
        );
        EventEnvelope<UserSignupCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "user.user.signup-completed", "user-service",
                LocalDateTime.of(2026, 4, 16, 10, 0), event
        );
        return new ConsumerRecord<>("user.user.signup-completed", 0, 0L,
                String.valueOf(userId), envelope);
    }

    @Test
    @DisplayName("회원가입 완료 이벤트 수신 시 알림톡을 발송하고 acknowledge한다")
    void shouldSendAlimTalkAndAcknowledge() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(
                1L, "user-key-123", "01012345678", "홍길동"
        );

        // when
        consumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<SendAligoAlimTalkCommand> captor = ArgumentCaptor.forClass(SendAligoAlimTalkCommand.class);
        verify(sendAligoAlimTalkUseCase).sendAlimTalk(captor.capture());

        SendAligoAlimTalkCommand command = captor.getValue();
        assertThat(command.templateCode()).isEqualTo(AligoTemplateCodes.USER_SIGNUP);
        assertThat(command.sender()).isEqualTo("0212345678");
        assertThat(command.recipients()).hasSize(1);
        assertThat(command.recipients().get(0).receiver()).isEqualTo("01012345678");
        assertThat(command.recipients().get(0).recvName()).isEqualTo("홍길동");
        assertThat(command.variables()).containsEntry("user_name", "홍길동");
        assertThat(command.failover()).isNull();

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 알림톡을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenEnvelopeIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "user.user.signup-completed", 0, 0L, "1", null
        );

        // when
        consumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendAligoAlimTalkUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 알림톡을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenEventTypeMismatch() {
        // given
        UserSignupCompletedEvent event = new UserSignupCompletedEvent(
                1L, "user-key-123", "01012345678", "홍길동"
        );
        EventEnvelope<UserSignupCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "user-service",
                LocalDateTime.of(2026, 4, 16, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "user.user.signup-completed", 0, 0L, "1", envelope
        );

        // when
        consumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendAligoAlimTalkUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("userId가 null이면 알림톡을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenUserIdIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(
                null, "user-key-123", "01012345678", "홍길동"
        );

        // when
        consumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendAligoAlimTalkUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("phoneNumber가 null이면 알림톡을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenPhoneNumberIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(
                1L, "user-key-123", null, "홍길동"
        );

        // when
        consumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendAligoAlimTalkUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("알림톡 발송 실패 시 예외를 로그하고 acknowledge한다")
    void shouldLogAndAcknowledgeWhenAlimTalkFails() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(
                1L, "user-key-123", "01012345678", "홍길동"
        );
        org.mockito.Mockito.doThrow(new RuntimeException("알리고 API 호출 실패"))
                .when(sendAligoAlimTalkUseCase).sendAlimTalk(org.mockito.ArgumentMatchers.any());

        // when
        consumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }
}
