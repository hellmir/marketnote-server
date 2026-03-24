package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.domain.exception.illegalargument.novalue.PasswordNoValueException;
import com.personal.marketnote.user.domain.authentication.Role;
import com.personal.marketnote.user.domain.user.LoginHistory;
import com.personal.marketnote.user.domain.user.Terms;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.exception.InvalidVerificationCodeException;
import com.personal.marketnote.user.exception.UserExistsException;
import com.personal.marketnote.user.exception.UserNotActiveException;
import com.personal.marketnote.user.port.in.command.SignUpCommand;
import com.personal.marketnote.user.port.in.result.SignUpResult;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.out.authentication.VerifyCodePort;
import com.personal.marketnote.user.port.out.event.PublishUserEventPort;
import com.personal.marketnote.user.port.out.user.*;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignUpUseCaseTest {
    @Mock
    private GetUserUseCase getUserUseCase;
    @Mock
    private SaveUserPort saveUserPort;
    @Mock
    private FindUserPort findUserPort;
    @Mock
    private FindTermsPort findTermsPort;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UpdateUserPort updateUserPort;
    @Mock
    private VerifyCodePort verifyCodePort;
    @Mock
    private SaveLoginHistoryPort saveLoginHistoryPort;
    @Mock
    private PublishUserEventPort publishUserEventPort;

    @InjectMocks
    private SignUpService signUpService;

    @Test
    @DisplayName("신규 회원 가입 시 포인트 도메인 및 로그인 내역을 생성한다")
    void signUp_newUser_success() {
        // given
        String email = "user@test.com";
        String password = "password1!";
        String verificationCode = "123456";
        String nickname = "tester";
        SignUpCommand command = createCommand(email, password, verificationCode, nickname, "홍길동", null);

        when(findUserPort.existsByEmail(email)).thenReturn(false);
        when(findUserPort.existsByNickname(nickname)).thenReturn(false);
        when(verifyCodePort.verify(email, verificationCode)).thenReturn(true);
        when(findTermsPort.findAll()).thenReturn(List.of(mock(Terms.class)));
        when(passwordEncoder.encode(password)).thenReturn("encoded");

        UUID userKey = UUID.randomUUID();
        User savedUser = mock(User.class);
        when(savedUser.getId()).thenReturn(1L);
        when(savedUser.getUserKey()).thenReturn(userKey);
        when(savedUser.getRole()).thenReturn(Role.getBuyer());
        when(saveUserPort.save(any(User.class))).thenReturn(savedUser);

        // when
        SignUpResult result = signUpService.signUp(command, AuthVendor.NATIVE, null, "127.0.0.1");

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.isNewUser()).isTrue();
        assertThat(result.roleId()).isEqualTo("ROLE_BUYER");

        verify(saveUserPort).save(any(User.class));
        verify(publishUserEventPort).publishUserSignupCompletedEvent(1L, userKey.toString());

        verify(saveLoginHistoryPort).saveLoginHistory(any(LoginHistory.class));
        verify(updateUserPort, never()).update(any());
    }

    @Test
    @DisplayName("기존 회원이 다른 수단으로 가입 요청하는 경우, 로그인 계정 정보만 추가한다")
    void signUp_existingEmail_activeUser_addLoginAccount() {
        // given
        String email = "user@test.com";
        SignUpCommand command = createCommand(email, null, "999999", "tester", "홍길동", null);

        when(findUserPort.existsByEmail(email)).thenReturn(true);
        when(findUserPort.existsByAuthVendorAndOidcId(any(), anyString())).thenReturn(false);
        when(findUserPort.existsByNickname(anyString())).thenReturn(false);
        when(verifyCodePort.verify(eq(email), anyString())).thenReturn(true);

        User existingUser = mock(User.class);
        when(existingUser.isWithdrawn()).thenReturn(false);
        when(existingUser.isActive()).thenReturn(true);
        when(existingUser.getId()).thenReturn(2L);
        when(existingUser.getRole()).thenReturn(Role.getBuyer());

        when(getUserUseCase.getAllStatusUser(email)).thenReturn(existingUser);

        // when
        SignUpResult result = signUpService.signUp(command, AuthVendor.KAKAO, "oidc-123", "127.0.0.1");

        // then
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.isNewUser()).isFalse();

        verify(updateUserPort).update(existingUser);
        verify(saveLoginHistoryPort).saveLoginHistory(any(LoginHistory.class));
        verify(publishUserEventPort, never()).publishUserSignupCompletedEvent(anyLong(), anyString());

        verify(saveUserPort, never()).save(any());
    }

    @Test
    @DisplayName("네이티브 회원이 비밀번호 없이 가입을 시도하면 예외를 던진다")
    void signUp_nativeWithoutPassword_throws() {
        // given
        SignUpCommand command = createCommand("user@test.com", null, "123456", "tester", "홍길동", null);

        // expect
        assertThatThrownBy(() -> signUpService.signUp(command, AuthVendor.NATIVE, null, "127.0.0.1"))
                .isInstanceOf(PasswordNoValueException.class);

        verifyNoInteractions(saveUserPort, updateUserPort, saveLoginHistoryPort, publishUserEventPort, findTermsPort, verifyCodePort, getUserUseCase);
    }

    @Test
    @DisplayName("소셜 회원이 동일한 OIDC ID로 가입을 시도하면 중복 예외를 던진다")
    void signUp_socialDuplicateOidc_throws() {
        // given
        String email = "user@test.com";
        String oidcId = "dup-oidc";
        SignUpCommand command = createCommand(email, null, "123456", "tester", "홍길동", null);

        when(findUserPort.existsByAuthVendorAndOidcId(AuthVendor.GOOGLE, oidcId)).thenReturn(true);

        // expect
        assertThatThrownBy(() -> signUpService.signUp(command, AuthVendor.GOOGLE, oidcId, "127.0.0.1"))
                .isInstanceOf(UserExistsException.class);

        verify(verifyCodePort, never()).verify(anyString(), anyString());
        verifyNoInteractions(saveUserPort, updateUserPort, saveLoginHistoryPort, publishUserEventPort, findTermsPort, getUserUseCase);
    }

    @Test
    @DisplayName("닉네임이 중복되는 경우 회원 가입에 실패한다")
    void signUp_duplicateNickname_throws() {
        // given
        String email = "user@test.com";
        SignUpCommand command = createCommand(email, null, "123456", "dupNick", "홍길동", null);

        when(findUserPort.existsByAuthVendorAndOidcId(any(), anyString())).thenReturn(false);
        when(findUserPort.existsByNickname("dupNick")).thenReturn(true);

        // expect
        assertThatThrownBy(() -> signUpService.signUp(command, AuthVendor.KAKAO, "oidc", "127.0.0.1"))
                .isInstanceOf(UserExistsException.class);

        verify(verifyCodePort, never()).verify(anyString(), anyString());
        verifyNoInteractions(saveUserPort, updateUserPort, saveLoginHistoryPort, publishUserEventPort, findTermsPort, getUserUseCase);
    }

    @Test
    @DisplayName("전화번호가 중복되는 경우 회원 가입에 실패한다")
    void signUp_duplicatePhone_throws() {
        // given
        String email = "user@test.com";
        String phone = "010-1111-2222";
        SignUpCommand command = createCommand(email, null, "123456", "tester", "홍길동", phone);

        when(findUserPort.existsByAuthVendorAndOidcId(any(), anyString())).thenReturn(false);
        when(findUserPort.existsByNickname(anyString())).thenReturn(false);
        when(findUserPort.existsByPhoneNumber(phone)).thenReturn(true);

        // expect
        assertThatThrownBy(() -> signUpService.signUp(command, AuthVendor.KAKAO, "oidc", "127.0.0.1"))
                .isInstanceOf(UserExistsException.class);

        verify(verifyCodePort, never()).verify(anyString(), anyString());
        verifyNoInteractions(saveUserPort, updateUserPort, saveLoginHistoryPort, publishUserEventPort, findTermsPort, getUserUseCase);
    }

    @Test
    @DisplayName("이메일 인증 코드가 잘못된 경우 회원 가입에 실패한다")
    void signUp_invalidVerificationCode_throws() {
        // given
        String email = "user@test.com";
        String verificationCode = "wrong";
        SignUpCommand command = createCommand(email, "pw", verificationCode, "tester", "홍길동", null);

        when(findUserPort.existsByNickname(anyString())).thenReturn(false);
        when(verifyCodePort.verify(email, verificationCode)).thenReturn(false);

        // expect
        assertThatThrownBy(() -> signUpService.signUp(command, AuthVendor.NATIVE, null, "127.0.0.1"))
                .isInstanceOf(InvalidVerificationCodeException.class);

        verify(saveUserPort, never()).save(any());
        verify(publishUserEventPort, never()).publishUserSignupCompletedEvent(anyLong(), anyString());

        verify(saveLoginHistoryPort, never()).saveLoginHistory(any());
    }

    @Test
    @DisplayName("기존 회원이 비활성(계정 정지) 상태인 경우 예외를 던진다")
    void signUp_existingEmail_inactiveUser_throws() {
        // given
        String email = "user@test.com";
        SignUpCommand command = createCommand(email, null, "123456", "tester", "홍길동", null);

        when(findUserPort.existsByEmail(email)).thenReturn(true);
        when(findUserPort.existsByAuthVendorAndOidcId(any(), anyString())).thenReturn(false);
        when(findUserPort.existsByNickname(anyString())).thenReturn(false);
        when(verifyCodePort.verify(eq(email), anyString())).thenReturn(true);

        User inactiveUser = mock(User.class);
        when(inactiveUser.isWithdrawn()).thenReturn(false);
        when(inactiveUser.isActive()).thenReturn(false);
        when(getUserUseCase.getAllStatusUser(email)).thenReturn(inactiveUser);

        // expect
        assertThatThrownBy(() -> signUpService.signUp(command, AuthVendor.KAKAO, "oidc-123", "127.0.0.1"))
                .isInstanceOf(UserNotActiveException.class);

        verify(updateUserPort, never()).update(any());
        verify(saveUserPort, never()).save(any());
        verify(publishUserEventPort, never()).publishUserSignupCompletedEvent(anyLong(), anyString());

    }

    private SignUpCommand createCommand(
            String email,
            String password,
            String verificationCode,
            String nickname,
            String fullName,
            String phoneNumber
    ) {
        return SignUpCommand.builder()
                .email(email)
                .password(password)
                .verificationCode(verificationCode)
                .nickname(nickname)
                .fullName(fullName)
                .phoneNumber(phoneNumber)
                .build();
    }
}
