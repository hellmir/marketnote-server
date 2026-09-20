package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.authentication.Role;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserPenaltyHistory;
import com.personal.marketnote.user.domain.user.UserPenaltyHistorySnapshotState;
import com.personal.marketnote.user.domain.user.UserSnapshotState;
import com.personal.marketnote.user.port.in.command.ApplyUserPenaltyCommand;
import com.personal.marketnote.user.port.in.result.ApplyUserPenaltyResult;
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
class ApplyUserPenaltyServiceTest {
    @Mock
    private GetUserUseCase getUserUseCase;
    @Mock
    private UpdateUserPort updateUserPort;
    @Mock
    private SaveUserPenaltyHistoryPort saveUserPenaltyHistoryPort;

    @InjectMocks
    private ApplyUserPenaltyService applyUserPenaltyService;

    @Test
    @DisplayName("패널티 부과 시 회원의 패널티 횟수가 1 증가한다")
    void shouldIncreasePenaltyCountByOne() {
        // given
        Long userId = 1L;
        Long adminId = 99L;
        String reason = "게시글 도배 행위";
        User user = createUser(userId, 0);
        ApplyUserPenaltyCommand command = new ApplyUserPenaltyCommand(userId, adminId, reason);

        when(getUserUseCase.getAllStatusUser(userId)).thenReturn(user);
        when(saveUserPenaltyHistoryPort.save(any(UserPenaltyHistory.class))).thenAnswer(restoreWithId(10L));

        // when
        ApplyUserPenaltyResult result = applyUserPenaltyService.applyPenalty(command);

        // then
        assertThat(user.getPenaltyCount()).isEqualTo(1);
        assertThat(result.penaltyCount()).isEqualTo(1);
        verify(updateUserPort).update(user);
    }

    @Test
    @DisplayName("패널티 부과 시 이력이 previousCount, currentCount, reason, createdBy와 함께 저장된다")
    void shouldSaveHistoryWithCorrectFields() {
        // given
        Long userId = 1L;
        Long adminId = 99L;
        String reason = "게시글 도배 행위";
        User user = createUser(userId, 2);
        ApplyUserPenaltyCommand command = new ApplyUserPenaltyCommand(userId, adminId, reason);

        when(getUserUseCase.getAllStatusUser(userId)).thenReturn(user);
        when(saveUserPenaltyHistoryPort.save(any(UserPenaltyHistory.class))).thenAnswer(restoreWithId(10L));

        // when
        applyUserPenaltyService.applyPenalty(command);

        // then
        ArgumentCaptor<UserPenaltyHistory> captor = ArgumentCaptor.forClass(UserPenaltyHistory.class);
        verify(saveUserPenaltyHistoryPort).save(captor.capture());
        UserPenaltyHistory saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getPreviousCount()).isEqualTo(2);
        assertThat(saved.getCurrentCount()).isEqualTo(3);
        assertThat(saved.getReason()).isEqualTo(reason);
        assertThat(saved.getCreatedBy()).isEqualTo(adminId);
    }

    @Test
    @DisplayName("패널티 부과 응답에 userId, penaltyCount, historyId가 포함된다")
    void shouldReturnResultWithUserIdPenaltyCountAndHistoryId() {
        // given
        Long userId = 1L;
        Long adminId = 99L;
        Long historyId = 10L;
        User user = createUser(userId, 0);
        ApplyUserPenaltyCommand command = new ApplyUserPenaltyCommand(userId, adminId, "이유");

        when(getUserUseCase.getAllStatusUser(userId)).thenReturn(user);
        when(saveUserPenaltyHistoryPort.save(any(UserPenaltyHistory.class))).thenAnswer(restoreWithId(historyId));

        // when
        ApplyUserPenaltyResult result = applyUserPenaltyService.applyPenalty(command);

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.penaltyCount()).isEqualTo(1);
        assertThat(result.historyId()).isEqualTo(historyId);
    }

    @Test
    @DisplayName("회원이 존재하지 않으면 UserNotFoundException이 발생한다")
    void shouldThrowUserNotFoundExceptionWhenUserNotExists() {
        // given
        Long userId = 999L;
        ApplyUserPenaltyCommand command = new ApplyUserPenaltyCommand(userId, 99L, "이유");
        UserNotFoundException exception = new UserNotFoundException("not found");

        when(getUserUseCase.getAllStatusUser(userId)).thenThrow(exception);

        // expect
        assertThatThrownBy(() -> applyUserPenaltyService.applyPenalty(command))
                .isSameAs(exception);

        verify(getUserUseCase).getAllStatusUser(userId);
        verifyNoMoreInteractions(getUserUseCase);
        verifyNoInteractions(updateUserPort, saveUserPenaltyHistoryPort);
    }

    @Test
    @DisplayName("이미 패널티가 부과된 회원에 다시 부과하면 횟수가 누적된다")
    void shouldAccumulatePenaltyCountOnRepeatedCalls() {
        // given
        Long userId = 1L;
        User user = createUser(userId, 5);
        ApplyUserPenaltyCommand command = new ApplyUserPenaltyCommand(userId, 99L, "이유");

        when(getUserUseCase.getAllStatusUser(userId)).thenReturn(user);
        when(saveUserPenaltyHistoryPort.save(any(UserPenaltyHistory.class))).thenAnswer(restoreWithId(20L));

        // when
        ApplyUserPenaltyResult result = applyUserPenaltyService.applyPenalty(command);

        // then
        ArgumentCaptor<UserPenaltyHistory> captor = ArgumentCaptor.forClass(UserPenaltyHistory.class);
        verify(saveUserPenaltyHistoryPort).save(captor.capture());
        assertThat(captor.getValue().getPreviousCount()).isEqualTo(5);
        assertThat(captor.getValue().getCurrentCount()).isEqualTo(6);
        assertThat(result.penaltyCount()).isEqualTo(6);
        assertThat(user.getPenaltyCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("getAllStatusUser 호출 후 update, save 순서로 수행된다")
    void shouldExecuteInOrder() {
        // given
        Long userId = 1L;
        User user = createUser(userId, 0);
        ApplyUserPenaltyCommand command = new ApplyUserPenaltyCommand(userId, 99L, "이유");

        when(getUserUseCase.getAllStatusUser(userId)).thenReturn(user);
        when(saveUserPenaltyHistoryPort.save(any(UserPenaltyHistory.class))).thenAnswer(restoreWithId(30L));

        // when
        applyUserPenaltyService.applyPenalty(command);

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
