package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserStatusAction;
import com.personal.marketnote.user.domain.user.UserStatusHistory;
import com.personal.marketnote.user.port.in.command.ChangeUserStatusCommand;
import com.personal.marketnote.user.port.in.result.ChangeUserStatusResult;
import com.personal.marketnote.user.port.in.usecase.user.ChangeUserStatusUseCase;
import com.personal.marketnote.user.exception.AdminSelfDeactivationException;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.out.user.SaveUserStatusHistoryPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class ChangeUserStatusService implements ChangeUserStatusUseCase {
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserPort updateUserPort;
    private final SaveUserStatusHistoryPort saveUserStatusHistoryPort;

    @Override
    public ChangeUserStatusResult changeStatus(ChangeUserStatusCommand command) {
        UserStatusAction action = UserStatusAction.valueOf(command.action());
        validateNotSelfDeactivation(action, command.userId(), command.adminId());

        User user = getUserUseCase.getAllStatusUser(command.userId());
        applyStatusChange(user, action, command.deactivatedUntil());
        updateUserPort.update(user);

        UserStatusHistory savedHistory = saveUserStatusHistoryPort.save(
                UserStatusHistory.of(
                        command.userId(),
                        action,
                        command.reason(),
                        resolveDeactivatedUntil(action, command.deactivatedUntil()),
                        command.adminId()
                )
        );

        return ChangeUserStatusResult.of(
                user.getId(),
                user.getStatus().name(),
                user.getDeactivatedUntil(),
                savedHistory.getId()
        );
    }

    private void applyStatusChange(User user, UserStatusAction action, LocalDateTime deactivatedUntil) {
        if (action.isDeactivate()) {
            user.deactivateWithDuration(deactivatedUntil);
            return;
        }
        user.activateFromDeactivation();
    }

    private void validateNotSelfDeactivation(UserStatusAction action, Long userId, Long adminId) {
        if (action.isDeactivate() && userId.equals(adminId)) {
            throw new AdminSelfDeactivationException(adminId);
        }
    }

    private LocalDateTime resolveDeactivatedUntil(UserStatusAction action, LocalDateTime deactivatedUntil) {
        if (action.isActivate()) {
            return null;
        }
        return deactivatedUntil;
    }
}
