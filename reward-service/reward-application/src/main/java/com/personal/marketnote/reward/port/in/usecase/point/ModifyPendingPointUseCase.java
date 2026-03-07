package com.personal.marketnote.reward.port.in.usecase.point;

import com.personal.marketnote.reward.port.in.command.point.ModifyPendingPointCommand;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;

public interface ModifyPendingPointUseCase {
    UpdateUserPointResult modifyPending(ModifyPendingPointCommand command);
}
