package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.exception.PendingPointReflectionMismatchException;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointHistory;
import com.personal.marketnote.reward.port.in.command.point.CancelPendingPointCommand;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;
import com.personal.marketnote.reward.port.in.usecase.point.CancelPendingPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.out.point.FindUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.UpdateUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.UpdateUserPointPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class CancelPendingPointService implements CancelPendingPointUseCase {
    private final GetUserPointUseCase getUserPointUseCase;
    private final FindUserPointHistoryPort findUserPointHistoryPort;
    private final UpdateUserPointPort updateUserPointPort;
    private final UpdateUserPointHistoryPort updateUserPointHistoryPort;

    @Override
    public UpdateUserPointResult cancelPending(CancelPendingPointCommand command) {
        List<UserPointHistory> pendingHistories = findUserPointHistoryPort.findUnreflectedByUserIdAndSource(
                command.userId(), command.sourceType(), command.sourceId()
        );

        // 이미 취소 처리된 경우 현재 포인트를 그대로 반환 (멱등성 보장 - 결제 취소 재시도 대응)
        if (pendingHistories.isEmpty()) {
            UserPoint userPoint = getUserPointUseCase.getUserPoint(command.userId());
            return UpdateUserPointResult.from(userPoint);
        }

        Long totalAmount = calculateTotalPendingAmount(pendingHistories);

        UserPoint userPoint = getUserPointUseCase.getUserPoint(command.userId());
        userPoint.deductPendingAmount(totalAmount);
        UserPoint updatedPoint = updateUserPointPort.update(userPoint);

        int updatedCount = updateUserPointHistoryPort.markAsReflected(
                command.userId(), command.sourceType(), command.sourceId()
        );
        validateReflectionCount(pendingHistories.size(), updatedCount);

        return UpdateUserPointResult.from(updatedPoint);
    }

    private Long calculateTotalPendingAmount(List<UserPointHistory> pendingHistories) {
        long total = 0L;
        for (UserPointHistory history : pendingHistories) {
            total = Math.addExact(total, history.getAmount());
        }
        return total;
    }

    private void validateReflectionCount(int expectedCount, int actualCount) {
        if (expectedCount != actualCount) {
            throw new PendingPointReflectionMismatchException(expectedCount, actualCount);
        }
    }
}
