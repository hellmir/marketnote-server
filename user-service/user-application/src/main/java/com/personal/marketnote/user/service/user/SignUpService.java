package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.PasswordNoValueException;
import com.personal.marketnote.common.utility.RandomCodeGenerator;
import com.personal.marketnote.user.domain.user.LoginHistory;
import com.personal.marketnote.user.domain.user.Terms;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.exception.InvalidVerificationCodeException;
import com.personal.marketnote.user.exception.UserExistsException;
import com.personal.marketnote.user.exception.UserNotActiveException;
import com.personal.marketnote.user.port.in.command.SignUpCommand;
import com.personal.marketnote.user.port.in.mapper.UserCommandToStateMapper;
import com.personal.marketnote.user.port.in.result.SignUpResult;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.in.usecase.user.SignUpUseCase;
import com.personal.marketnote.user.port.out.authentication.VerifyCodePort;
import com.personal.marketnote.user.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.user.port.out.user.*;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.*;
import static com.personal.marketnote.user.exception.ExceptionMessage.*;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED)
public class SignUpService implements SignUpUseCase {
    private final GetUserUseCase getUserUseCase;
    private final SaveUserPort saveUserPort;
    private final FindUserPort findUserPort;
    private final FindTermsPort findTermsPort;
    private final PasswordEncoder passwordEncoder;
    private final UpdateUserPort updateUserPort;
    private final VerifyCodePort verifyCodePort;
    private final SaveLoginHistoryPort saveLoginHistoryPort;
    private final ModifyUserPointPort modifyUserPointPort;

    @Override
    public SignUpResult signUp(SignUpCommand signUpCommand, AuthVendor authVendor, String oidcId, String ipAddress) {
        validate(signUpCommand, authVendor, oidcId);

        String email = signUpCommand.email();
        String password = signUpCommand.password();

        // 이메일 가입 정보가 있는 경우 기존 회원 엔티티에 통합
        if (findUserPort.existsByEmail(email)) {
            AtomicBoolean isNewUser = new AtomicBoolean(false);
            User signedUpUser = addLoginAccountInfo(email, password, authVendor, oidcId, ipAddress, isNewUser);

            return SignUpResult.from(signedUpUser, isNewUser.get());
        }

        List<Terms> terms = findTermsPort.findAll();
        String referenceCode = RandomCodeGenerator.generateReferenceCode();

        User signedUpUser = saveUserPort.save(
                User.from(
                        UserCommandToStateMapper.mapToState(
                                signUpCommand,
                                authVendor,
                                oidcId,
                                terms,
                                referenceCode,
                                passwordEncoder
                        )
                )
        );

        registerUserPointAfterCommit(signedUpUser.getId(), signedUpUser.getUserKey().toString());

        saveLoginHistoryPort.saveLoginHistory(
                LoginHistory.of(signedUpUser, authVendor, ipAddress)
        );

        return SignUpResult.from(signedUpUser, true);
    }

    private void validate(SignUpCommand signUpCommand, AuthVendor authVendor, String oidcId) {
        if (authVendor.isNative() && !signUpCommand.hasPassword()) {
            throw new PasswordNoValueException(FIRST_ERROR_CODE);
        }

        if (!authVendor.isNative() && findUserPort.existsByAuthVendorAndOidcId(authVendor, oidcId)) {
            throw new UserExistsException(
                    String.format(OIDC_ID_ALREADY_EXISTS_EXCEPTION_MESSAGE, SECOND_ERROR_CODE, oidcId)
            );
        }

        String nickname = signUpCommand.nickname();
        if (findUserPort.existsByNickname(nickname)) {
            throw new UserExistsException(
                    String.format(NICKNAME_ALREADY_EXISTS_EXCEPTION_MESSAGE, THIRD_ERROR_CODE, nickname)
            );
        }

        String phoneNumber = signUpCommand.phoneNumber();
        if (signUpCommand.hasPhoneNumber() && findUserPort.existsByPhoneNumber(phoneNumber)) {
            throw new UserExistsException(
                    String.format(PHONE_NUMBER_ALREADY_EXISTS_EXCEPTION_MESSAGE, FOURTH_ERROR_CODE, phoneNumber)
            );
        }

        // 이메일 인증 코드 검증
        String email = signUpCommand.email();
        if (!verifyCodePort.verify(email, signUpCommand.verificationCode())) {
            throw new InvalidVerificationCodeException(FIFTH_ERROR_CODE, email);
        }
    }

    private User addLoginAccountInfo(
            String email, String password, AuthVendor authVendor, String oidcId, String ipAddress, AtomicBoolean isNewUser
    ) {
        User signedUpUser = getUserUseCase.getAllStatusUser(email);

        // 탈퇴 회원인 경우 재활성화
        if (signedUpUser.isWithdrawn()) {
            signedUpUser.cancelWithdrawal();
            isNewUser.set(true);
        }

        // 계정 활성화 여부 검증
        if (signedUpUser.isActive()) {
            signedUpUser.addLoginAccountInfo(authVendor, oidcId, password, passwordEncoder);
            updateUserPort.update(signedUpUser);
            saveLoginHistoryPort.saveLoginHistory(
                    LoginHistory.of(signedUpUser, authVendor, ipAddress)
            );

            return signedUpUser;
        }

        throw new UserNotActiveException(SIXTH_ERROR_CODE, email);
    }

    private void registerUserPointAfterCommit(Long userId, String userKey) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    modifyUserPointPort.registerUserPoint(userId, userKey);
                }
            });
            return;
        }

        modifyUserPointPort.registerUserPoint(userId, userKey);
    }
}
