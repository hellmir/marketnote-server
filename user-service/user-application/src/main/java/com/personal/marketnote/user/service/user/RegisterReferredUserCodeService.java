package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.in.usecase.user.RegisterReferredUserCodeUseCase;
import com.personal.marketnote.user.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static com.personal.marketnote.user.exception.ExceptionMessage.USER_REFERENCE_CODE_NOT_FOUND_EXCEPTION_MESSAGE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED)
public class RegisterReferredUserCodeService implements RegisterReferredUserCodeUseCase {
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserPort updateUserPort;
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

        // 추천한 회원/추천 받은 회원 포인트 적립 요청
        // FIXME: Kafka 이벤트 Production으로 변경
        runAfterCommit(() -> modifyUserPointPort.accrueReferralPoints(requestUser.getId(), referredUser.getId()));
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
