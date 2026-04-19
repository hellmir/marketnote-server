package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.exception.UserPointNotFoundException;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointHistory;
import com.personal.marketnote.reward.mapper.RewardCommandToStateMapper;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyUserPointUseCase;
import com.personal.marketnote.reward.port.out.point.FindUserPointPort;
import com.personal.marketnote.reward.port.out.point.SaveUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.UpdateUserPointPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class ModifyUserPointService implements ModifyUserPointUseCase {
    private final GetUserPointUseCase getUserPointUseCase;
    private final FindUserPointPort findUserPointPort;
    private final UpdateUserPointPort updateUserPointPort;
    private final SaveUserPointHistoryPort saveUserPointHistoryPort;

    @Override
    public UpdateUserPointResult modify(ModifyUserPointCommand command) {
        UserPoint userPoint = getTargetUserPoint(command);
        userPoint.changeAmount(command.isAccrual(), command.amount());
        UserPoint updatedPoint = updateUserPointPort.update(userPoint);

        saveUserPointHistoryPort.save(
                UserPointHistory.from(
                        RewardCommandToStateMapper.mapToUserPointHistoryCreateState(
                                command, updatedPoint.getUserId(), updatedPoint.getModifiedAt()
                        )
                )
        );

        return UpdateUserPointResult.from(updatedPoint);
    }

    private UserPoint getTargetUserPoint(ModifyUserPointCommand command) {
        if (!command.isAccrual() && FormatValidator.hasValue(command.userId())) {
            return findUserPointPort.findByUserIdForUpdate(command.userId())
                    .orElseThrow(() -> new UserPointNotFoundException(command.userId()));
        }

        Long userId = command.userId();
        if (FormatValidator.hasValue(userId)) {
            return getUserPointUseCase.getUserPoint(userId);
        }

        return getUserPointUseCase.getUserPoint(command.userKey());
    }
}
