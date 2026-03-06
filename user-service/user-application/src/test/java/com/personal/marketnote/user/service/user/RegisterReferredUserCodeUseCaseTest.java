package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.exception.RewardServiceRequestFailedException;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.exception.ReferredUserCodeAlreadyExistsException;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.out.event.PublishUserEventPort;
import com.personal.marketnote.user.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.SECOND_ERROR_CODE;
import static com.personal.marketnote.user.exception.ExceptionMessage.USER_REFERENCE_CODE_NOT_FOUND_EXCEPTION_MESSAGE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterReferredUserCodeUseCaseTest {
    @Mock
    private GetUserUseCase getUserUseCase;
    @Mock
    private UpdateUserPort updateUserPort;
    @Mock
    private PublishUserEventPort publishUserEventPort;
    @Mock
    private ModifyUserPointPort modifyUserPointPort;

    @InjectMocks
    private RegisterReferredUserCodeService registerReferredUserCodeService;

    @Test
    @DisplayName("추천 대상 회원이 존재하지 않으면 예외를 던진다")
    void registerReferredUserCode_notExists_throws() {
        // given
        Long requestUserId = 1L;
        String referredUserCode = "ref-123";

        when(getUserUseCase.existsUser(referredUserCode)).thenReturn(false);

        // expect
        assertThatThrownBy(() -> registerReferredUserCodeService.registerReferredUserCode(requestUserId, referredUserCode))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format(
                        USER_REFERENCE_CODE_NOT_FOUND_EXCEPTION_MESSAGE, referredUserCode
                ));

        verify(getUserUseCase).existsUser(referredUserCode);
        verifyNoMoreInteractions(getUserUseCase);
        verifyNoInteractions(updateUserPort, publishUserEventPort, modifyUserPointPort);
    }

    @Test
    @DisplayName("추천인 코드 등록 성공 시 포인트 적립을 요청한다")
    void registerReferredUserCode_success_accruesPoints() {
        // given
        Long requestUserId = 1L;
        String referredUserCode = "ref-123";
        Long referredUserId = 2L;
        User requestUser = spy(UserTestObjectFactory.createDefaultUser(
                requestUserId, EntityStatus.ACTIVE, false, List.of()
        ));
        User referredUser = mock(User.class);

        when(getUserUseCase.existsUser(referredUserCode)).thenReturn(true);
        when(getUserUseCase.getUser(requestUserId)).thenReturn(requestUser);
        when(getUserUseCase.getUser(referredUserCode)).thenReturn(referredUser);
        when(referredUser.getId()).thenReturn(referredUserId);

        // when
        registerReferredUserCodeService.registerReferredUserCode(requestUserId, referredUserCode);

        // then
        verify(getUserUseCase).existsUser(referredUserCode);
        verify(getUserUseCase).getUser(requestUserId);
        verify(requestUser).registerReferredUserCode(referredUserCode);
        verify(updateUserPort).update(requestUser);
        verify(getUserUseCase).getUser(referredUserCode);
        verify(referredUser, times(2)).getId();
        verify(requestUser, times(2)).getId();
        verify(publishUserEventPort).publishUserReferralCompletedEvent(requestUserId, referredUserId);
        verify(modifyUserPointPort).accrueReferralPoints(requestUserId, referredUserId);
        verify(requestUser, never()).removeReferredUserCode();
        verifyNoMoreInteractions(getUserUseCase, updateUserPort, publishUserEventPort, modifyUserPointPort, referredUser);
    }

    @Test
    @DisplayName("이미 추천인 코드를 등록한 회원인 경우 예외를 던진다")
    void registerReferredUserCode_alreadyRegistered_throws() {
        // given
        Long requestUserId = 1L;
        String referredUserCode = "ref-123";
        User requestUser = spy(UserTestObjectFactory.createDefaultUser(
                requestUserId, EntityStatus.ACTIVE, false, List.of()
        ));
        requestUser.registerReferredUserCode("existing");

        when(getUserUseCase.existsUser(referredUserCode)).thenReturn(true);
        when(getUserUseCase.getUser(requestUserId)).thenReturn(requestUser);

        // expect
        assertThatThrownBy(() -> registerReferredUserCodeService.registerReferredUserCode(requestUserId, referredUserCode))
                .isInstanceOf(ReferredUserCodeAlreadyExistsException.class)
                .hasMessage(new ReferredUserCodeAlreadyExistsException(SECOND_ERROR_CODE).getMessage());

        verify(getUserUseCase).existsUser(referredUserCode);
        verify(getUserUseCase).getUser(requestUserId);
        verify(requestUser).registerReferredUserCode(referredUserCode);
        verify(getUserUseCase, never()).getUser(referredUserCode);
        verifyNoInteractions(updateUserPort, publishUserEventPort, modifyUserPointPort);
        verifyNoMoreInteractions(getUserUseCase);
    }

    @Test
    @DisplayName("포인트 적립 요청이 실패하면 추천인 코드 등록 변경사항을 롤백한다")
    void registerReferredUserCode_rewardAccrualFails_rollsBack() {
        // given
        Long requestUserId = 1L;
        String referredUserCode = "ref-123";
        Long referredUserId = 2L;
        User requestUser = spy(UserTestObjectFactory.createDefaultUser(
                requestUserId, EntityStatus.ACTIVE, false, List.of()
        ));
        User referredUser = mock(User.class);
        RewardServiceRequestFailedException exception
                = new RewardServiceRequestFailedException(new IOException("fail"));

        when(getUserUseCase.existsUser(referredUserCode)).thenReturn(true);
        when(getUserUseCase.getUser(requestUserId)).thenReturn(requestUser);
        when(getUserUseCase.getUser(referredUserCode)).thenReturn(referredUser);
        when(referredUser.getId()).thenReturn(referredUserId);
        doThrow(exception).when(modifyUserPointPort)
                .accrueReferralPoints(requestUserId, referredUserId);

        // expect
        assertThatThrownBy(() -> registerReferredUserCodeService.registerReferredUserCode(requestUserId, referredUserCode))
                .isSameAs(exception);

        verify(getUserUseCase).existsUser(referredUserCode);
        verify(getUserUseCase).getUser(requestUserId);
        verify(requestUser).registerReferredUserCode(referredUserCode);
        verify(updateUserPort, times(2)).update(requestUser);
        verify(getUserUseCase).getUser(referredUserCode);
        verify(referredUser, times(2)).getId();
        verify(requestUser, times(2)).getId();
        verify(publishUserEventPort).publishUserReferralCompletedEvent(requestUserId, referredUserId);
        verify(modifyUserPointPort).accrueReferralPoints(requestUserId, referredUserId);
        verify(requestUser).removeReferredUserCode();
        verifyNoMoreInteractions(getUserUseCase, updateUserPort, publishUserEventPort, modifyUserPointPort, referredUser);
    }

    @Test
    @DisplayName("요청 회원을 찾지 못하면 예외를 던진다")
    void registerReferredUserCode_requestUserNotFound_throws() {
        // given
        Long requestUserId = 1L;
        String referredUserCode = "ref-123";
        UserNotFoundException exception = new UserNotFoundException("not found");

        when(getUserUseCase.existsUser(referredUserCode)).thenReturn(true);
        when(getUserUseCase.getUser(requestUserId)).thenThrow(exception);

        // expect
        assertThatThrownBy(() -> registerReferredUserCodeService.registerReferredUserCode(requestUserId, referredUserCode))
                .isSameAs(exception);

        verify(getUserUseCase).existsUser(referredUserCode);
        verify(getUserUseCase).getUser(requestUserId);
        verify(getUserUseCase, never()).getUser(referredUserCode);
        verifyNoInteractions(updateUserPort, publishUserEventPort, modifyUserPointPort);
        verifyNoMoreInteractions(getUserUseCase);
    }

    @Test
    @DisplayName("추천 대상 회원 조회에 실패하면 예외를 던진다")
    void registerReferredUserCode_referredUserNotFound_throws() {
        // given
        Long requestUserId = 1L;
        String referredUserCode = "ref-123";
        User requestUser = spy(UserTestObjectFactory.createDefaultUser(
                requestUserId, EntityStatus.ACTIVE, false, List.of()
        ));
        UserNotFoundException exception = new UserNotFoundException("not found");

        when(getUserUseCase.existsUser(referredUserCode)).thenReturn(true);
        when(getUserUseCase.getUser(requestUserId)).thenReturn(requestUser);
        when(getUserUseCase.getUser(referredUserCode)).thenThrow(exception);

        // expect
        assertThatThrownBy(() -> registerReferredUserCodeService.registerReferredUserCode(requestUserId, referredUserCode))
                .isSameAs(exception);

        verify(getUserUseCase).existsUser(referredUserCode);
        verify(getUserUseCase).getUser(requestUserId);
        verify(requestUser).registerReferredUserCode(referredUserCode);
        verify(updateUserPort).update(requestUser);
        verify(getUserUseCase).getUser(referredUserCode);
        verifyNoInteractions(publishUserEventPort, modifyUserPointPort);
        verify(requestUser, never()).removeReferredUserCode();
        verifyNoMoreInteractions(getUserUseCase, updateUserPort);
    }
}
