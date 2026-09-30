package com.personal.marketnote.user.adapter.out.token;

import com.personal.marketnote.common.domain.exception.token.InvalidRefreshTokenException;
import com.personal.marketnote.user.utility.jwt.JwtUtil;
import com.personal.marketnote.user.utility.jwt.claims.TokenClaims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenParserAdapterTest {

    @InjectMocks
    private RefreshTokenParserAdapter adapter;

    @Mock
    private JwtUtil jwtUtil;

    @Nested
    @DisplayName("extractUserId - 리프레시 토큰에서 userId 추출")
    class ExtractUserIdTest {

        @Test
        @DisplayName("유효한 리프레시 토큰에서 userId를 추출한다")
        void shouldExtractUserIdFromValidToken() {
            // given
            String refreshToken = "valid-refresh-token";
            TokenClaims claims = mock(TokenClaims.class);
            when(claims.getUserId()).thenReturn(42L);
            when(jwtUtil.parseRefreshToken(refreshToken)).thenReturn(claims);

            // when
            Long userId = adapter.extractUserId(refreshToken);

            // then
            assertThat(userId).isEqualTo(42L);
        }

        @Test
        @DisplayName("파싱 실패 시 InvalidRefreshTokenException을 던진다")
        void shouldThrowInvalidRefreshTokenExceptionOnParseFailure() {
            // given
            String invalidToken = "invalid-token";
            when(jwtUtil.parseRefreshToken(invalidToken)).thenThrow(new JwtException("Malformed JWT"));

            // when & then
            assertThatThrownBy(() -> adapter.extractUserId(invalidToken))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }

        @Test
        @DisplayName("예기치 않은 예외 발생 시에도 InvalidRefreshTokenException으로 래핑한다")
        void shouldWrapUnexpectedExceptionAsInvalidRefreshTokenException() {
            // given
            String token = "some-token";
            when(jwtUtil.parseRefreshToken(token)).thenThrow(new IllegalArgumentException("Unexpected error"));

            // when & then
            assertThatThrownBy(() -> adapter.extractUserId(token))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }
    }
}
