package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.UserSignupCompletedEvent;
import com.personal.marketnote.reward.exception.DuplicateUserPointException;
import com.personal.marketnote.reward.port.in.command.point.RegisterUserPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.RegisterUserPointUseCase;
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
class UserSignupCompletedRewardConsumerTest {
    @InjectMocks
    private UserSignupCompletedRewardConsumer userSignupCompletedRewardConsumer;

    @Mock
    private RegisterUserPointUseCase registerUserPointUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long userId, String userKey) {
        UserSignupCompletedEvent event = new UserSignupCompletedEvent(userId, userKey);
        EventEnvelope<UserSignupCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "user.user.signup-completed", "user-service",
                LocalDateTime.of(2026, 3, 2, 10, 0), event
        );
        return new ConsumerRecord<>("user.user.signup-completed", 0, 0L, "key-1", envelope);
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 포인트 등록 UseCase를 호출하고 acknowledge한다")
    void handleUserSignupCompletedEvent_success_registersPointAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "user-key-123");

        // when
        userSignupCompletedRewardConsumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<RegisterUserPointCommand> captor = ArgumentCaptor.forClass(RegisterUserPointCommand.class);
        verify(registerUserPointUseCase).register(captor.capture());

        RegisterUserPointCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(1L);
        assertThat(command.userKey()).isEqualTo("user-key-123");

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("userId가 null이면 포인트 등록을 호출하지 않고 acknowledge한다")
    void handleUserSignupCompletedEvent_nullUserId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, "user-key-123");

        // when
        userSignupCompletedRewardConsumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(registerUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("userKey가 null이면 포인트 등록을 호출하지 않고 acknowledge한다")
    void handleUserSignupCompletedEvent_nullUserKey_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, null);

        // when
        userSignupCompletedRewardConsumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(registerUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleUserSignupCompletedEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "user.user.signup-completed", 0, 0L, "1", null
        );

        // when
        userSignupCompletedRewardConsumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(registerUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handleUserSignupCompletedEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        UserSignupCompletedEvent event = new UserSignupCompletedEvent(1L, "user-key-123");
        EventEnvelope<UserSignupCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "user-service",
                LocalDateTime.of(2026, 3, 2, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "user.user.signup-completed", 0, 0L, "key-1", envelope
        );

        // when
        userSignupCompletedRewardConsumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(registerUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("userId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleUserSignupCompletedEvent_zeroUserId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, "user-key-123");

        // when
        userSignupCompletedRewardConsumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(registerUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("userId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleUserSignupCompletedEvent_negativeUserId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, "user-key-123");

        // when
        userSignupCompletedRewardConsumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(registerUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이미 포인트가 존재하면 예외를 무시하고 acknowledge한다")
    void handleUserSignupCompletedEvent_duplicateUserPoint_acknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "user-key-123");
        doThrow(new DuplicateUserPointException(1L))
                .when(registerUserPointUseCase).register(any(RegisterUserPointCommand.class));

        // when
        userSignupCompletedRewardConsumer.handleUserSignupCompletedEvent(record, acknowledgment);

        // then
        verify(registerUserPointUseCase).register(any(RegisterUserPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예기치 않은 예외 발생 시 acknowledge하지 않고 예외를 전파한다")
    void handleUserSignupCompletedEvent_unexpectedException_propagatesWithoutAcknowledge() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "user-key-123");
        doThrow(new RuntimeException("DB 연결 실패"))
                .when(registerUserPointUseCase).register(any(RegisterUserPointCommand.class));

        // expect
        assertThatThrownBy(() ->
                userSignupCompletedRewardConsumer.handleUserSignupCompletedEvent(record, acknowledgment)
        ).isInstanceOf(RuntimeException.class);

        verifyNoInteractions(acknowledgment);
    }
}
