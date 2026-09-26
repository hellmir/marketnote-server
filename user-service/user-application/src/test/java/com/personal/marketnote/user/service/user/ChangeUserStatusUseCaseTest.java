package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserSnapshotState;
import com.personal.marketnote.user.domain.user.UserStatusAction;
import com.personal.marketnote.user.domain.user.UserStatusActor;
import com.personal.marketnote.user.domain.user.UserStatusHistory;
import com.personal.marketnote.user.domain.user.UserStatusHistorySnapshotState;
import com.personal.marketnote.user.exception.AdminSelfDeactivationException;
import com.personal.marketnote.user.exception.InvalidUserStatusTransitionException;
import com.personal.marketnote.user.port.in.command.ChangeUserStatusCommand;
import com.personal.marketnote.user.port.in.result.ChangeUserStatusResult;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.out.authentication.DeleteRefreshTokenPort;
import com.personal.marketnote.user.port.out.user.SaveUserStatusHistoryPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeUserStatusUseCaseTest {
    @InjectMocks
    private ChangeUserStatusService changeUserStatusService;

    @Mock
    private GetUserUseCase getUserUseCase;

    @Mock
    private UpdateUserPort updateUserPort;

    @Mock
    private SaveUserStatusHistoryPort saveUserStatusHistoryPort;

    @Mock
    private DeleteRefreshTokenPort deleteRefreshTokenPort;

    private static final Long USER_ID = 1L;
    private static final Long ADMIN_ID = 100L;

    @Test
    @DisplayName("ACTIVE 회원을 비활성화하면 status가 INACTIVE로 변경되고 이력이 저장된다")
    void shouldDeactivateActiveUser() {
        // given
        User activeUser = createUserWithStatus(EntityStatus.ACTIVE);
        when(getUserUseCase.getAllStatusUser(USER_ID)).thenReturn(activeUser);

        LocalDateTime deactivatedUntil = LocalDateTime.of(2026, 5, 13, 0, 0);
        ChangeUserStatusCommand command = new ChangeUserStatusCommand(
                USER_ID, ADMIN_ID, UserStatusAction.DEACTIVATE.name(), "욕설 사용", deactivatedUntil
        );

        UserStatusHistory savedHistory = createSavedHistory(1L, UserStatusAction.DEACTIVATE, deactivatedUntil);
        when(saveUserStatusHistoryPort.save(any(UserStatusHistory.class))).thenReturn(savedHistory);

        // when
        ChangeUserStatusResult result = changeUserStatusService.changeStatus(command);

        // then
        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.status()).isEqualTo(EntityStatus.INACTIVE.name());
        assertThat(result.deactivatedUntil()).isEqualTo(deactivatedUntil);
        assertThat(result.historyId()).isEqualTo(1L);

        verify(getUserUseCase).getAllStatusUser(USER_ID);
        verify(updateUserPort).update(activeUser);

        ArgumentCaptor<UserStatusHistory> historyCaptor = ArgumentCaptor.forClass(UserStatusHistory.class);
        verify(saveUserStatusHistoryPort).save(historyCaptor.capture());
        UserStatusHistory capturedHistory = historyCaptor.getValue();
        assertThat(capturedHistory.getUserId()).isEqualTo(USER_ID);
        assertThat(capturedHistory.getStatusAction()).isEqualTo(UserStatusAction.DEACTIVATE);
        assertThat(capturedHistory.getReason()).isEqualTo("욕설 사용");
        assertThat(capturedHistory.getDeactivatedUntil()).isEqualTo(deactivatedUntil);
        assertThat(capturedHistory.getActor()).isEqualTo(UserStatusActor.ADMIN);
        assertThat(capturedHistory.getCreatedBy()).isEqualTo(ADMIN_ID);
    }

    @Test
    @DisplayName("영구 비활성화 시 deactivatedUntil이 null로 저장된다")
    void shouldDeactivatePermanentlyWhenDeactivatedUntilIsNull() {
        // given
        User activeUser = createUserWithStatus(EntityStatus.ACTIVE);
        when(getUserUseCase.getAllStatusUser(USER_ID)).thenReturn(activeUser);

        ChangeUserStatusCommand command = new ChangeUserStatusCommand(
                USER_ID, ADMIN_ID, UserStatusAction.DEACTIVATE.name(), "영구 정지", null
        );

        UserStatusHistory savedHistory = createSavedHistory(2L, UserStatusAction.DEACTIVATE, null);
        when(saveUserStatusHistoryPort.save(any(UserStatusHistory.class))).thenReturn(savedHistory);

        // when
        ChangeUserStatusResult result = changeUserStatusService.changeStatus(command);

        // then
        assertThat(result.deactivatedUntil()).isNull();

        ArgumentCaptor<UserStatusHistory> historyCaptor = ArgumentCaptor.forClass(UserStatusHistory.class);
        verify(saveUserStatusHistoryPort).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getDeactivatedUntil()).isNull();
    }

    @Test
    @DisplayName("INACTIVE 회원을 활성화하면 status가 ACTIVE로 변경되고 deactivatedUntil이 null로 초기화된다")
    void shouldActivateInactiveUser() {
        // given
        User inactiveUser = createUserWithStatus(EntityStatus.INACTIVE);
        when(getUserUseCase.getAllStatusUser(USER_ID)).thenReturn(inactiveUser);

        ChangeUserStatusCommand command = new ChangeUserStatusCommand(
                USER_ID, ADMIN_ID, UserStatusAction.ACTIVATE.name(), "정지 해제", null
        );

        UserStatusHistory savedHistory = createSavedHistory(3L, UserStatusAction.ACTIVATE, null);
        when(saveUserStatusHistoryPort.save(any(UserStatusHistory.class))).thenReturn(savedHistory);

        // when
        ChangeUserStatusResult result = changeUserStatusService.changeStatus(command);

        // then
        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.status()).isEqualTo(EntityStatus.ACTIVE.name());
        assertThat(result.deactivatedUntil()).isNull();
        assertThat(result.historyId()).isEqualTo(3L);

        verify(updateUserPort).update(inactiveUser);
    }

    @Test
    @DisplayName("이미 ACTIVE인 회원을 ACTIVATE하면 InvalidUserStatusTransitionException이 발생한다")
    void shouldThrowExceptionWhenActivatingAlreadyActiveUser() {
        // given
        User activeUser = createUserWithStatus(EntityStatus.ACTIVE);
        when(getUserUseCase.getAllStatusUser(USER_ID)).thenReturn(activeUser);

        ChangeUserStatusCommand command = new ChangeUserStatusCommand(
                USER_ID, ADMIN_ID, UserStatusAction.ACTIVATE.name(), "정지 해제", null
        );

        // when & then
        assertThatThrownBy(() -> changeUserStatusService.changeStatus(command))
                .isInstanceOf(InvalidUserStatusTransitionException.class);

        verifyNoInteractions(updateUserPort);
        verifyNoInteractions(saveUserStatusHistoryPort);
    }

    @Test
    @DisplayName("이미 INACTIVE인 회원을 DEACTIVATE하면 InvalidUserStatusTransitionException이 발생한다")
    void shouldThrowExceptionWhenDeactivatingAlreadyInactiveUser() {
        // given
        User inactiveUser = createUserWithStatus(EntityStatus.INACTIVE);
        when(getUserUseCase.getAllStatusUser(USER_ID)).thenReturn(inactiveUser);

        ChangeUserStatusCommand command = new ChangeUserStatusCommand(
                USER_ID, ADMIN_ID, UserStatusAction.DEACTIVATE.name(), "추가 정지", null
        );

        // when & then
        assertThatThrownBy(() -> changeUserStatusService.changeStatus(command))
                .isInstanceOf(InvalidUserStatusTransitionException.class);

        verifyNoInteractions(updateUserPort);
        verifyNoInteractions(saveUserStatusHistoryPort);
    }

    @Test
    @DisplayName("관리자가 자기 자신을 비활성화하면 AdminSelfDeactivationException이 발생한다")
    void shouldThrowExceptionWhenAdminDeactivatesSelf() {
        // given
        Long sameId = 100L;
        ChangeUserStatusCommand command = new ChangeUserStatusCommand(
                sameId, sameId, UserStatusAction.DEACTIVATE.name(), "자기 정지", null
        );

        // when & then
        assertThatThrownBy(() -> changeUserStatusService.changeStatus(command))
                .isInstanceOf(AdminSelfDeactivationException.class);

        verifyNoInteractions(getUserUseCase);
        verifyNoInteractions(updateUserPort);
        verifyNoInteractions(saveUserStatusHistoryPort);
    }

    @Test
    @DisplayName("탈퇴한 INACTIVE 회원을 ACTIVATE하면 InvalidUserStatusTransitionException이 발생한다")
    void shouldThrowExceptionWhenActivatingWithdrawnUser() {
        // given
        User withdrawnUser = User.from(
                UserSnapshotState.builder()
                        .id(USER_ID)
                        .nickname("withdrawn")
                        .status(EntityStatus.INACTIVE)
                        .penaltyCount(0)
                        .withdrawalYn(true)
                        .userAuthProviders(List.of())
                        .userTerms(List.of())
                        .lastLoggedInAt(LocalDateTime.now())
                        .build()
        );
        when(getUserUseCase.getAllStatusUser(USER_ID)).thenReturn(withdrawnUser);

        ChangeUserStatusCommand command = new ChangeUserStatusCommand(
                USER_ID, ADMIN_ID, UserStatusAction.ACTIVATE.name(), "정지 해제 시도", null
        );

        // when & then
        assertThatThrownBy(() -> changeUserStatusService.changeStatus(command))
                .isInstanceOf(InvalidUserStatusTransitionException.class);

        verifyNoInteractions(updateUserPort);
        verifyNoInteractions(saveUserStatusHistoryPort);
    }

    private User createUserWithStatus(EntityStatus status) {
        return User.from(
                UserSnapshotState.builder()
                        .id(USER_ID)
                        .nickname("testuser")
                        .status(status)
                        .penaltyCount(0)
                        .userAuthProviders(List.of())
                        .userTerms(List.of())
                        .lastLoggedInAt(LocalDateTime.now())
                        .build()
        );
    }

    private UserStatusHistory createSavedHistory(Long id, UserStatusAction action, LocalDateTime deactivatedUntil) {
        return UserStatusHistory.from(
                UserStatusHistorySnapshotState.builder()
                        .id(id)
                        .userId(USER_ID)
                        .statusAction(action)
                        .reason("테스트 사유")
                        .deactivatedUntil(deactivatedUntil)
                        .actor(UserStatusActor.ADMIN)
                        .createdBy(ADMIN_ID)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }
}
