package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserStatusAction;
import com.personal.marketnote.user.domain.user.UserStatusHistory;
import com.personal.marketnote.user.port.in.result.ActivateExpiredDeactivationResult;
import com.personal.marketnote.user.port.in.usecase.user.ActivateExpiredDeactivationUseCase;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.port.out.user.SaveUserStatusHistoryPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ActivateExpiredDeactivationService implements ActivateExpiredDeactivationUseCase {
    private static final String SYSTEM_AUTO_ACTIVATION_REASON = "비활성화 기간 만료 자동 활성화";

    private final Clock clock;
    private final FindUserPort findUserPort;
    private final UpdateUserPort updateUserPort;
    private final SaveUserStatusHistoryPort saveUserStatusHistoryPort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public ActivateExpiredDeactivationResult activateExpiredDeactivations() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<User> expiredUsers = findUserPort.findAllByDeactivatedUntilExpired(now);

        List<Long> activatedUserIds = new ArrayList<>();
        for (User user : expiredUsers) {
            try {
                activateOne(user);
                activatedUserIds.add(user.getId());
            } catch (Exception e) {
                log.error("회원 자동 활성화 실패 userId={}", user.getId(), e);
            }
        }
        return ActivateExpiredDeactivationResult.of(activatedUserIds);
    }

    private void activateOne(User user) {
        user.activateFromDeactivation();
        updateUserPort.update(user);
        saveUserStatusHistoryPort.save(
                UserStatusHistory.bySystem(
                        user.getId(),
                        UserStatusAction.ACTIVATE,
                        SYSTEM_AUTO_ACTIVATION_REASON,
                        null
                )
        );
    }
}
