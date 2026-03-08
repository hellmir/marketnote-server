package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.reward.domain.exception.InsufficientPendingPointAmountException;
import com.personal.marketnote.reward.domain.exception.PendingPointReflectionMismatchException;
import com.personal.marketnote.reward.domain.point.*;
import com.personal.marketnote.reward.exception.UserPointNotFoundException;
import com.personal.marketnote.reward.port.in.command.point.CancelPendingPointCommand;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.out.point.FindUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.UpdateUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.UpdateUserPointPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelPendingPointServiceTest {

    @InjectMocks
    private CancelPendingPointService cancelPendingPointService;

    @Mock
    private GetUserPointUseCase getUserPointUseCase;

    @Mock
    private FindUserPointHistoryPort findUserPointHistoryPort;

    @Mock
    private UpdateUserPointPort updateUserPointPort;

    @Mock
    private UpdateUserPointHistoryPort updateUserPointHistoryPort;

    private static final Long USER_ID = 1L;
    private static final Long ORDER_ID = 100L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 4, 10, 0);

    private UserPoint createUserPoint(Long amount, Long addExpectedAmount) {
        return UserPoint.from(UserPointSnapshotState.builder()
                .userId(USER_ID)
                .amount(amount)
                .addExpectedAmount(addExpectedAmount)
                .expireExpectedAmount(0L)
                .createdAt(NOW)
                .modifiedAt(NOW)
                .build());
    }

    private CancelPendingPointCommand createCommand() {
        return CancelPendingPointCommand.builder()
                .userId(USER_ID)
                .sourceType(UserPointSourceType.ORDER)
                .sourceId(ORDER_ID)
                .reason("결제 취소 적립 예정 포인트 회수")
                .build();
    }

    private UserPointHistory createPendingHistory(Long amount) {
        return UserPointHistory.from(UserPointHistorySnapshotState.builder()
                .id(1L)
                .userId(USER_ID)
                .amount(amount)
                .isReflected(Boolean.FALSE)
                .sourceType(UserPointSourceType.ORDER)
                .sourceId(ORDER_ID)
                .reason("상품 구매 적립")
                .accumulatedAt(NOW)
                .createdAt(NOW)
                .build());
    }

    @Test
    @DisplayName("적립 예정 포인트를 취소하면 pending에서 차감되고 실제 포인트는 변경되지 않는다")
    void shouldCancelPendingPointSuccessfully() {
        // given
        UserPoint userPoint = createUserPoint(1000L, 500L);
        UserPointHistory pendingHistory = createPendingHistory(500L);

        when(findUserPointHistoryPort.findUnreflectedByUserIdAndSource(
                USER_ID, UserPointSourceType.ORDER, ORDER_ID
        )).thenReturn(List.of(pendingHistory));
        when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(userPoint);
        when(updateUserPointPort.update(any(UserPoint.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(updateUserPointHistoryPort.markAsReflected(
                USER_ID, UserPointSourceType.ORDER, ORDER_ID
        )).thenReturn(1);

        CancelPendingPointCommand command = createCommand();

        // when
        UpdateUserPointResult result = cancelPendingPointService.cancelPending(command);

        // then
        assertThat(result.addExpectedAmount()).isEqualTo(0L);
        assertThat(result.amount()).isEqualTo(1000L);

        ArgumentCaptor<UserPoint> captor = ArgumentCaptor.forClass(UserPoint.class);
        verify(updateUserPointPort).update(captor.capture());
        UserPoint capturedPoint = captor.getValue();
        assertThat(capturedPoint.getAddExpectedAmount()).isEqualTo(0L);
        assertThat(capturedPoint.getAmountValue()).isEqualTo(1000L);

        verify(updateUserPointHistoryPort).markAsReflected(
                USER_ID, UserPointSourceType.ORDER, ORDER_ID
        );
    }

    @Test
    @DisplayName("여러 건의 적립 예정 포인트 이력이 있으면 합산하여 취소한다")
    void shouldCancelMultiplePendingHistories() {
        // given
        UserPoint userPoint = createUserPoint(1000L, 800L);
        UserPointHistory history1 = createPendingHistory(500L);
        UserPointHistory history2 = createPendingHistory(300L);

        when(findUserPointHistoryPort.findUnreflectedByUserIdAndSource(
                USER_ID, UserPointSourceType.ORDER, ORDER_ID
        )).thenReturn(List.of(history1, history2));
        when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(userPoint);
        when(updateUserPointPort.update(any(UserPoint.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(updateUserPointHistoryPort.markAsReflected(
                USER_ID, UserPointSourceType.ORDER, ORDER_ID
        )).thenReturn(2);

        CancelPendingPointCommand command = createCommand();

        // when
        UpdateUserPointResult result = cancelPendingPointService.cancelPending(command);

        // then
        assertThat(result.addExpectedAmount()).isEqualTo(0L);
        assertThat(result.amount()).isEqualTo(1000L);

        ArgumentCaptor<UserPoint> captor = ArgumentCaptor.forClass(UserPoint.class);
        verify(updateUserPointPort).update(captor.capture());
        UserPoint capturedPoint = captor.getValue();
        assertThat(capturedPoint.getAddExpectedAmount()).isEqualTo(0L);
        assertThat(capturedPoint.getAmountValue()).isEqualTo(1000L);

        verify(updateUserPointHistoryPort).markAsReflected(
                USER_ID, UserPointSourceType.ORDER, ORDER_ID
        );
    }

    @Test
    @DisplayName("취소 대상 적립 예정 포인트 이력이 없으면 현재 포인트를 그대로 반환한다")
    void shouldReturnCurrentPointWhenNoPendingHistoryFound() {
        // given
        UserPoint userPoint = createUserPoint(1000L, 0L);

        when(findUserPointHistoryPort.findUnreflectedByUserIdAndSource(
                USER_ID, UserPointSourceType.ORDER, ORDER_ID
        )).thenReturn(Collections.emptyList());
        when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(userPoint);

        CancelPendingPointCommand command = createCommand();

        // when
        UpdateUserPointResult result = cancelPendingPointService.cancelPending(command);

        // then
        assertThat(result.amount()).isEqualTo(1000L);
        assertThat(result.addExpectedAmount()).isEqualTo(0L);

        verify(updateUserPointPort, never()).update(any());
        verify(updateUserPointHistoryPort, never()).markAsReflected(anyLong(), any(), anyLong());
    }

    @Test
    @DisplayName("회원 포인트 정보가 존재하지 않으면 UserPointNotFoundException이 발생한다")
    void shouldThrowWhenUserPointNotFound() {
        // given
        UserPointHistory pendingHistory = createPendingHistory(500L);

        when(findUserPointHistoryPort.findUnreflectedByUserIdAndSource(
                USER_ID, UserPointSourceType.ORDER, ORDER_ID
        )).thenReturn(List.of(pendingHistory));
        when(getUserPointUseCase.getUserPoint(USER_ID))
                .thenThrow(new UserPointNotFoundException(USER_ID));

        CancelPendingPointCommand command = createCommand();

        // expect
        assertThatThrownBy(() -> cancelPendingPointService.cancelPending(command))
                .isInstanceOf(UserPointNotFoundException.class);

        verify(updateUserPointPort, never()).update(any());
    }

    @Test
    @DisplayName("적립 예정 포인트가 부족하면 InsufficientPendingPointAmountException이 발생한다")
    void shouldThrowWhenInsufficientPendingAmount() {
        // given
        UserPoint userPoint = createUserPoint(1000L, 200L);
        UserPointHistory pendingHistory = createPendingHistory(500L);

        when(findUserPointHistoryPort.findUnreflectedByUserIdAndSource(
                USER_ID, UserPointSourceType.ORDER, ORDER_ID
        )).thenReturn(List.of(pendingHistory));
        when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(userPoint);

        CancelPendingPointCommand command = createCommand();

        // expect
        assertThatThrownBy(() -> cancelPendingPointService.cancelPending(command))
                .isInstanceOf(InsufficientPendingPointAmountException.class);

        verify(updateUserPointPort, never()).update(any());
    }

    @Test
    @DisplayName("이력 반영 건수가 조회 건수와 일치하지 않으면 PendingPointReflectionMismatchException이 발생한다")
    void shouldThrowWhenReflectionCountMismatch() {
        // given
        UserPoint userPoint = createUserPoint(1000L, 500L);
        UserPointHistory pendingHistory = createPendingHistory(500L);

        when(findUserPointHistoryPort.findUnreflectedByUserIdAndSource(
                USER_ID, UserPointSourceType.ORDER, ORDER_ID
        )).thenReturn(List.of(pendingHistory));
        when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(userPoint);
        when(updateUserPointPort.update(any(UserPoint.class))).thenReturn(userPoint);
        when(updateUserPointHistoryPort.markAsReflected(
                USER_ID, UserPointSourceType.ORDER, ORDER_ID
        )).thenReturn(0);

        CancelPendingPointCommand command = createCommand();

        // expect
        assertThatThrownBy(() -> cancelPendingPointService.cancelPending(command))
                .isInstanceOf(PendingPointReflectionMismatchException.class);
    }

    @Test
    @DisplayName("취소 시 기존 pending 이력의 isReflected를 true로 업데이트한다")
    void shouldMarkExistingPendingHistoriesAsReflected() {
        // given
        UserPoint userPoint = createUserPoint(1000L, 500L);
        UserPointHistory pendingHistory = createPendingHistory(500L);

        when(findUserPointHistoryPort.findUnreflectedByUserIdAndSource(
                USER_ID, UserPointSourceType.ORDER, ORDER_ID
        )).thenReturn(List.of(pendingHistory));
        when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(userPoint);
        when(updateUserPointPort.update(any(UserPoint.class))).thenReturn(userPoint);
        when(updateUserPointHistoryPort.markAsReflected(
                USER_ID, UserPointSourceType.ORDER, ORDER_ID
        )).thenReturn(1);

        CancelPendingPointCommand command = createCommand();

        // when
        cancelPendingPointService.cancelPending(command);

        // then
        verify(updateUserPointHistoryPort).markAsReflected(
                USER_ID, UserPointSourceType.ORDER, ORDER_ID
        );
    }
}
