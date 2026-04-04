package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingPointCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingSharedPointCommand;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingSharedPointUseCase;
import com.personal.marketnote.reward.port.out.point.FindUserPointPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class ModifyPendingSharedPointService implements ModifyPendingSharedPointUseCase {
    private final FindUserPointPort findUserPointPort;
    private final ModifyPendingPointUseCase modifyPendingPointUseCase;

    @Override
    public UpdateUserPointResult modifyPending(ModifyPendingSharedPointCommand command) {
        UserPoint sharerPoint = findUserPointPort.findByUserKey(command.sharerKey().toString())
                .orElseThrow(() -> new com.personal.marketnote.reward.exception.UserPointNotFoundException(command.sharerKey().toString()));

        Long sharerId = sharerPoint.getUserId();

        if (FormatValidator.hasValue(command.buyerId()) && Objects.equals(command.buyerId(), sharerId)) {
            log.info("구매자와 공유자가 동일인 (적립 예정 포인트 변경 스킵). buyerId={}, sharerKey={}",
                    command.buyerId(), command.sharerKey());
            return null;
        }

        ModifyPendingPointCommand delegateCommand = ModifyPendingPointCommand.builder()
                .userId(sharerId)
                .changeType(command.changeType())
                .amount(command.amount())
                .sourceType(command.sourceType())
                .sourceId(command.sourceId())
                .reason(command.reason())
                .build();

        return modifyPendingPointUseCase.modifyPending(delegateCommand);
    }
}
