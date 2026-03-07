package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointHistory;
import com.personal.marketnote.reward.mapper.RewardCommandToStateMapper;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingPointCommand;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingPointUseCase;
import com.personal.marketnote.reward.port.out.point.SaveUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.UpdateUserPointPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class ModifyPendingPointService implements ModifyPendingPointUseCase {
    private final GetUserPointUseCase getUserPointUseCase;
    private final UpdateUserPointPort updateUserPointPort;
    private final SaveUserPointHistoryPort saveUserPointHistoryPort;

    @Override
    public UpdateUserPointResult modifyPending(ModifyPendingPointCommand command) {
        UserPoint userPoint = getUserPointUseCase.getUserPoint(command.userId());
        addOrDeductExpectedPoint(userPoint, command);
        UserPoint updatedPoint = updateUserPointPort.update(userPoint);

        saveUserPointHistoryPort.save(
                UserPointHistory.from(
                        RewardCommandToStateMapper.mapToPendingPointHistoryCreateState(
                                command, updatedPoint.getUserId(), updatedPoint.getModifiedAt()
                        )
                )
        );

        return UpdateUserPointResult.from(updatedPoint);
    }

    private void addOrDeductExpectedPoint(UserPoint userPoint, ModifyPendingPointCommand command) {
        if (command.isAccrual()) {
            userPoint.addPendingAmount(command.amount());
            return;
        }

        userPoint.deductPendingAmount(command.amount());
    }
}
