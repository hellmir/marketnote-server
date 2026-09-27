package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.domain.exception.accessdenied.LoginFailedException;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.authentication.Role;
import com.personal.marketnote.user.domain.user.LoginHistory;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.exception.UserNotActiveException;
import com.personal.marketnote.user.port.in.command.SignInCommand;
import com.personal.marketnote.user.port.in.result.SignInResult;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.out.user.SaveLoginHistoryPort;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignInUseCaseTest {
    @Mock
    private GetUserUseCase getUserUseCase;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SaveLoginHistoryPort saveLoginHistoryPort;

    @InjectMocks
    private SignInService signInService;

    @Test
    @DisplayName("네이티브 로그인 후, 로그인 내역을 저장하고 결과를 반환한다")
    void signIn_nativeUser_success() {
        // given
        String email = "user@test.com";
        String password = "password1!";
        String ipAddress = "127.0.0.1";
        SignInCommand command = SignInCommand.of(email, password);

        User user = mockUserForResult(1L, true, Role.getBuyer());
        when(getUserUseCase.getAllStatusUser(email)).thenReturn(user);
        when(user.isValidPassword(passwordEncoder, password)).thenReturn(true);

        // when
        SignInResult result = signInService.signIn(command, AuthVendor.NATIVE, null, ipAddress);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.roleId()).isEqualTo("ROLE_BUYER");
        assertThat(result.isRequiredTermsAgreed()).isTrue();
        assertLoginHistorySaved(user, AuthVendor.NATIVE, ipAddress);
    }

    @Test
    @DisplayName("소셜 로그인 후, OIDC ID로 조회해 로그인 내역을 저장한다")
    void signIn_socialUser_success() {
        // given
        String email = "user@test.com";
        String oidcId = "oidc-123";
        String ipAddress = "192.168.0.1";
        SignInCommand command = SignInCommand.of(email, "password1!");

        User user = mockUserForResult(2L, false, Role.getBuyer());
        when(getUserUseCase.getAllStatusUser(AuthVendor.KAKAO, oidcId)).thenReturn(user);

        // when
        SignInResult result = signInService.signIn(command, AuthVendor.KAKAO, oidcId, ipAddress);

        // then
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.roleId()).isEqualTo("ROLE_BUYER");
        assertThat(result.isRequiredTermsAgreed()).isFalse();
        assertLoginHistorySaved(user, AuthVendor.KAKAO, ipAddress);
        verify(getUserUseCase, never()).getAllStatusUser(anyString());
        verify(user, never()).isValidPassword(any(PasswordEncoder.class), anyString());
    }

    @Test
    @DisplayName("네이티브 로그인 시 사용자가 비활성 상태인 경우 예외를 던진다")
    void signIn_nativeUser_inactive_throws() {
        // given
        String email = "user@test.com";
        String password = "password1!";
        SignInCommand command = SignInCommand.of(email, password);

        User user = mock(User.class);
        when(getUserUseCase.getAllStatusUser(email)).thenReturn(user);
        when(user.isValidPassword(passwordEncoder, password)).thenReturn(true);
        when(user.isActive()).thenReturn(false);

        // expect
        assertThatThrownBy(() -> signInService.signIn(command, AuthVendor.NATIVE, null, "127.0.0.1"))
                .isInstanceOf(UserNotActiveException.class);

        verifyNoInteractions(saveLoginHistoryPort);
    }

    @Test
    @DisplayName("소셜 로그인 시 사용자가 비활성 상태인 경우 예외를 던진다")
    void signIn_socialUser_inactive_throws() {
        // given
        String oidcId = "oidc-123";
        SignInCommand command = SignInCommand.of(null, null);

        User user = mock(User.class);
        when(getUserUseCase.getAllStatusUser(AuthVendor.GOOGLE, oidcId)).thenReturn(user);
        when(user.isActive()).thenReturn(false);

        // expect
        assertThatThrownBy(() -> signInService.signIn(command, AuthVendor.GOOGLE, oidcId, "127.0.0.1"))
                .isInstanceOf(UserNotActiveException.class);

        verifyNoInteractions(saveLoginHistoryPort);
    }

    @Test
    @DisplayName("이메일이 존재하지 않으면 로그인 실패 예외를 던진다")
    void signIn_nativeUser_notFound_throws() {
        // given
        String email = "missing@test.com";
        SignInCommand command = SignInCommand.of(email, "password1!");

        when(getUserUseCase.getAllStatusUser(email)).thenThrow(new UserNotFoundException("not found"));

        // expect
        assertThatThrownBy(() -> signInService.signIn(command, AuthVendor.NATIVE, null, "127.0.0.1"))
                .isInstanceOf(LoginFailedException.class);

        verifyNoInteractions(saveLoginHistoryPort);
    }

    @Test
    @DisplayName("전송된 비밀번호가 틀리면 로그인 실패 예외를 던진다")
    void signIn_nativeUser_invalidPassword_throws() {
        // given
        String email = "user@test.com";
        String password = "wrong-password";
        SignInCommand command = SignInCommand.of(email, password);

        User user = mock(User.class);
        when(getUserUseCase.getAllStatusUser(email)).thenReturn(user);
        when(user.isValidPassword(passwordEncoder, password)).thenReturn(false);

        // expect
        assertThatThrownBy(() -> signInService.signIn(command, AuthVendor.NATIVE, null, "127.0.0.1"))
                .isInstanceOf(LoginFailedException.class);

        verifyNoInteractions(saveLoginHistoryPort);
    }

    @Test
    @DisplayName("이메일과 OIDC ID가 모두 존재하지 않으면 로그인 실패 예외를 던진다")
    void signIn_missingEmailAndOidc_throws() {
        // given
        SignInCommand command = SignInCommand.of("  ", null);

        // expect
        assertThatThrownBy(() -> signInService.signIn(command, AuthVendor.NATIVE, null, "127.0.0.1"))
                .isInstanceOf(LoginFailedException.class);

        verifyNoInteractions(getUserUseCase, saveLoginHistoryPort);
    }

    @Test
    @DisplayName("OIDC ID가 비어 있으면 네이티브 로그인으로 처리한다")
    void signIn_socialWithoutOidc_fallsBackToEmail() {
        // given
        String email = "user@test.com";
        String password = "password1!";
        String ipAddress = "10.0.0.1";
        String oidcId = " ";
        SignInCommand command = SignInCommand.of(email, password);

        User user = mockUserForResult(3L, true, Role.getBuyer());
        when(getUserUseCase.getAllStatusUser(email)).thenReturn(user);
        when(user.isValidPassword(passwordEncoder, password)).thenReturn(true);

        // when
        SignInResult result = signInService.signIn(command, AuthVendor.KAKAO, oidcId, ipAddress);

        // then
        assertThat(result.id()).isEqualTo(3L);
        assertThat(result.roleId()).isEqualTo("ROLE_BUYER");
        assertThat(result.isRequiredTermsAgreed()).isTrue();
        assertLoginHistorySaved(user, AuthVendor.KAKAO, ipAddress);
        verify(getUserUseCase, never()).getAllStatusUser(any(AuthVendor.class), anyString());
    }

    @Test
    @DisplayName("소셜 로그인 시 사용자를 찾지 못하면 예외가 전파된다")
    void signIn_socialUser_notFound_propagates() {
        // given
        String oidcId = "missing-oidc";
        SignInCommand command = SignInCommand.of(null, null);

        when(getUserUseCase.getAllStatusUser(AuthVendor.APPLE, oidcId))
                .thenThrow(new UserNotFoundException("not found"));

        // expect
        assertThatThrownBy(() -> signInService.signIn(command, AuthVendor.APPLE, oidcId, "127.0.0.1"))
                .isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(saveLoginHistoryPort);
    }

    private User mockUserForResult(Long id, boolean requiredTermsAgreed, Role role) {
        User user = mock(User.class);
        when(user.isActive()).thenReturn(true);
        when(user.getId()).thenReturn(id);
        when(user.getRole()).thenReturn(role);
        when(user.isRequiredTermsAgreed()).thenReturn(requiredTermsAgreed);
        return user;
    }

    private void assertLoginHistorySaved(User user, AuthVendor authVendor, String ipAddress) {
        ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);

        verify(saveLoginHistoryPort).saveLoginHistory(captor.capture());

        LoginHistory loginHistory = captor.getValue();
        assertThat(loginHistory.getUser()).isEqualTo(user);
        assertThat(loginHistory.getAuthVendor()).isEqualTo(authVendor);
        assertThat(loginHistory.getIpAddress().getValue()).isEqualTo(ipAddress);
    }
}
