package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.domain.exception.illegalargument.novalue.UpdateTargetNoValueException;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.exception.InvalidNicknameContainsProfanityException;
import com.personal.marketnote.user.exception.UserExistsException;
import com.personal.marketnote.user.port.in.command.UpdateUserInfoCommand;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.out.profanity.FindProfanityWordPort;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.*;
import static com.personal.marketnote.user.exception.ExceptionMessage.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserUseCaseTest {
    @Mock
    private GetUserUseCase getUserUseCase;
    @Mock
    private FindUserPort findUserPort;
    @Mock
    private UpdateUserPort updateUserPort;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private FindProfanityWordPort findProfanityWordPort;

    @InjectMocks
    private UpdateUserService updateUserService;

    @Test
    @DisplayName("회원 상태 변경 요청 시 회원 도메인의 상태를 갱신한다")
    void updateUserInfo_adminUpdatesStatus() {
        // given
        Long id = 1L;
        boolean isActive = false;
        UpdateUserInfoCommand command = UpdateUserInfoCommand.builder()
                .isActive(isActive)
                .build();
        User user = mock(User.class);

        when(getUserUseCase.getAllStatusUser(id)).thenReturn(user);

        // when
        updateUserService.updateUserInfo(true, id, command);

        // then
        verify(getUserUseCase).getAllStatusUser(id);
        verify(user).updateStatus(isActive);
        verify(updateUserPort).update(user);
        verifyNoMoreInteractions(getUserUseCase, user, updateUserPort);
        verifyNoInteractions(findUserPort, passwordEncoder, findProfanityWordPort);
    }

    @Test
    @DisplayName("관리자가 아닌 경우 회원 활성 상태를 변경하려 시도하면 예외를 던진다")
    void updateUserInfo_nonAdminWithIsActive_throws() {
        // given
        Long id = 2L;
        UpdateUserInfoCommand command = UpdateUserInfoCommand.builder()
                .isActive(true)
                .build();
        User user = mock(User.class);

        when(getUserUseCase.getUser(id)).thenReturn(user);

        // expect
        assertThatThrownBy(() -> updateUserService.updateUserInfo(false, id, command))
                .isInstanceOf(UpdateTargetNoValueException.class);

        verify(getUserUseCase).getUser(id);
        verifyNoMoreInteractions(getUserUseCase);
        verifyNoInteractions(findUserPort, updateUserPort, passwordEncoder, findProfanityWordPort);
        verifyNoMoreInteractions(user);
    }

    @Test
    @DisplayName("회원 정보 수정 요청 시 대상 회원이 존재하지 않으면 예외를 던진다")
    void updateUserInfo_noUpdateTarget_throws() {
        // given
        Long id = 3L;
        UpdateUserInfoCommand command = UpdateUserInfoCommand.builder().build();
        User user = mock(User.class);

        when(getUserUseCase.getUser(id)).thenReturn(user);

        // expect
        assertThatThrownBy(() -> updateUserService.updateUserInfo(false, id, command))
                .isInstanceOf(UpdateTargetNoValueException.class);

        verify(getUserUseCase).getUser(id);
        verifyNoMoreInteractions(getUserUseCase);
        verifyNoInteractions(findUserPort, updateUserPort, passwordEncoder, findProfanityWordPort);
        verifyNoMoreInteractions(user);
    }

    @Test
    @DisplayName("회원 비밀번호 변경 요청 시 회원 도메인의 비밀번호를 갱신한다")
    void updateUserInfo_updatesPassword() {
        // given
        Long id = 4L;
        String password = "new-password";
        UpdateUserInfoCommand command = UpdateUserInfoCommand.builder()
                .password(password)
                .build();
        User user = mock(User.class);

        when(getUserUseCase.getUser(id)).thenReturn(user);

        // when
        updateUserService.updateUserInfo(false, id, command);

        // then
        verify(getUserUseCase).getUser(id);
        verify(user).updatePassword(password, passwordEncoder);
        verify(updateUserPort).update(user);
        verifyNoMoreInteractions(getUserUseCase, user, updateUserPort);
        verifyNoInteractions(findUserPort, findProfanityWordPort);
    }

    @Test
    @DisplayName("회원 비밀번호 변경 요청 시 이메일 변경 요청은 무시된다")
    void updateUserInfo_passwordTakesPrecedenceOverEmail() {
        // given
        Long id = 5L;
        String password = "new-password";
        String email = "new@test.com";
        UpdateUserInfoCommand command = UpdateUserInfoCommand.builder()
                .password(password)
                .email(email)
                .build();
        User user = mock(User.class);

        when(getUserUseCase.getUser(id)).thenReturn(user);

        // when
        updateUserService.updateUserInfo(false, id, command);

        // then
        verify(getUserUseCase).getUser(id);
        verify(user).updatePassword(password, passwordEncoder);
        verify(user, never()).validateDifferentEmail(anyString());
        verify(user, never()).updateEmail(anyString());
        verify(updateUserPort).update(user);
        verifyNoMoreInteractions(getUserUseCase, user, updateUserPort);
        verifyNoInteractions(findUserPort, findProfanityWordPort);
    }

    @Test
    @DisplayName("회원 이메일 주소 변경 요청 시 회원 도메인의 이메일 주소를 갱신한다")
    void updateUserInfo_updatesEmail() {
        // given
        Long id = 6L;
        String email = "new@test.com";
        UpdateUserInfoCommand command = UpdateUserInfoCommand.builder()
                .email(email)
                .build();
        User user = mock(User.class);

        when(getUserUseCase.getUser(id)).thenReturn(user);
        when(findUserPort.existsByEmail(email)).thenReturn(false);

        // when
        updateUserService.updateUserInfo(false, id, command);

        // then
        verify(getUserUseCase).getUser(id);
        verify(user).validateDifferentEmail(email);
        verify(findUserPort).existsByEmail(email);
        verify(user).updateEmail(email);
        verify(updateUserPort).update(user);
        verifyNoMoreInteractions(getUserUseCase, user, updateUserPort);
        verifyNoMoreInteractions(findUserPort);
        verifyNoInteractions(findProfanityWordPort);
    }

    @Test
    @DisplayName("이미 사용 중인 이메일 주소를 등록 시도하면 예외를 던진다")
    void updateUserInfo_duplicateEmail_throws() {
        // given
        Long id = 7L;
        String email = "dup@test.com";
        UpdateUserInfoCommand command = UpdateUserInfoCommand.builder()
                .email(email)
                .build();
        User user = mock(User.class);

        when(getUserUseCase.getUser(id)).thenReturn(user);
        when(findUserPort.existsByEmail(email)).thenReturn(true);

        // expect
        assertThatThrownBy(() -> updateUserService.updateUserInfo(false, id, command))
                .isInstanceOf(UserExistsException.class)
                .hasMessage(String.format(EMAIL_ALREADY_EXISTS_EXCEPTION_MESSAGE, FOURTH_ERROR_CODE, email));

        verify(getUserUseCase).getUser(id);
        verify(user).validateDifferentEmail(email);
        verify(findUserPort).existsByEmail(email);
        verify(user, never()).updateEmail(anyString());
        verifyNoInteractions(updateUserPort, findProfanityWordPort);
        verifyNoMoreInteractions(getUserUseCase, user, findUserPort);
    }

    @Test
    @DisplayName("회원 닉네임 변경 요청 시 회원 도메인의 닉네임을 갱신한다")
    void updateUserInfo_updatesNickname() {
        // given
        Long id = 8L;
        String nickname = "new-nick";
        UpdateUserInfoCommand command = UpdateUserInfoCommand.builder()
                .nickname(nickname)
                .build();
        User user = mock(User.class);

        when(getUserUseCase.getUser(id)).thenReturn(user);
        when(findProfanityWordPort.containsProfanity(nickname)).thenReturn(false);
        when(findUserPort.existsByNickname(nickname)).thenReturn(false);

        // when
        updateUserService.updateUserInfo(false, id, command);

        // then
        verify(getUserUseCase).getUser(id);
        verify(user).validateDifferentNickname(nickname);
        verify(findProfanityWordPort).containsProfanity(nickname);
        verify(findUserPort).existsByNickname(nickname);
        verify(user).updateNickname(nickname);
        verify(updateUserPort).update(user);
        verifyNoMoreInteractions(getUserUseCase, user, updateUserPort, findProfanityWordPort);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("이미 사용 중인 닉네임을 등록 시도하면 예외를 던진다")
    void updateUserInfo_duplicateNickname_throws() {
        // given
        Long id = 9L;
        String nickname = "dup-nick";
        UpdateUserInfoCommand command = UpdateUserInfoCommand.builder()
                .nickname(nickname)
                .build();
        User user = mock(User.class);

        when(getUserUseCase.getUser(id)).thenReturn(user);
        when(findProfanityWordPort.containsProfanity(nickname)).thenReturn(false);
        when(findUserPort.existsByNickname(nickname)).thenReturn(true);

        // expect
        assertThatThrownBy(() -> updateUserService.updateUserInfo(false, id, command))
                .isInstanceOf(UserExistsException.class)
                .hasMessage(String.format(NICKNAME_ALREADY_EXISTS_EXCEPTION_MESSAGE, FIFTH_ERROR_CODE, nickname));

        verify(getUserUseCase).getUser(id);
        verify(user).validateDifferentNickname(nickname);
        verify(findProfanityWordPort).containsProfanity(nickname);
        verify(findUserPort).existsByNickname(nickname);
        verify(user, never()).updateNickname(anyString());
        verifyNoInteractions(updateUserPort);
        verifyNoMoreInteractions(getUserUseCase, user, findUserPort, findProfanityWordPort);
    }

    @Test
    @DisplayName("회원 전화번호 변경 요청 시 회원 도메인의 전화번호를 갱신한다")
    void updateUserInfo_updatesPhoneNumber() {
        // given
        Long id = 10L;
        String phoneNumber = "010-1111-2222";
        UpdateUserInfoCommand command = UpdateUserInfoCommand.builder()
                .phoneNumber(phoneNumber)
                .build();
        User user = mock(User.class);

        when(getUserUseCase.getUser(id)).thenReturn(user);
        when(findUserPort.existsByPhoneNumber(phoneNumber)).thenReturn(false);

        // when
        updateUserService.updateUserInfo(false, id, command);

        // then
        verify(getUserUseCase).getUser(id);
        verify(user).validateDifferentPhoneNumber(phoneNumber);
        verify(findUserPort).existsByPhoneNumber(phoneNumber);
        verify(user).updatePhoneNumber(phoneNumber);
        verify(updateUserPort).update(user);
        verifyNoMoreInteractions(getUserUseCase, user, updateUserPort);
        verifyNoMoreInteractions(findUserPort);
        verifyNoInteractions(findProfanityWordPort);
    }

    @Test
    @DisplayName("이미 사용 중인 전화번호를 등록 시도하면 예외를 던진다")
    void updateUserInfo_duplicatePhoneNumber_throws() {
        // given
        Long id = 11L;
        String phoneNumber = "010-3333-4444";
        UpdateUserInfoCommand command = UpdateUserInfoCommand.builder()
                .phoneNumber(phoneNumber)
                .build();
        User user = mock(User.class);

        when(getUserUseCase.getUser(id)).thenReturn(user);
        when(findUserPort.existsByPhoneNumber(phoneNumber)).thenReturn(true);

        // expect
        assertThatThrownBy(() -> updateUserService.updateUserInfo(false, id, command))
                .isInstanceOf(UserExistsException.class)
                .hasMessage(String.format(PHONE_NUMBER_ALREADY_EXISTS_EXCEPTION_MESSAGE, SIXTH_ERROR_CODE, phoneNumber));

        verify(getUserUseCase).getUser(id);
        verify(user).validateDifferentPhoneNumber(phoneNumber);
        verify(findUserPort).existsByPhoneNumber(phoneNumber);
        verify(user, never()).updatePhoneNumber(anyString());
        verifyNoInteractions(updateUserPort, findProfanityWordPort);
        verifyNoMoreInteractions(getUserUseCase, user, findUserPort);
    }

    @Test
    @DisplayName("회원 조회 요청 시 회원이 존재하지 않는 경우 예외를 던진다")
    void updateUserInfo_userNotFound_throws() {
        // given
        Long id = 12L;
        UpdateUserInfoCommand command = UpdateUserInfoCommand.builder()
                .email("user@test.com")
                .build();
        UserNotFoundException exception = new UserNotFoundException("not found");

        when(getUserUseCase.getUser(id)).thenThrow(exception);

        // expect
        assertThatThrownBy(() -> updateUserService.updateUserInfo(false, id, command))
                .isSameAs(exception);

        verify(getUserUseCase).getUser(id);
        verifyNoMoreInteractions(getUserUseCase);
        verifyNoInteractions(findUserPort, updateUserPort, passwordEncoder, findProfanityWordPort);
    }

    @Test
    @DisplayName("닉네임에 금칙어가 포함된 경우 닉네임 수정에 실패한다")
    void updateUserInfo_profanityNickname_throws() {
        // given
        Long id = 13L;
        String nickname = "나는바보야";
        UpdateUserInfoCommand command = UpdateUserInfoCommand.builder()
                .nickname(nickname)
                .build();
        User user = mock(User.class);

        when(getUserUseCase.getUser(id)).thenReturn(user);
        when(findProfanityWordPort.containsProfanity(nickname)).thenReturn(true);

        // expect
        assertThatThrownBy(() -> updateUserService.updateUserInfo(false, id, command))
                .isInstanceOf(InvalidNicknameContainsProfanityException.class);

        verify(getUserUseCase).getUser(id);
        verify(user).validateDifferentNickname(nickname);
        verify(findProfanityWordPort).containsProfanity(nickname);
        verify(user, never()).updateNickname(anyString());
        verifyNoInteractions(findUserPort, updateUserPort);
    }
}
