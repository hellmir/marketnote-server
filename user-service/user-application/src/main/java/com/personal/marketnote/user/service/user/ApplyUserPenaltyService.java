package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserPenaltyHistory;
import com.personal.marketnote.user.port.in.command.ApplyUserPenaltyCommand;
import com.personal.marketnote.user.port.in.result.ApplyUserPenaltyResult;
import com.personal.marketnote.user.port.in.usecase.user.ApplyUserPenaltyUseCase;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.out.user.SaveUserPenaltyHistoryPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class ApplyUserPenaltyService implements ApplyUserPenaltyUseCase {
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserPort updateUserPort;
    private final SaveUserPenaltyHistoryPort saveUserPenaltyHistoryPort;

    @Override
    public ApplyUserPenaltyResult applyPenalty(ApplyUserPenaltyCommand command) {
        User user = getUserUseCase.getAllStatusUser(command.userId());
        int previousCount = user.getPenaltyCount();
        user.addPenalty();
        updateUserPort.update(user);

        UserPenaltyHistory savedHistory = saveUserPenaltyHistoryPort.save(
                UserPenaltyHistory.of(
                        command.userId(),
                        previousCount,
                        user.getPenaltyCount(),
                        command.reason(),
                        command.adminId()
                )
        );

        return ApplyUserPenaltyResult.of(user.getId(), user.getPenaltyCount(), savedHistory.getId());
    }
}
