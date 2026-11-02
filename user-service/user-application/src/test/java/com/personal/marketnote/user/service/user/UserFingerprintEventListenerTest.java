package com.personal.marketnote.user.service.user;

import com.personal.marketnote.user.domain.user.FingerprintSource;
import com.personal.marketnote.user.domain.user.UserFingerprint;
import com.personal.marketnote.user.port.in.event.UserFingerprintSavedEvent;
import com.personal.marketnote.user.port.out.user.SaveUserFingerprintPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserFingerprintEventListenerTest {
    @Mock
    private SaveUserFingerprintPort saveUserFingerprintPort;

    @Mock
    private Clock clock;

    @InjectMocks
    private UserFingerprintEventListener userFingerprintEventListener;

    @Test
    @DisplayName("이벤트 수신 시 SaveUserFingerprintPort.save()가 올바른 인자로 호출된다")
    void handleEvent_withFingerprint_savesCalled() {
        // given
        Instant fixedInstant = Instant.parse("2026-10-11T10:00:00Z");
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));

        UserFingerprintSavedEvent event = new UserFingerprintSavedEvent(
                1L, "fp-abc-123", FingerprintSource.SIGNUP
        );

        // when
        userFingerprintEventListener.handleUserFingerprintSavedEvent(event);

        // then
        ArgumentCaptor<UserFingerprint> captor = ArgumentCaptor.forClass(UserFingerprint.class);
        verify(saveUserFingerprintPort).save(captor.capture());

        UserFingerprint saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getFingerprint()).isEqualTo("fp-abc-123");
        assertThat(saved.getSource()).isEqualTo(FingerprintSource.SIGNUP);
        assertThat(saved.getFirstSeenAt()).isNotNull();
        assertThat(saved.getLastSeenAt()).isEqualTo(saved.getFirstSeenAt());
    }

    @Test
    @DisplayName("fingerprint가 null인 이벤트 수신 시 save가 호출되지 않는다")
    void handleEvent_fingerprintNull_saveNotCalled() {
        // given
        UserFingerprintSavedEvent event = new UserFingerprintSavedEvent(
                1L, null, FingerprintSource.SIGNUP
        );

        // when
        userFingerprintEventListener.handleUserFingerprintSavedEvent(event);

        // then
        verifyNoInteractions(saveUserFingerprintPort);
    }

    @Test
    @DisplayName("fingerprint가 빈 문자열인 이벤트 수신 시 save가 호출되지 않는다")
    void handleEvent_fingerprintEmpty_saveNotCalled() {
        // given
        UserFingerprintSavedEvent event = new UserFingerprintSavedEvent(
                1L, "  ", FingerprintSource.SIGNUP
        );

        // when
        userFingerprintEventListener.handleUserFingerprintSavedEvent(event);

        // then
        verifyNoInteractions(saveUserFingerprintPort);
    }

    @Test
    @DisplayName("save 중 예외 발생 시 예외가 전파되지 않는다")
    void handleEvent_saveThrowsException_doesNotPropagate() {
        // given
        Instant fixedInstant = Instant.parse("2026-10-11T10:00:00Z");
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));

        UserFingerprintSavedEvent event = new UserFingerprintSavedEvent(
                1L, "fp-abc-123", FingerprintSource.SIGNUP
        );

        doThrow(new RuntimeException("DB error")).when(saveUserFingerprintPort).save(any());

        // when - 예외가 전파되지 않는다
        userFingerprintEventListener.handleUserFingerprintSavedEvent(event);

        // then
        verify(saveUserFingerprintPort).save(any());
    }
}
