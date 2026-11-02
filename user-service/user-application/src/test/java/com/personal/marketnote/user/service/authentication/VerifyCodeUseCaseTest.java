package com.personal.marketnote.user.service.authentication;

import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.authentication.Role;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.exception.InvalidVerificationCodeException;
import com.personal.marketnote.user.exception.UserNotActiveException;
import com.personal.marketnote.user.port.in.command.VerifyCodeCommand;
import com.personal.marketnote.user.port.in.result.VerifyCodeResult;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.out.authentication.VerifyCodePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static com.personal.marketnote.common.domain.exception.ExceptionCode.SECOND_ERROR_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyCodeUseCaseTest {
    @Mock
    private GetUserUseCase getUserUseCase;
    @Mock
    private VerifyCodePort verifyCodePort;

    @InjectMocks
    private VerifyCodeService verifyCodeService;

    @Test
    @DisplayName("이메일 인증 코드가 유효하고 계정이 활성 상태면 검증 결과를 반환한다")
    void verifyCode_success_returnsResult() {
        // given
        String email = "user@test.com";
        String code = "123456";
        VerifyCodeCommand command = VerifyCodeCommand.of(email, code);
        User user = mock(User.class);

        when(verifyCodePort.verify(email, code)).thenReturn(true);
        when(getUserUseCase.getAllStatusUser(email)).thenReturn(user);
        when(user.isActive()).thenReturn(true);
        when(user.getId()).thenReturn(1L);
        when(user.getRole()).thenReturn(Role.getBuyer());

        // when
        VerifyCodeResult result = verifyCodeService.verifyCode(command);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.roleId()).isEqualTo("ROLE_BUYER");

        verify(verifyCodePort).verify(email, code);
        verify(getUserUseCase).getAllStatusUser(email);
        verify(user).isActive();
        verify(user).getId();
        verify(user).getRole();
        verifyNoMoreInteractions(getUserUseCase, verifyCodePort, user);
    }

    @Test
    @DisplayName("이메일 인증 코드가 유효하지 않으면 예외를 던진다")
    void verifyCode_invalidCode_throws() {
        // given
        String email = "user@test.com";
        String code = "wrong";
        VerifyCodeCommand command = VerifyCodeCommand.of(email, code);

        when(verifyCodePort.verify(email, code)).thenReturn(false);

        // expect
        assertThatThrownBy(() -> verifyCodeService.verifyCode(command))
                .isInstanceOf(InvalidVerificationCodeException.class)
                .hasMessage(new InvalidVerificationCodeException(FIRST_ERROR_CODE, email).getMessage());

        verify(verifyCodePort).verify(email, code);
        verifyNoInteractions(getUserUseCase);
        verifyNoMoreInteractions(verifyCodePort);
    }

    @Test
    @DisplayName("이메일 인증 코드가 유효하지만 계정이 비활성 상태면 예외를 던진다")
    void verifyCode_inactiveUser_throws() {
        // given
        String email = "user@test.com";
        String code = "123456";
        VerifyCodeCommand command = VerifyCodeCommand.of(email, code);
        User user = mock(User.class);

        when(verifyCodePort.verify(email, code)).thenReturn(true);
        when(getUserUseCase.getAllStatusUser(email)).thenReturn(user);
        when(user.isActive()).thenReturn(false);

        // expect
        assertThatThrownBy(() -> verifyCodeService.verifyCode(command))
                .isInstanceOf(UserNotActiveException.class)
                .hasMessage(new UserNotActiveException(SECOND_ERROR_CODE, email).getMessage());

        verify(verifyCodePort).verify(email, code);
        verify(getUserUseCase).getAllStatusUser(email);
        verify(user).isActive();
        verify(user, never()).getId();
        verify(user, never()).getRole();
        verifyNoMoreInteractions(getUserUseCase, verifyCodePort, user);
    }

    @Test
    @DisplayName("인증 코드 검증 후 회원 조회가 실패하면 예외가 전파된다")
    void verifyCode_userNotFound_throws() {
        // given
        String email = "user@test.com";
        String code = "123456";
        VerifyCodeCommand command = VerifyCodeCommand.of(email, code);
        UserNotFoundException exception = new UserNotFoundException("not found");

        when(verifyCodePort.verify(email, code)).thenReturn(true);
        when(getUserUseCase.getAllStatusUser(email)).thenThrow(exception);

        // expect
        assertThatThrownBy(() -> verifyCodeService.verifyCode(command))
                .isSameAs(exception);

        verify(verifyCodePort).verify(email, code);
        verify(getUserUseCase).getAllStatusUser(email);
        verifyNoMoreInteractions(getUserUseCase, verifyCodePort);
    }
}
