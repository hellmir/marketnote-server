package com.personal.marketnote.user.service.authentication;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.exception.InvalidVerificationCodeException;
import com.personal.marketnote.user.exception.UserNotActiveException;
import com.personal.marketnote.user.port.in.command.VerifyCodeCommand;
import com.personal.marketnote.user.port.in.result.VerifyCodeResult;
import com.personal.marketnote.user.port.in.usecase.authentication.VerifyCodeUseCase;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.out.authentication.VerifyCodePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static com.personal.marketnote.common.domain.exception.ExceptionCode.SECOND_ERROR_CODE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED, timeout = 180)
public class VerifyCodeService implements VerifyCodeUseCase {
    private final GetUserUseCase getUserUseCase;
    private final VerifyCodePort verifyCodePort;

    @Override
    public VerifyCodeResult verifyCode(VerifyCodeCommand verifyCodeCommand) {
        String email = verifyCodeCommand.getEmail();

        if (
                !verifyCodePort.verify(
                        email, verifyCodeCommand.getVerificationCode()
                )
        ) {
            throw new InvalidVerificationCodeException(FIRST_ERROR_CODE, email);
        }

        User signedUpUser = getUserUseCase.getAllStatusUser(email);

        // 계정 활성화 여부 검증
        if (!signedUpUser.isActive()) {
            throw new UserNotActiveException(SECOND_ERROR_CODE, email);
        }

        return VerifyCodeResult.from(signedUpUser);
    }
}
