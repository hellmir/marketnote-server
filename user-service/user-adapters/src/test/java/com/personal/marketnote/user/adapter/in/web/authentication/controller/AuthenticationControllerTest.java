package com.personal.marketnote.user.adapter.in.web.authentication.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.domain.exception.token.InvalidRefreshTokenException;
import com.personal.marketnote.user.adapter.in.web.authentication.response.RefreshedAccessTokenResponse;
import com.personal.marketnote.user.adapter.in.web.authentication.response.WebBasedTokenRefreshResponse;
import com.personal.marketnote.user.adapter.out.vendor.authentication.WebBasedAuthenticationServiceAdapter;
import com.personal.marketnote.user.port.in.usecase.authentication.RefreshAccessTokenUseCase;
import com.personal.marketnote.user.port.in.usecase.authentication.SendEmailVerificationUseCase;
import com.personal.marketnote.user.port.in.usecase.authentication.VerifyCodeUseCase;
import com.personal.marketnote.user.utility.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private RefreshAccessTokenUseCase refreshAccessTokenUseCase;

    @Mock
    private JwtUtil jwtUtil;

    @Nested
    @DisplayName("refreshAccessToken - 토큰 갱신")
    class RefreshAccessTokenTest {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 액세스 토큰을 재발급한다")
        void shouldRefreshAccessTokenWithValidRefreshToken() {
            // given
            String refreshToken = "valid-refresh-token";
            doNothing().when(refreshAccessTokenUseCase).validateRefreshToken(refreshToken);

            RefreshedAccessTokenResponse accessTokenResponse = new RefreshedAccessTokenResponse("new-access-token");
            WebBasedTokenRefreshResponse refreshResponse = new WebBasedTokenRefreshResponse(new HttpHeaders(), accessTokenResponse);
            when(authServiceAdapter.issueNewAccessToken(refreshToken)).thenReturn(refreshResponse);

            // when
            ResponseEntity<BaseResponse<RefreshedAccessTokenResponse>> response = authenticationController.refreshAccessToken(refreshToken);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(refreshAccessTokenUseCase).validateRefreshToken(refreshToken);
            verify(authServiceAdapter).issueNewAccessToken(refreshToken);
        }

        @Test
        @DisplayName("리프레시 토큰 검증 실패 시 InvalidRefreshTokenException이 전파된다")
        void shouldPropagateExceptionWhenValidationFails() {
            // given
            String refreshToken = "invalid-token";
            doThrow(new InvalidRefreshTokenException("유효하지 않은 리프레시 토큰"))
                    .when(refreshAccessTokenUseCase).validateRefreshToken(refreshToken);

            // expect
            assertThatThrownBy(() -> authenticationController.refreshAccessToken(refreshToken))
                    .isInstanceOf(InvalidRefreshTokenException.class);

            verifyNoInteractions(authServiceAdapter);
        }
    }
}
