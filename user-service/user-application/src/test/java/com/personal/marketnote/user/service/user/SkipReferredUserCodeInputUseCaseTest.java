package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.user.ReferenceCode;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.exception.ReferredUserCodeAlreadyExistsException;
import com.personal.marketnote.user.exception.ReferredUserCodeInputAlreadySkippedException;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkipReferredUserCodeInputUseCaseTest {
    @Mock
    private GetUserUseCase getUserUseCase;
    @Mock
    private UpdateUserPort updateUserPort;

    @InjectMocks
    private SkipReferredUserCodeInputService skipReferredUserCodeInputService;

    @Test
    @DisplayName("미입력 회원이 건너뛰기 요청 시 referredUserCodeSkippedAt이 설정되고 update가 호출된다")
    void skipReferredUserCodeInput_pending_success() {
        // given
        Long requestUserId = 1L;
        User requestUser = UserTestObjectFactory.createDefaultUser(
                requestUserId, EntityStatus.ACTIVE, false, List.of()
        );
        LocalDateTime before = LocalDateTime.now();

        when(getUserUseCase.getUser(requestUserId)).thenReturn(requestUser);

        // when
        skipReferredUserCodeInputService.skipReferredUserCodeInput(requestUserId);

        // then
        assertThat(requestUser.isReferredUserCodeInputSkipped()).isTrue();
        assertThat(requestUser.getReferredUserCodeSkippedAt()).isAfterOrEqualTo(before);
        verify(getUserUseCase).getUser(requestUserId);
        verify(updateUserPort).update(requestUser);
        verifyNoMoreInteractions(getUserUseCase, updateUserPort);
    }

    @Test
    @DisplayName("이미 추천 코드를 등록한 회원이 건너뛰기 요청 시 ReferredUserCodeAlreadyExistsException이 발생한다")
    void skipReferredUserCodeInput_alreadyRegistered_throws() {
        // given
        Long requestUserId = 1L;
        User requestUser = UserTestObjectFactory.createDefaultUser(
                requestUserId, EntityStatus.ACTIVE, false, List.of()
        );
        requestUser.registerReferredUserCode(ReferenceCode.of("EXIST1"));

        when(getUserUseCase.getUser(requestUserId)).thenReturn(requestUser);

        // expect
        assertThatThrownBy(() -> skipReferredUserCodeInputService.skipReferredUserCodeInput(requestUserId))
                .isInstanceOf(ReferredUserCodeAlreadyExistsException.class);

        verify(getUserUseCase).getUser(requestUserId);
        verifyNoInteractions(updateUserPort);
        verifyNoMoreInteractions(getUserUseCase);
    }

    @Test
    @DisplayName("이미 건너뛰기를 한 회원이 다시 건너뛰기 요청 시 ReferredUserCodeInputAlreadySkippedException이 발생한다")
    void skipReferredUserCodeInput_alreadySkipped_throws() {
        // given
        Long requestUserId = 1L;
        User requestUser = UserTestObjectFactory.createDefaultUser(
                requestUserId, EntityStatus.ACTIVE, false, List.of()
        );
        requestUser.skipReferredUserCodeInput(LocalDateTime.of(2026, 4, 27, 10, 0));

        when(getUserUseCase.getUser(requestUserId)).thenReturn(requestUser);

        // expect
        assertThatThrownBy(() -> skipReferredUserCodeInputService.skipReferredUserCodeInput(requestUserId))
                .isInstanceOf(ReferredUserCodeInputAlreadySkippedException.class);

        verify(getUserUseCase).getUser(requestUserId);
        verifyNoInteractions(updateUserPort);
        verifyNoMoreInteractions(getUserUseCase);
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID 요청 시 UserNotFoundException이 전파된다")
    void skipReferredUserCodeInput_userNotFound_throws() {
        // given
        Long requestUserId = 999L;
        UserNotFoundException exception = new UserNotFoundException();

        when(getUserUseCase.getUser(requestUserId)).thenThrow(exception);

        // expect
        assertThatThrownBy(() -> skipReferredUserCodeInputService.skipReferredUserCodeInput(requestUserId))
                .isSameAs(exception);

        verify(getUserUseCase).getUser(requestUserId);
        verifyNoInteractions(updateUserPort);
        verifyNoMoreInteractions(getUserUseCase);
    }
}
