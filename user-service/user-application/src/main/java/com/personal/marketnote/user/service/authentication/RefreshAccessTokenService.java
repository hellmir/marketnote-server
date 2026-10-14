package com.personal.marketnote.user.service.authentication;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.domain.exception.token.InvalidRefreshTokenException;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.port.in.usecase.authentication.RefreshAccessTokenUseCase;
import com.personal.marketnote.user.port.out.authentication.FindRefreshTokenPort;
import com.personal.marketnote.user.port.out.authentication.ParseRefreshTokenPort;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class RefreshAccessTokenService implements RefreshAccessTokenUseCase {
    private final ParseRefreshTokenPort parseRefreshTokenPort;
    private final FindRefreshTokenPort findRefreshTokenPort;
    private final FindUserPort findUserPort;

    @Override
    public void validateRefreshToken(String refreshToken) {
        Long userId = parseRefreshTokenPort.extractUserId(refreshToken);

        if (FormatValidator.hasNoValue(userId)) {
            throw new InvalidRefreshTokenException("유효하지 않은 리프레시 토큰");
        }

        if (!findRefreshTokenPort.matchesByUserId(userId, refreshToken)) {
            throw new InvalidRefreshTokenException("유효하지 않은 리프레시 토큰");
        }

        Optional<User> userOptional = findUserPort.findAllStatusUserById(userId);
        if (userOptional.isEmpty() || !userOptional.get().isActive()) {
            throw new InvalidRefreshTokenException("유효하지 않은 리프레시 토큰");
        }
    }
}
