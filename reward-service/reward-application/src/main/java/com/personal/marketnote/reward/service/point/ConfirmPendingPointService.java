package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.exception.PendingPointReflectionMismatchException;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointHistory;
import com.personal.marketnote.reward.mapper.RewardCommandToStateMapper;
import com.personal.marketnote.reward.port.in.command.point.ConfirmPendingPointCommand;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;
import com.personal.marketnote.reward.port.in.usecase.point.ConfirmPendingPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.out.point.FindUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.SaveUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.UpdateUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.UpdateUserPointPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class ConfirmPendingPointService implements ConfirmPendingPointUseCase {
    private final GetUserPointUseCase getUserPointUseCase;
    private final FindUserPointHistoryPort findUserPointHistoryPort;
    private final UpdateUserPointPort updateUserPointPort;
    private final UpdateUserPointHistoryPort updateUserPointHistoryPort;
    private final SaveUserPointHistoryPort saveUserPointHistoryPort;

    @Override
    public UpdateUserPointResult confirmPending(ConfirmPendingPointCommand command) {
        List<UserPointHistory> pendingHistories = findUserPointHistoryPort.findUnreflectedByUserIdAndSource(
                command.userId(), command.sourceType(), command.sourceId()
        );

        if (pendingHistories.isEmpty()) {
            log.info("이미 확정 처리된 적립 예정 포인트 (멱등 처리). userId={}, sourceType={}, sourceId={}",
                    command.userId(), command.sourceType(), command.sourceId());
            UserPoint userPoint = getUserPointUseCase.getUserPoint(command.userId());
            return UpdateUserPointResult.from(userPoint);
        }

        Long totalAmount = calculateTotalPendingAmount(pendingHistories);

        UserPoint userPoint = getUserPointUseCase.getUserPoint(command.userId());
        userPoint.confirmPendingAmount(totalAmount);
        UserPoint updatedPoint = updateUserPointPort.update(userPoint);

        int updatedCount = updateUserPointHistoryPort.markAsReflected(
                command.userId(), command.sourceType(), command.sourceId()
        );
        validateReflectionCount(pendingHistories.size(), updatedCount);

        saveConfirmedHistory(command, totalAmount, updatedPoint);

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

    private void saveConfirmedHistory(
            ConfirmPendingPointCommand command,
            Long totalAmount,
            UserPoint updatedPoint
    ) {
        saveUserPointHistoryPort.save(
                UserPointHistory.from(
                        RewardCommandToStateMapper.mapToConfirmedPointHistoryCreateState(
                                command, totalAmount, updatedPoint.getUserId(), updatedPoint.getModifiedAt()
                        )
                )
        );
    }
}
