package com.personal.marketnote.user.service.user;

import com.personal.marketnote.user.domain.authentication.Role;
import com.personal.marketnote.user.domain.user.FingerprintSource;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.port.in.command.SignInCommand;
import com.personal.marketnote.user.port.in.event.UserFingerprintSavedEvent;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.out.authentication.GenerateTokenPort;
import com.personal.marketnote.user.port.out.authentication.SaveRefreshTokenPort;
import com.personal.marketnote.user.port.out.user.SaveLoginHistoryPort;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignInFingerprintUseCaseTest {
    @Mock
    private GetUserUseCase getUserUseCase;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SaveLoginHistoryPort saveLoginHistoryPort;
    @Mock
    private GenerateTokenPort generateTokenPort;
    @Mock
    private SaveRefreshTokenPort saveRefreshTokenPort;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private SignInService signInService;

    @BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(signInService, "refreshTokenTtlMillis", 1209600000L);
    }

    @Test
    @DisplayName("일반 로그인 시 fingerprint가 있으면 UserFingerprintSavedEvent가 발행된다")
    void signIn_withFingerprint_publishesEvent() {
        // given
        String email = "user@test.com";
        String password = "password1!";
        String fingerprint = "fp-login-abc";
        SignInCommand command = SignInCommand.of(email, password, fingerprint);

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.isActive()).thenReturn(true);
        when(user.getRole()).thenReturn(Role.getBuyer());
        when(user.isValidPassword(passwordEncoder, password)).thenReturn(true);
        when(user.isRequiredTermsAgreed()).thenReturn(true);
        when(getUserUseCase.getAllStatusUser(email)).thenReturn(user);

        when(generateTokenPort.generateAccessToken(any(), any(), any(), any())).thenReturn("access-token");
        when(generateTokenPort.generateRefreshToken(any(), any(), any(), any())).thenReturn("refresh-token");

        // when
        signInService.signIn(command, AuthVendor.NATIVE, null, "127.0.0.1");

        // then
        ArgumentCaptor<UserFingerprintSavedEvent> captor = ArgumentCaptor.forClass(UserFingerprintSavedEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        UserFingerprintSavedEvent event = captor.getValue();
        assertThat(event.userId()).isEqualTo(1L);
        assertThat(event.fingerprint()).isEqualTo("fp-login-abc");
        assertThat(event.source()).isEqualTo(FingerprintSource.LOGIN);
    }

    @Test
    @DisplayName("소셜 로그인 시 fingerprint가 있으면 UserFingerprintSavedEvent가 발행된다")
    void signIn_social_withFingerprint_publishesEvent() {
        // given
        String fingerprint = "fp-social-xyz";
        SignInCommand command = SignInCommand.of(null, null, fingerprint);

        User user = mock(User.class);
        when(user.getId()).thenReturn(2L);
        when(user.isActive()).thenReturn(true);
        when(user.getRole()).thenReturn(Role.getBuyer());
        when(user.isRequiredTermsAgreed()).thenReturn(true);
        when(getUserUseCase.getAllStatusUser(AuthVendor.KAKAO, "oidc-123")).thenReturn(user);

        when(generateTokenPort.generateAccessToken(any(), any(), any(), any())).thenReturn("access-token");
        when(generateTokenPort.generateRefreshToken(any(), any(), any(), any())).thenReturn("refresh-token");

        // when
        signInService.signIn(command, AuthVendor.KAKAO, "oidc-123", "127.0.0.1");

        // then
        ArgumentCaptor<UserFingerprintSavedEvent> captor = ArgumentCaptor.forClass(UserFingerprintSavedEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        UserFingerprintSavedEvent event = captor.getValue();
        assertThat(event.userId()).isEqualTo(2L);
        assertThat(event.fingerprint()).isEqualTo("fp-social-xyz");
        assertThat(event.source()).isEqualTo(FingerprintSource.LOGIN);
    }

    @Test
    @DisplayName("로그인 시 fingerprint가 null이면 이벤트가 발행되지 않는다")
    void signIn_fingerprintNull_noEvent() {
        // given
        String email = "user@test.com";
        String password = "password1!";
        SignInCommand command = SignInCommand.of(email, password);

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.isActive()).thenReturn(true);
        when(user.getRole()).thenReturn(Role.getBuyer());
        when(user.isValidPassword(passwordEncoder, password)).thenReturn(true);
        when(user.isRequiredTermsAgreed()).thenReturn(true);
        when(getUserUseCase.getAllStatusUser(email)).thenReturn(user);

        when(generateTokenPort.generateAccessToken(any(), any(), any(), any())).thenReturn("access-token");
        when(generateTokenPort.generateRefreshToken(any(), any(), any(), any())).thenReturn("refresh-token");

        // when
        signInService.signIn(command, AuthVendor.NATIVE, null, "127.0.0.1");

        // then
        verify(applicationEventPublisher, never()).publishEvent(any(UserFingerprintSavedEvent.class));
    }
}
