package com.personal.marketnote.reward.port.in.usecase.point;

import com.personal.marketnote.reward.port.in.command.point.ModifyPendingSharedPointCommand;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;

public interface ModifyPendingSharedPointUseCase {
    UpdateUserPointResult modifyPending(ModifyPendingSharedPointCommand command);
}
