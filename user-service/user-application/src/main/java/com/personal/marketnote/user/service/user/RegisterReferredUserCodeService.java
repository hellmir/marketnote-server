package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.in.usecase.user.RegisterReferredUserCodeUseCase;
import com.personal.marketnote.user.port.out.event.PublishUserEventPort;
import com.personal.marketnote.user.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static com.personal.marketnote.user.exception.ExceptionMessage.USER_REFERENCE_CODE_NOT_FOUND_EXCEPTION_MESSAGE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED)
public class RegisterReferredUserCodeService implements RegisterReferredUserCodeUseCase {
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserPort updateUserPort;
    private final PublishUserEventPort publishUserEventPort;
    private final ModifyUserPointPort modifyUserPointPort;

    @Override
    public void registerReferredUserCode(Long requestUserId, String referredUserCode) {
        if (!getUserUseCase.existsUser(referredUserCode)) {
            throw new UserNotFoundException(
                    String.format(USER_REFERENCE_CODE_NOT_FOUND_EXCEPTION_MESSAGE, referredUserCode)
            );
        }

        User requestUser = getUserUseCase.getUser(requestUserId);
        requestUser.registerReferredUserCode(referredUserCode);

        updateUserPort.update(requestUser);

        User referredUser = getUserUseCase.getUser(referredUserCode);

        // Outbox 이벤트 저장 (트랜잭션 내)
        try {
            publishUserEventPort.publishUserReferralCompletedEvent(requestUser.getId(), referredUser.getId());
        } catch (Exception e) {
            requestUser.removeReferredUserCode();
            updateUserPort.update(requestUser);
            throw e;
        }

        // 추천한 회원/추천 받은 회원 포인트 적립 요청 (커밋 후 HTTP 호출)
        Long requestId = requestUser.getId();
        Long referredId = referredUser.getId();
        runAfterCommit(() -> {
            modifyUserPointPort.accrueReferralPoints(requestId, referredId);
            // TODO: Kafka 검증 완료 후 HTTP 호출 제거
        });
    }

    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }

        action.run();
    }
}
