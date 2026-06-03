package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.exception.MutualReferralNotAllowedException;
import com.personal.marketnote.user.exception.SelfReferralNotAllowedException;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.in.usecase.user.RegisterReferredUserCodeUseCase;
import com.personal.marketnote.user.port.out.event.PublishUserEventPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static com.personal.marketnote.user.exception.ExceptionMessage.USER_REFERENCE_CODE_NOT_FOUND_EXCEPTION_MESSAGE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED)
public class RegisterReferredUserCodeService implements RegisterReferredUserCodeUseCase {
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserPort updateUserPort;
    private final PublishUserEventPort publishUserEventPort;

    @Override
    public void registerReferredUserCode(Long requestUserId, String referredUserCode) {
        if (!getUserUseCase.existsUser(referredUserCode)) {
            throw new UserNotFoundException(
                    String.format(USER_REFERENCE_CODE_NOT_FOUND_EXCEPTION_MESSAGE, referredUserCode)
            );
        }

        User requestUser = getUserUseCase.getUser(requestUserId);
        validateNotSelfReferral(requestUser, referredUserCode);

        User referredUser = getUserUseCase.getUser(referredUserCode);
        validateNotMutualReferral(referredUser, requestUser);

        requestUser.registerReferredUserCode(referredUserCode);
        updateUserPort.update(requestUser);

        // Outbox 이벤트 저장 (트랜잭션 내)
        // [#929][#1037] 추천 포인트 적립은 Kafka Consumer(UserReferralCompletedRewardConsumer)로 전환 완료
        try {
            publishUserEventPort.publishUserReferralCompletedEvent(requestUser.getId(), referredUser.getId());
        } catch (Exception e) {
            requestUser.removeReferredUserCode();
            updateUserPort.update(requestUser);
            throw e;
        }
    }

    private void validateNotSelfReferral(User requestUser, String referredUserCode) {
        if (requestUser.isSelfReferral(referredUserCode)) {
            throw new SelfReferralNotAllowedException(FIRST_ERROR_CODE);
        }
    }

    private void validateNotMutualReferral(User referredUser, User requestUser) {
        if (referredUser.isMutualReferralWith(requestUser)) {
            throw new MutualReferralNotAllowedException(FIRST_ERROR_CODE);
        }
    }
}
