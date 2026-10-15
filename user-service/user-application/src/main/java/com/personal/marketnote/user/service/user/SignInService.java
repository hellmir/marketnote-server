package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.domain.exception.accessdenied.LoginFailedException;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.domain.user.LoginHistory;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.exception.UserNotActiveException;
import com.personal.marketnote.user.domain.user.FingerprintSource;
import com.personal.marketnote.user.port.in.command.SignInCommand;
import com.personal.marketnote.user.port.in.event.UserFingerprintSavedEvent;
import com.personal.marketnote.user.port.in.result.SignInResult;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.in.usecase.user.SignInUseCase;
import com.personal.marketnote.user.port.out.authentication.GenerateTokenPort;
import com.personal.marketnote.user.port.out.authentication.SaveRefreshTokenPort;
import com.personal.marketnote.user.port.out.user.SaveLoginHistoryPort;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static com.personal.marketnote.common.domain.exception.ExceptionCode.SECOND_ERROR_CODE;
import static com.personal.marketnote.user.exception.ExceptionMessage.LOGIN_FAILED_EXCEPTION_MESSAGE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED)
public class SignInService implements SignInUseCase {
    private final GetUserUseCase getUserUseCase;
    private final PasswordEncoder passwordEncoder;
    private final SaveLoginHistoryPort saveLoginHistoryPort;
    private final GenerateTokenPort generateTokenPort;
    private final SaveRefreshTokenPort saveRefreshTokenPort;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${spring.jwt.refresh-token.ttl:1209600000}")
    private long refreshTokenTtlMillis;

    @Override
    public SignInResult signIn(SignInCommand signInCommand, AuthVendor authVendor, String oidcId, String ipAddress) {
        User signedUpUser = getSignedUpUser(signInCommand, authVendor, oidcId);

        // 계정 활성화 여부 검증
        if (!signedUpUser.isActive()) {
            throw new UserNotActiveException(SECOND_ERROR_CODE, signInCommand.getEmail());
        }

        saveLoginHistoryPort.saveLoginHistory(
                LoginHistory.of(signedUpUser, authVendor, ipAddress)
        );

        publishFingerprintEvent(signedUpUser.getId(), signInCommand.getFingerprint());
        return generateTokensAndBuildResult(signedUpUser, authVendor);
    }

    private SignInResult generateTokensAndBuildResult(User user, AuthVendor authVendor) {
        Long userId = user.getId();
        String subject = String.valueOf(userId);
        List<String> roleIds = List.of(user.getRole().getId());

        String accessToken = generateTokenPort.generateAccessToken(subject, userId, roleIds, authVendor);
        String refreshToken = generateTokenPort.generateRefreshToken(subject, userId, roleIds, authVendor);

        saveRefreshTokenPort.saveByUserId(userId, refreshToken, refreshTokenTtlMillis);

        return SignInResult.of(user, accessToken, refreshToken);
    }

    private User getSignedUpUser(SignInCommand signInCommand, AuthVendor authVendor, String oidcId) {
        if (FormatValidator.hasValue(authVendor) && !authVendor.isNative() && FormatValidator.hasValue(oidcId)) {
            return getUserUseCase.getAllStatusUser(authVendor, oidcId);
        }

        String email = signInCommand.getEmail();
        if (FormatValidator.hasValue(email)) {
            User signedUpUser;

            try {
                signedUpUser = getUserUseCase.getAllStatusUser(email);
            } catch (UserNotFoundException e) {
                throw new LoginFailedException(String.format(LOGIN_FAILED_EXCEPTION_MESSAGE, FIRST_ERROR_CODE));
            }

            validatePassword(signedUpUser, signInCommand.getPassword());

            return signedUpUser;
        }

        throw new LoginFailedException(String.format(LOGIN_FAILED_EXCEPTION_MESSAGE, FIRST_ERROR_CODE));
    }

    private void validatePassword(User user, String password) {
        if (!user.isValidPassword(passwordEncoder, password)) {
            throw new LoginFailedException(String.format(LOGIN_FAILED_EXCEPTION_MESSAGE, FIRST_ERROR_CODE));
        }
    }

    private void publishFingerprintEvent(Long userId, String fingerprint) {
        if (FormatValidator.hasNoValue(fingerprint)) {
            return;
        }

        applicationEventPublisher.publishEvent(
                new UserFingerprintSavedEvent(userId, fingerprint, FingerprintSource.LOGIN)
        );
    }
}
