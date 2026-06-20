package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.UserSignupCompletedEvent;
import com.personal.marketnote.notification.domain.preference.DuplicateNotificationPreferenceException;
import com.personal.marketnote.notification.port.in.command.InitializeNotificationPreferenceCommand;
import com.personal.marketnote.notification.port.in.usecase.preference.InitializeNotificationPreferenceUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSignupCompletedNotificationPreferenceConsumerTest {

    @InjectMocks
    private UserSignupCompletedNotificationPreferenceConsumer consumer;

    @Mock
    private InitializeNotificationPreferenceUseCase initializeNotificationPreferenceUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long userId, String userKey) {
        UserSignupCompletedEvent event = new UserSignupCompletedEvent(userId, userKey);
        EventEnvelope<UserSignupCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "user.user.signup-completed", "user-service",
                LocalDateTime.of(2026, 4, 9, 10, 0), event
        );
        return new ConsumerRecord<>("user.user.signup-completed", 0, 0L, "key-1", envelope);
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 알림 수신 설정 초기화 UseCase를 호출하고 acknowledge한다")
    void shouldInitializePreferenceAndAcknowledge() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "user-key-123");

        // when
        consumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<InitializeNotificationPreferenceCommand> captor = ArgumentCaptor.forClass(InitializeNotificationPreferenceCommand.class);
        verify(initializeNotificationPreferenceUseCase).initializeNotificationPreference(captor.capture());

        InitializeNotificationPreferenceCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(1L);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void shouldSkipWhenEnvelopeIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "user.user.signup-completed", 0, 0L, "1", null
        );

        // when
        consumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(initializeNotificationPreferenceUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void shouldSkipWhenEventTypeMismatch() {
        // given
        UserSignupCompletedEvent event = new UserSignupCompletedEvent(1L, "user-key-123");
        EventEnvelope<UserSignupCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "user-service",
                LocalDateTime.of(2026, 4, 9, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "user.user.signup-completed", 0, 0L, "key-1", envelope
        );

        // when
        consumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(initializeNotificationPreferenceUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("userId가 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void shouldSkipWhenUserIdIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, "user-key-123");

        // when
        consumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(initializeNotificationPreferenceUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("userId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void shouldSkipWhenUserIdIsZero() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, "user-key-123");

        // when
        consumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(initializeNotificationPreferenceUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이미 알림 수신 설정이 존재하면 예외를 무시하고 acknowledge한다")
    void shouldHandleDuplicateIdempotently() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "user-key-123");
        doThrow(new DuplicateNotificationPreferenceException(1L))
                .when(initializeNotificationPreferenceUseCase).initializeNotificationPreference(any(InitializeNotificationPreferenceCommand.class));

        // when
        consumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        verify(initializeNotificationPreferenceUseCase).initializeNotificationPreference(any(InitializeNotificationPreferenceCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예기치 않은 예외 발생 시 acknowledge하지 않고 예외를 전파한다")
    void shouldPropagateUnexpectedException() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "user-key-123");
        doThrow(new RuntimeException("DB 연결 실패"))
                .when(initializeNotificationPreferenceUseCase).initializeNotificationPreference(any(InitializeNotificationPreferenceCommand.class));

        // when & then
        assertThatThrownBy(() ->
                consumer.handleUserSignupCompletedEvent(record, acknowledgment)
        ).isInstanceOf(RuntimeException.class);

        verifyNoInteractions(acknowledgment);
    }
}
