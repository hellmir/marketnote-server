package com.personal.marketnote.user.service.authentication;

import com.personal.marketnote.common.domain.exception.token.InvalidRefreshTokenException;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.port.out.authentication.FindRefreshTokenPort;
import com.personal.marketnote.user.port.out.authentication.ParseRefreshTokenPort;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshAccessTokenUseCaseTest {

    @InjectMocks
    private RefreshAccessTokenService refreshAccessTokenService;

    @Mock
    private ParseRefreshTokenPort parseRefreshTokenPort;

    @Mock
    private FindRefreshTokenPort findRefreshTokenPort;

    @Mock
    private FindUserPort findUserPort;

    @Test
    @DisplayName("유효한 리프레시 토큰으로 Access Token 재발급 검증에 성공한다")
    void validateRefreshToken_validToken_success() {
        // given
        String refreshToken = "valid-refresh-token";
        Long userId = 1L;

        when(parseRefreshTokenPort.extractUserId(refreshToken)).thenReturn(userId);
        when(findRefreshTokenPort.matchesByUserId(userId, refreshToken)).thenReturn(true);

        User activeUser = mock(User.class);
        when(activeUser.isActive()).thenReturn(true);
        when(findUserPort.findAllStatusUserById(userId)).thenReturn(Optional.of(activeUser));

        // expect
        assertThatCode(() -> refreshAccessTokenService.validateRefreshToken(refreshToken))
                .doesNotThrowAnyException();

        verify(parseRefreshTokenPort).extractUserId(refreshToken);
        verify(findRefreshTokenPort).matchesByUserId(userId, refreshToken);
        verify(findUserPort).findAllStatusUserById(userId);
    }

    @Test
    @DisplayName("화이트리스트에 없는 리프레시 토큰으로 재발급 시 실패한다")
    void validateRefreshToken_notInWhitelist_throws() {
        // given
        String refreshToken = "not-in-whitelist";
        Long userId = 2L;

        when(parseRefreshTokenPort.extractUserId(refreshToken)).thenReturn(userId);
        when(findRefreshTokenPort.matchesByUserId(userId, refreshToken)).thenReturn(false);

        // expect
        assertThatThrownBy(() -> refreshAccessTokenService.validateRefreshToken(refreshToken))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(findUserPort, never()).findAllStatusUserById(any());
    }

    @Test
    @DisplayName("화이트리스트 토큰 해시 불일치 시 실패한다")
    void validateRefreshToken_hashMismatch_throws() {
        // given
        String refreshToken = "mismatched-hash-token";
        Long userId = 3L;

        when(parseRefreshTokenPort.extractUserId(refreshToken)).thenReturn(userId);
        when(findRefreshTokenPort.matchesByUserId(userId, refreshToken)).thenReturn(false);

        // expect
        assertThatThrownBy(() -> refreshAccessTokenService.validateRefreshToken(refreshToken))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(findUserPort, never()).findAllStatusUserById(any());
    }

    @Test
    @DisplayName("비활성 사용자의 리프레시 토큰으로 재발급 시 실패한다")
    void validateRefreshToken_inactiveUser_throws() {
        // given
        String refreshToken = "inactive-user-token";
        Long userId = 4L;

        when(parseRefreshTokenPort.extractUserId(refreshToken)).thenReturn(userId);
        when(findRefreshTokenPort.matchesByUserId(userId, refreshToken)).thenReturn(true);

        User inactiveUser = mock(User.class);
        when(inactiveUser.isActive()).thenReturn(false);
        when(findUserPort.findAllStatusUserById(userId)).thenReturn(Optional.of(inactiveUser));

        // expect
        assertThatThrownBy(() -> refreshAccessTokenService.validateRefreshToken(refreshToken))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 리프레시 토큰으로 재발급 시 실패한다")
    void validateRefreshToken_userNotFound_throws() {
        // given
        String refreshToken = "missing-user-token";
        Long userId = 5L;

        when(parseRefreshTokenPort.extractUserId(refreshToken)).thenReturn(userId);
        when(findRefreshTokenPort.matchesByUserId(userId, refreshToken)).thenReturn(true);
        when(findUserPort.findAllStatusUserById(userId)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> refreshAccessTokenService.validateRefreshToken(refreshToken))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("userId가 null인 리프레시 토큰으로 재발급 시 실패한다")
    void validateRefreshToken_nullUserId_throws() {
        // given
        String refreshToken = "null-userid-token";

        when(parseRefreshTokenPort.extractUserId(refreshToken)).thenReturn(null);

        // expect
        assertThatThrownBy(() -> refreshAccessTokenService.validateRefreshToken(refreshToken))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verifyNoInteractions(findRefreshTokenPort, findUserPort);
    }
}
