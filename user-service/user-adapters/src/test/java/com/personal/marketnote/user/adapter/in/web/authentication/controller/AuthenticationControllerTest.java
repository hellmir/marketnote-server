package com.personal.marketnote.user.adapter.in.web.authentication.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.user.adapter.in.web.authentication.response.RefreshedAccessTokenResponse;
import com.personal.marketnote.user.adapter.in.web.authentication.response.WebBasedTokenRefreshResponse;
import com.personal.marketnote.user.adapter.out.vendor.authentication.WebBasedAuthenticationServiceAdapter;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserSnapshotState;
import com.personal.marketnote.user.port.in.usecase.authentication.SendEmailVerificationUseCase;
import com.personal.marketnote.user.port.in.usecase.authentication.VerifyCodeUseCase;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import com.personal.marketnote.user.utility.jwt.JwtUtil;
import com.personal.marketnote.user.utility.jwt.claims.TokenClaims;
import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @InjectMocks
    private AuthenticationController authenticationController;

    @Mock
    private WebBasedAuthenticationServiceAdapter authServiceAdapter;

    @Mock
    private SendEmailVerificationUseCase sendEmailVerificationUseCase;

    @Mock
    private VerifyCodeUseCase verifyCodeUseCase;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private FindUserPort findUserPort;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Nested
    @DisplayName("refreshAccessToken - 토큰 갱신 Redis 검증")
    class RefreshAccessTokenTest {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 액세스 토큰을 재발급한다")
        void shouldRefreshAccessTokenWithValidRefreshToken() throws NoSuchAlgorithmException {
            // given
            String refreshToken = "valid-refresh-token";
            Long userId = 1L;

            TokenClaims claims = mock(TokenClaims.class);
            when(claims.getUserId()).thenReturn(userId);
            when(jwtUtil.parseRefreshToken(refreshToken)).thenReturn(claims);

            String expectedRedisValue = "r" + sha256Hex(refreshToken);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("refreshToken:1")).thenReturn(expectedRedisValue);

            User activeUser = createActiveUser(userId);
            when(findUserPort.findAllStatusUserById(userId)).thenReturn(Optional.of(activeUser));

            RefreshedAccessTokenResponse accessTokenResponse = new RefreshedAccessTokenResponse("new-access-token");
            WebBasedTokenRefreshResponse refreshResponse = new WebBasedTokenRefreshResponse(new HttpHeaders(), accessTokenResponse);
            when(authServiceAdapter.issueNewAccessToken(refreshToken)).thenReturn(refreshResponse);

            // when
            ResponseEntity<BaseResponse<RefreshedAccessTokenResponse>> response = authenticationController.refreshAccessToken(refreshToken);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(authServiceAdapter).issueNewAccessToken(refreshToken);
        }

        @Test
        @DisplayName("userId가 null이면 UNAUTHORIZED를 반환한다")
        void shouldReturnUnauthorizedWhenUserIdIsNull() {
            // given
            String refreshToken = "token-without-userid";

            TokenClaims claims = mock(TokenClaims.class);
            when(claims.getUserId()).thenReturn(null);
            when(jwtUtil.parseRefreshToken(refreshToken)).thenReturn(claims);

            // when
            ResponseEntity<BaseResponse<RefreshedAccessTokenResponse>> response = authenticationController.refreshAccessToken(refreshToken);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verifyNoInteractions(authServiceAdapter);
        }

        @Test
        @DisplayName("Redis에 저장된 토큰과 불일치하면 UNAUTHORIZED를 반환한다")
        void shouldReturnUnauthorizedWhenRedisTokenMismatch() {
            // given
            String refreshToken = "mismatched-token";
            Long userId = 1L;

            TokenClaims claims = mock(TokenClaims.class);
            when(claims.getUserId()).thenReturn(userId);
            when(jwtUtil.parseRefreshToken(refreshToken)).thenReturn(claims);

            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("refreshToken:1")).thenReturn("r_different_hash_value");

            // when
            ResponseEntity<BaseResponse<RefreshedAccessTokenResponse>> response = authenticationController.refreshAccessToken(refreshToken);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verifyNoInteractions(authServiceAdapter);
        }

        @Test
        @DisplayName("Redis에 토큰이 없으면 UNAUTHORIZED를 반환한다")
        void shouldReturnUnauthorizedWhenRedisTokenNotFound() {
            // given
            String refreshToken = "token-not-in-redis";
            Long userId = 1L;

            TokenClaims claims = mock(TokenClaims.class);
            when(claims.getUserId()).thenReturn(userId);
            when(jwtUtil.parseRefreshToken(refreshToken)).thenReturn(claims);

            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("refreshToken:1")).thenReturn(null);

            // when
            ResponseEntity<BaseResponse<RefreshedAccessTokenResponse>> response = authenticationController.refreshAccessToken(refreshToken);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verifyNoInteractions(authServiceAdapter);
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 UNAUTHORIZED를 반환한다")
        void shouldReturnUnauthorizedWhenUserNotFound() throws NoSuchAlgorithmException {
            // given
            String refreshToken = "valid-token";
            Long userId = 1L;

            TokenClaims claims = mock(TokenClaims.class);
            when(claims.getUserId()).thenReturn(userId);
            when(jwtUtil.parseRefreshToken(refreshToken)).thenReturn(claims);

            String expectedRedisValue = "r" + sha256Hex(refreshToken);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("refreshToken:1")).thenReturn(expectedRedisValue);

            when(findUserPort.findAllStatusUserById(userId)).thenReturn(Optional.empty());

            // when
            ResponseEntity<BaseResponse<RefreshedAccessTokenResponse>> response = authenticationController.refreshAccessToken(refreshToken);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verifyNoInteractions(authServiceAdapter);
        }

        @Test
        @DisplayName("비활성화된 사용자이면 UNAUTHORIZED를 반환한다")
        void shouldReturnUnauthorizedWhenUserIsInactive() throws NoSuchAlgorithmException {
            // given
            String refreshToken = "valid-token";
            Long userId = 1L;

            TokenClaims claims = mock(TokenClaims.class);
            when(claims.getUserId()).thenReturn(userId);
            when(jwtUtil.parseRefreshToken(refreshToken)).thenReturn(claims);

            String expectedRedisValue = "r" + sha256Hex(refreshToken);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("refreshToken:1")).thenReturn(expectedRedisValue);

            User inactiveUser = createInactiveUser(userId);
            when(findUserPort.findAllStatusUserById(userId)).thenReturn(Optional.of(inactiveUser));

            // when
            ResponseEntity<BaseResponse<RefreshedAccessTokenResponse>> response = authenticationController.refreshAccessToken(refreshToken);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verifyNoInteractions(authServiceAdapter);
        }

        private User createActiveUser(Long userId) {
            User user = User.from(
                    UserSnapshotState.builder()
                            .id(userId)
                            .userKey(UUID.randomUUID())
                            .status(EntityStatus.ACTIVE)
                            .withdrawalYn(false)
                            .penaltyCount(0)
                            .build()
            );
            return user;
        }

        private User createInactiveUser(Long userId) {
            User user = User.from(
                    UserSnapshotState.builder()
                            .id(userId)
                            .userKey(UUID.randomUUID())
                            .status(EntityStatus.INACTIVE)
                            .withdrawalYn(false)
                            .penaltyCount(0)
                            .build()
            );
            return user;
        }

        private String sha256Hex(String input) throws NoSuchAlgorithmException {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                String hex = Integer.toHexString(b & 0xff);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }
    }
}
