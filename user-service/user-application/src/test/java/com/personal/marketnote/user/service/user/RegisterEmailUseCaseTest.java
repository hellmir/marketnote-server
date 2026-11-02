package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.domain.email.Email;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.exception.UserExistsException;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static com.personal.marketnote.user.exception.ExceptionMessage.EMAIL_ALREADY_EXISTS_EXCEPTION_MESSAGE;
import static com.personal.marketnote.user.exception.ExceptionMessage.USER_ID_NOT_FOUND_EXCEPTION_MESSAGE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterEmailUseCaseTest {
    @Mock
    private FindUserPort findUserPort;
    @Mock
    private UpdateUserPort updateUserPort;

    @InjectMocks
    private RegisterEmailService registerEmailService;

    @Test
    @DisplayName("회원 이메일 주소를 등록하면 회원 도메인의 이메일 주소가 갱신된다")
    void registerEmail_success_updatesUser() {
        // given
        Long id = 1L;
        String email = "user@test.com";
        User user = mock(User.class);

        when(findUserPort.findById(id)).thenReturn(Optional.of(user));
        when(findUserPort.existsByEmail(email)).thenReturn(false);

        // when
        registerEmailService.registerEmail(id, AuthVendor.KAKAO, email);

        // then
        verify(findUserPort).findById(id);
        verify(findUserPort).existsByEmail(email);
        verify(user).registerEmail(Email.of(email));
        verify(updateUserPort).update(user);
        verifyNoMoreInteractions(findUserPort, updateUserPort, user);
    }

    @Test
    @DisplayName("존재하지 않는 회원의 이메일 주소 등록을 시도하면 예외를 던진다")
    void registerEmail_userNotFound_throws() {
        // given
        Long id = 999L;
        String email = "user@test.com";

        when(findUserPort.findById(id)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> registerEmailService.registerEmail(id, AuthVendor.GOOGLE, email))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format(USER_ID_NOT_FOUND_EXCEPTION_MESSAGE, id));

        verify(findUserPort).findById(id);
        verifyNoMoreInteractions(findUserPort);
        verifyNoInteractions(updateUserPort);
    }

    @Test
    @DisplayName("이미 사용 중인 이메일 주소를 등록 시도하면 예외를 던진다")
    void registerEmail_duplicateEmail_throws() {
        // given
        Long id = 2L;
        String email = "user@test.com";
        User user = mock(User.class);

        when(findUserPort.findById(id)).thenReturn(Optional.of(user));
        when(findUserPort.existsByEmail(email)).thenReturn(true);

        // expect
        assertThatThrownBy(() -> registerEmailService.registerEmail(id, AuthVendor.KAKAO, email))
                .isInstanceOf(UserExistsException.class)
                .hasMessage(String.format(EMAIL_ALREADY_EXISTS_EXCEPTION_MESSAGE, FIRST_ERROR_CODE, email));

        verify(findUserPort).findById(id);
        verify(findUserPort).existsByEmail(email);
        verify(user, never()).registerEmail(any(Email.class));
        verifyNoInteractions(updateUserPort);
        verifyNoMoreInteractions(findUserPort, user);
    }
}
