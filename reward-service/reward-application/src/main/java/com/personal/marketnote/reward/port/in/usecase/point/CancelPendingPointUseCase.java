package com.personal.marketnote.reward.port.in.usecase.point;

import com.personal.marketnote.reward.port.in.command.point.CancelPendingPointCommand;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;

public interface CancelPendingPointUseCase {
    UpdateUserPointResult cancelPending(CancelPendingPointCommand command);
}
