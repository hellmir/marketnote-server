package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserPenaltyHistory;
import com.personal.marketnote.user.port.in.command.UpdateUserPenaltyCountCommand;
import com.personal.marketnote.user.port.in.result.UpdateUserPenaltyCountResult;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.in.usecase.user.UpdateUserPenaltyCountUseCase;
import com.personal.marketnote.user.port.out.user.SaveUserPenaltyHistoryPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class UpdateUserPenaltyCountService implements UpdateUserPenaltyCountUseCase {
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserPort updateUserPort;
    private final SaveUserPenaltyHistoryPort saveUserPenaltyHistoryPort;

    @Override
    public UpdateUserPenaltyCountResult updatePenaltyCount(UpdateUserPenaltyCountCommand command) {
        User user = getUserUseCase.getAllStatusUser(command.userId());
        user.validateDifferentPenaltyCount(command.penaltyCount());

        int previousCount = user.getPenaltyCount();
        user.updatePenaltyCount(command.penaltyCount());
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

        return UpdateUserPenaltyCountResult.of(user.getId(), user.getPenaltyCount(), savedHistory.getId());
    }
}
