package com.personal.marketnote.reward.port.in.usecase.point;

import com.personal.marketnote.reward.port.in.command.point.ConfirmPendingPointCommand;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;

public interface ConfirmPendingPointUseCase {
    UpdateUserPointResult confirmPending(ConfirmPendingPointCommand command);
}
