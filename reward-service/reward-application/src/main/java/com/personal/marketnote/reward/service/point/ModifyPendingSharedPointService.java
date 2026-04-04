package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingPointCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingSharedPointCommand;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingSharedPointUseCase;
import com.personal.marketnote.reward.port.out.point.FindUserPointPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class ModifyPendingSharedPointService implements ModifyPendingSharedPointUseCase {
    private final FindUserPointPort findUserPointPort;
    private final ModifyPendingPointUseCase modifyPendingPointUseCase;

    @Override
    public UpdateUserPointResult modifyPending(ModifyPendingSharedPointCommand command) {
        Long userId = findUserPointPort.findByUserKey(command.sharerKey().toString())
                .orElseThrow(() -> new com.personal.marketnote.reward.exception.UserPointNotFoundException(command.sharerKey().toString()))
                .getUserId();

        ModifyPendingPointCommand delegateCommand = ModifyPendingPointCommand.builder()
                .userId(userId)
                .changeType(command.changeType())
                .amount(command.amount())
                .sourceType(command.sourceType())
                .sourceId(command.sourceId())
                .reason(command.reason())
                .build();

        return modifyPendingPointUseCase.modifyPending(delegateCommand);
    }
}
