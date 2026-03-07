package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.reward.domain.exception.InsufficientPendingPointAmountException;
import com.personal.marketnote.reward.domain.point.*;
import com.personal.marketnote.reward.exception.UserPointNotFoundException;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingPointCommand;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.out.point.SaveUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.UpdateUserPointPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModifyPendingPointServiceTest {

    @InjectMocks
    private ModifyPendingPointService modifyPendingPointService;

    @Mock
    private GetUserPointUseCase getUserPointUseCase;

    @Mock
    private UpdateUserPointPort updateUserPointPort;

    @Mock
    private SaveUserPointHistoryPort saveUserPointHistoryPort;

    private UserPoint createUserPoint(Long amount, Long addExpectedAmount) {
        return UserPoint.from(UserPointSnapshotState.builder()
                .userId(1L)
                .amount(amount)
                .addExpectedAmount(addExpectedAmount)
                .expireExpectedAmount(0L)
                .createdAt(LocalDateTime.of(2026, 3, 4, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 3, 4, 10, 0))
                .build());
    }

    private ModifyPendingPointCommand createCommand(UserPointChangeType changeType, Long amount) {
        return ModifyPendingPointCommand.builder()
                .userId(1L)
                .changeType(changeType)
                .amount(amount)
                .sourceType(UserPointSourceType.ORDER)
                .sourceId(100L)
                .reason("주문 결제 적립 예정")
                .build();
    }

    @Test
    @DisplayName("적립 예정 포인트를 추가하면 addExpectedAmount가 증가하고 이력이 저장된다")
    void shouldAddPendingAmountAndSaveHistory() {
        // given
        UserPoint userPoint = createUserPoint(1000L, 0L);
        when(getUserPointUseCase.getUserPoint(1L)).thenReturn(userPoint);
        when(updateUserPointPort.update(any(UserPoint.class))).thenReturn(userPoint);
        when(saveUserPointHistoryPort.save(any(UserPointHistory.class)))
                .thenReturn(UserPointHistory.from(UserPointHistoryCreateState.builder()
                        .userId(1L).amount(500L).isReflected(false)
                        .sourceType(UserPointSourceType.ORDER).sourceId(100L)
                        .reason("주문 결제 적립 예정").accumulatedAt(LocalDateTime.now())
                        .build()));

        ModifyPendingPointCommand command = createCommand(UserPointChangeType.ACCRUAL, 500L);

        // when
        UpdateUserPointResult result = modifyPendingPointService.modifyPending(command);

        // then
        assertThat(result.addExpectedAmount()).isEqualTo(500L);

        verify(updateUserPointPort).update(any(UserPoint.class));

        ArgumentCaptor<UserPointHistory> historyCaptor = ArgumentCaptor.forClass(UserPointHistory.class);
        verify(saveUserPointHistoryPort).save(historyCaptor.capture());
        UserPointHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getIsReflected()).isFalse();
        assertThat(savedHistory.getAmount()).isEqualTo(500L);
        assertThat(savedHistory.getSourceType()).isEqualTo(UserPointSourceType.ORDER);
    }

    @Test
    @DisplayName("적립 예정 포인트를 차감하면 addExpectedAmount가 감소하고 이력이 저장된다")
    void shouldDeductPendingAmountAndSaveHistory() {
        // given
        UserPoint userPoint = createUserPoint(1000L, 500L);
        when(getUserPointUseCase.getUserPoint(1L)).thenReturn(userPoint);
        when(updateUserPointPort.update(any(UserPoint.class))).thenReturn(userPoint);
        when(saveUserPointHistoryPort.save(any(UserPointHistory.class)))
                .thenReturn(UserPointHistory.from(UserPointHistoryCreateState.builder()
                        .userId(1L).amount(-300L).isReflected(false)
                        .sourceType(UserPointSourceType.ORDER).sourceId(100L)
                        .reason("주문 취소 적립 예정 차감").accumulatedAt(LocalDateTime.now())
                        .build()));

        ModifyPendingPointCommand command = createCommand(UserPointChangeType.DEDUCTION, 300L);

        // when
        UpdateUserPointResult result = modifyPendingPointService.modifyPending(command);

        // then
        assertThat(result.addExpectedAmount()).isEqualTo(200L);

        ArgumentCaptor<UserPointHistory> historyCaptor = ArgumentCaptor.forClass(UserPointHistory.class);
        verify(saveUserPointHistoryPort).save(historyCaptor.capture());
        UserPointHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getIsReflected()).isFalse();
        assertThat(savedHistory.getAmount()).isEqualTo(-300L);
    }

    @Test
    @DisplayName("적립 예정 포인트보다 큰 금액을 차감하면 InsufficientPendingPointAmountException이 발생한다")
    void shouldThrowWhenInsufficientPendingAmount() {
        // given
        UserPoint userPoint = createUserPoint(1000L, 200L);
        when(getUserPointUseCase.getUserPoint(1L)).thenReturn(userPoint);

        ModifyPendingPointCommand command = createCommand(UserPointChangeType.DEDUCTION, 500L);

        // expect
        assertThatThrownBy(() -> modifyPendingPointService.modifyPending(command))
                .isInstanceOf(InsufficientPendingPointAmountException.class);

        verify(updateUserPointPort, never()).update(any());
        verify(saveUserPointHistoryPort, never()).save(any());
    }

    @Test
    @DisplayName("회원 포인트 정보가 존재하지 않으면 UserPointNotFoundException이 발생한다")
    void shouldThrowWhenUserPointNotFound() {
        // given
        when(getUserPointUseCase.getUserPoint(1L))
                .thenThrow(new UserPointNotFoundException(1L));

        ModifyPendingPointCommand command = createCommand(UserPointChangeType.ACCRUAL, 500L);

        // expect
        assertThatThrownBy(() -> modifyPendingPointService.modifyPending(command))
                .isInstanceOf(UserPointNotFoundException.class);
    }
}
