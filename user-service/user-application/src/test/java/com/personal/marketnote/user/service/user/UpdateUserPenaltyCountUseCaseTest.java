package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.exception.illegalstate.SameUpdateTargetException;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.authentication.Role;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserPenaltyHistory;
import com.personal.marketnote.user.domain.user.UserPenaltyHistorySnapshotState;
import com.personal.marketnote.user.domain.user.UserSnapshotState;
import com.personal.marketnote.user.port.in.command.UpdateUserPenaltyCountCommand;
import com.personal.marketnote.user.port.in.result.UpdateUserPenaltyCountResult;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.out.user.SaveUserPenaltyHistoryPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserPenaltyCountUseCaseTest {
    @Mock
    private GetUserUseCase getUserUseCase;
    @Mock
    private UpdateUserPort updateUserPort;
    @Mock
    private SaveUserPenaltyHistoryPort saveUserPenaltyHistoryPort;

    @InjectMocks
    private UpdateUserPenaltyCountService updateUserPenaltyCountService;

    @Test
    @DisplayName("패널티 횟수 수정 시 penaltyCount가 요청값으로 설정된다")
    void shouldUpdatePenaltyCountToRequestedValue() {
        // given
        Long userId = 1L;
        Long adminId = 99L;
        User user = createUser(userId, 2);
        UpdateUserPenaltyCountCommand command = new UpdateUserPenaltyCountCommand(userId, adminId, 10, "보정");

        when(getUserUseCase.getAllStatusUser(userId)).thenReturn(user);
        when(saveUserPenaltyHistoryPort.save(any(UserPenaltyHistory.class))).thenAnswer(restoreWithId(20L));

        // when
        UpdateUserPenaltyCountResult result = updateUserPenaltyCountService.updatePenaltyCount(command);

        // then
        assertThat(user.getPenaltyCount()).isEqualTo(10);
        assertThat(result.penaltyCount()).isEqualTo(10);
        verify(updateUserPort).update(user);
    }

    @Test
    @DisplayName("수정 시 이력이 previousCount(기존), currentCount(신규), reason, createdBy와 함께 저장된다")
    void shouldSaveHistoryWithPreviousAndCurrentCounts() {
        // given
        Long userId = 1L;
        Long adminId = 99L;
        String reason = "중복 부과 보정";
        User user = createUser(userId, 7);
        UpdateUserPenaltyCountCommand command = new UpdateUserPenaltyCountCommand(userId, adminId, 3, reason);

        when(getUserUseCase.getAllStatusUser(userId)).thenReturn(user);
        when(saveUserPenaltyHistoryPort.save(any(UserPenaltyHistory.class))).thenAnswer(restoreWithId(30L));

        // when
        updateUserPenaltyCountService.updatePenaltyCount(command);

        // then
        ArgumentCaptor<UserPenaltyHistory> captor = ArgumentCaptor.forClass(UserPenaltyHistory.class);
        verify(saveUserPenaltyHistoryPort).save(captor.capture());
        UserPenaltyHistory saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getPreviousCount()).isEqualTo(7);
        assertThat(saved.getCurrentCount()).isEqualTo(3);
        assertThat(saved.getReason()).isEqualTo(reason);
        assertThat(saved.getCreatedBy()).isEqualTo(adminId);
    }

    @Test
    @DisplayName("수정 성공 응답에 userId, 수정된 penaltyCount, historyId가 포함된다")
    void shouldReturnResultWithUserIdPenaltyCountAndHistoryId() {
        // given
        Long userId = 1L;
        Long adminId = 99L;
        Long historyId = 50L;
        User user = createUser(userId, 0);
        UpdateUserPenaltyCountCommand command = new UpdateUserPenaltyCountCommand(userId, adminId, 4, "이유");

        when(getUserUseCase.getAllStatusUser(userId)).thenReturn(user);
        when(saveUserPenaltyHistoryPort.save(any(UserPenaltyHistory.class))).thenAnswer(restoreWithId(historyId));

        // when
        UpdateUserPenaltyCountResult result = updateUserPenaltyCountService.updatePenaltyCount(command);

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.penaltyCount()).isEqualTo(4);
        assertThat(result.historyId()).isEqualTo(historyId);
    }

    @Test
    @DisplayName("회원이 존재하지 않으면 UserNotFoundException이 발생한다")
    void shouldThrowUserNotFoundExceptionWhenUserNotExists() {
        // given
        Long userId = 999L;
        UpdateUserPenaltyCountCommand command = new UpdateUserPenaltyCountCommand(userId, 99L, 3, "이유");
        UserNotFoundException exception = new UserNotFoundException("not found");

        when(getUserUseCase.getAllStatusUser(userId)).thenThrow(exception);

        // expect
        assertThatThrownBy(() -> updateUserPenaltyCountService.updatePenaltyCount(command))
                .isSameAs(exception);

        verify(getUserUseCase).getAllStatusUser(userId);
        verifyNoMoreInteractions(getUserUseCase);
        verifyNoInteractions(updateUserPort, saveUserPenaltyHistoryPort);
    }

    @Test
    @DisplayName("동일한 패널티 횟수로 요청하면 SameUpdateTargetException이 발생한다")
    void shouldThrowSameUpdateTargetExceptionWhenSameValue() {
        // given
        Long userId = 1L;
        User user = createUser(userId, 5);
        UpdateUserPenaltyCountCommand command = new UpdateUserPenaltyCountCommand(userId, 99L, 5, "이유");

        when(getUserUseCase.getAllStatusUser(userId)).thenReturn(user);

        // expect
        assertThatThrownBy(() -> updateUserPenaltyCountService.updatePenaltyCount(command))
                .isInstanceOf(SameUpdateTargetException.class);

        verify(getUserUseCase).getAllStatusUser(userId);
        verifyNoInteractions(updateUserPort, saveUserPenaltyHistoryPort);
    }

    @Test
    @DisplayName("getAllStatusUser 호출 후 update, save 순서로 수행된다")
    void shouldExecuteInOrder() {
        // given
        Long userId = 1L;
        User user = createUser(userId, 0);
        UpdateUserPenaltyCountCommand command = new UpdateUserPenaltyCountCommand(userId, 99L, 5, "이유");

        when(getUserUseCase.getAllStatusUser(userId)).thenReturn(user);
        when(saveUserPenaltyHistoryPort.save(any(UserPenaltyHistory.class))).thenAnswer(restoreWithId(40L));

        // when
        updateUserPenaltyCountService.updatePenaltyCount(command);

        // then
        InOrder inOrder = inOrder(getUserUseCase, updateUserPort, saveUserPenaltyHistoryPort);
        inOrder.verify(getUserUseCase).getAllStatusUser(userId);
        inOrder.verify(updateUserPort).update(user);
        inOrder.verify(saveUserPenaltyHistoryPort).save(any(UserPenaltyHistory.class));
    }

    private static User createUser(Long id, int penaltyCount) {
        return User.from(UserSnapshotState.builder()
                .id(id)
                .userKey(UUID.randomUUID())
                .role(Role.getBuyer())
                .userAuthProviders(new ArrayList<>())
                .userTerms(List.of())
                .status(EntityStatus.ACTIVE)
                .withdrawalYn(false)
                .penaltyCount(penaltyCount)
                .build());
    }

    private static Answer<UserPenaltyHistory> restoreWithId(Long historyId) {
        return invocation -> {
            UserPenaltyHistory argument = invocation.getArgument(0);
            return UserPenaltyHistory.from(
                    UserPenaltyHistorySnapshotState.builder()
                            .id(historyId)
                            .userId(argument.getUserId())
                            .previousCount(argument.getPreviousCount())
                            .currentCount(argument.getCurrentCount())
                            .reason(argument.getReason())
                            .createdBy(argument.getCreatedBy())
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        };
    }
}
