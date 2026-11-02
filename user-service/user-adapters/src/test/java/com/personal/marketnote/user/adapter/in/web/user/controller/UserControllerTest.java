package com.personal.marketnote.user.adapter.in.web.user.controller;

import com.personal.marketnote.user.adapter.in.web.user.request.SignInRequest;
import com.personal.marketnote.user.adapter.in.web.user.request.SignUpRequest;
import com.personal.marketnote.user.port.in.result.SignInResult;
import com.personal.marketnote.user.port.in.result.SignUpResult;
import com.personal.marketnote.user.port.in.usecase.user.*;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import static com.personal.marketnote.common.security.token.utility.TokenConstant.ISS_CLAIM_KEY;
import static com.personal.marketnote.common.security.token.utility.TokenConstant.SUB_CLAIM_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private SignUpUseCase signUpUseCase;

    @Mock
    private SignInUseCase signInUseCase;

    @Mock
    private GetUserUseCase getUserUseCase;

    @Mock
    private UpdateUserUseCase updateUserUseCase;

    @Mock
    private CheckNicknameUseCase checkNicknameUseCase;

    @Mock
    private RegisterReferredUserCodeUseCase registerReferredUserCodeUseCase;

    @Mock
    private SignOutUseCase signOutUseCase;

    @Mock
    private WithdrawUseCase withdrawUseCase;

    @Nested
    @DisplayName("signUpUser - 회원가입 토큰 및 쿠키")
    class SignUpUserTest {

        @Test
        @DisplayName("신규 회원가입 시 HTTP 201을 반환하고 액세스 토큰과 리프레시 토큰 쿠키를 설정한다")
        void shouldReturn201WithTokensForNewUser() {
            // given
            SignUpRequest signUpRequest = mock(SignUpRequest.class);
            when(signUpRequest.getEmail()).thenReturn("test@example.com");
            when(signUpRequest.getPassword()).thenReturn(null);
            when(signUpRequest.getVerificationCode()).thenReturn("123456");
            when(signUpRequest.getNickname()).thenReturn(null);
            when(signUpRequest.getFullName()).thenReturn(null);
            when(signUpRequest.getPhoneNumber()).thenReturn(null);

            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");

            SignUpResult signUpResult = new SignUpResult(1L, "ROLE_BUYER", true, "access-token-value", "refresh-token-value");
            when(signUpUseCase.signUp(any(), eq(AuthVendor.NATIVE), isNull(), eq("192.168.1.1")))
                    .thenReturn(signUpResult);

            // when
            ResponseEntity<?> response = userController.signUpUser(signUpRequest, null, httpServletRequest);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getHeaders().getFirst("Set-Cookie")).contains("refresh_token=refresh-token-value");
            assertThat(response.getHeaders().getFirst("Set-Cookie")).contains("HttpOnly");
            assertThat(response.getHeaders().getFirst("Set-Cookie")).contains("Secure");
            assertThat(response.getHeaders().getFirst("Set-Cookie")).contains("SameSite=Strict");
        }

        @Test
        @DisplayName("기존 회원 로그인 시 HTTP 200을 반환한다")
        void shouldReturn200ForExistingUser() {
            // given
            SignUpRequest signUpRequest = mock(SignUpRequest.class);
            when(signUpRequest.getEmail()).thenReturn("existing@example.com");
            when(signUpRequest.getPassword()).thenReturn(null);
            when(signUpRequest.getVerificationCode()).thenReturn("123456");
            when(signUpRequest.getNickname()).thenReturn(null);
            when(signUpRequest.getFullName()).thenReturn(null);
            when(signUpRequest.getPhoneNumber()).thenReturn(null);

            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");

            SignUpResult signUpResult = new SignUpResult(2L, "ROLE_BUYER", false, "access-token-existing", "refresh-token-existing");
            when(signUpUseCase.signUp(any(), eq(AuthVendor.NATIVE), isNull(), eq("10.0.0.1")))
                    .thenReturn(signUpResult);

            // when
            ResponseEntity<?> response = userController.signUpUser(signUpRequest, null, httpServletRequest);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("OAuth2 인증 정보가 있으면 벤더와 OIDC ID를 추출하여 사용한다")
        void shouldExtractVendorAndOidcIdFromPrincipal() {
            // given
            SignUpRequest signUpRequest = mock(SignUpRequest.class);
            when(signUpRequest.getEmail()).thenReturn("kakao@example.com");
            when(signUpRequest.getPassword()).thenReturn(null);
            when(signUpRequest.getVerificationCode()).thenReturn("123456");
            when(signUpRequest.getNickname()).thenReturn(null);
            when(signUpRequest.getFullName()).thenReturn(null);
            when(signUpRequest.getPhoneNumber()).thenReturn(null);

            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getAttribute(SUB_CLAIM_KEY)).thenReturn("kakao-oidc-123");
            when(principal.getAttribute(ISS_CLAIM_KEY)).thenReturn("https://kauth.kakao.com");

            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");

            SignUpResult signUpResult = new SignUpResult(3L, "ROLE_BUYER", true, "access-token-kakao", "refresh-token-kakao");
            when(signUpUseCase.signUp(any(), eq(AuthVendor.KAKAO), eq("kakao-oidc-123"), eq("10.0.0.1")))
                    .thenReturn(signUpResult);

            // when
            userController.signUpUser(signUpRequest, principal, httpServletRequest);

            // then
            verify(signUpUseCase).signUp(any(), eq(AuthVendor.KAKAO), eq("kakao-oidc-123"), eq("10.0.0.1"));
        }

        @Test
        @DisplayName("회원가입 시 SignUpResult의 토큰을 응답에 사용한다")
        void shouldUseTokensFromSignUpResult() {
            // given
            SignUpRequest signUpRequest = mock(SignUpRequest.class);
            when(signUpRequest.getEmail()).thenReturn("test@example.com");
            when(signUpRequest.getPassword()).thenReturn(null);
            when(signUpRequest.getVerificationCode()).thenReturn("123456");
            when(signUpRequest.getNickname()).thenReturn(null);
            when(signUpRequest.getFullName()).thenReturn(null);
            when(signUpRequest.getPhoneNumber()).thenReturn(null);

            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");

            SignUpResult signUpResult = new SignUpResult(5L, "ROLE_BUYER", true, "my-access-token", "my-refresh-token");
            when(signUpUseCase.signUp(any(), any(), any(), any())).thenReturn(signUpResult);

            // when
            ResponseEntity<?> response = userController.signUpUser(signUpRequest, null, httpServletRequest);

            // then
            assertThat(response.getHeaders().getFirst("Set-Cookie")).contains("refresh_token=my-refresh-token");
        }
    }

    @Nested
    @DisplayName("signInUser - 로그인 토큰 및 쿠키")
    class SignInUserTest {

        @Test
        @DisplayName("로그인 성공 시 HTTP 200과 리프레시 토큰 쿠키를 반환한다")
        void shouldReturn200WithRefreshTokenCookie() {
            // given
            SignInRequest signInRequest = mock(SignInRequest.class);
            when(signInRequest.getEmail()).thenReturn("test@example.com");
            when(signInRequest.getPassword()).thenReturn("password");

            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");

            SignInResult signInResult = new SignInResult(1L, "ROLE_BUYER", true, "access-token-signin", "refresh-token-signin");
            when(signInUseCase.signIn(any(), eq(AuthVendor.NATIVE), isNull(), eq("10.0.0.1")))
                    .thenReturn(signInResult);

            // when
            ResponseEntity<?> response = userController.signInUser(signInRequest, null, httpServletRequest);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getFirst("Set-Cookie")).contains("refresh_token=refresh-token-signin");
            assertThat(response.getHeaders().getFirst("Set-Cookie")).contains("HttpOnly");
            assertThat(response.getHeaders().getFirst("Set-Cookie")).contains("Secure");
        }

        @Test
        @DisplayName("로그인 시 SignInResult의 토큰을 응답에 사용한다")
        void shouldUseTokensFromSignInResult() {
            // given
            SignInRequest signInRequest = mock(SignInRequest.class);
            when(signInRequest.getEmail()).thenReturn("test@example.com");
            when(signInRequest.getPassword()).thenReturn("password");

            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");

            SignInResult signInResult = new SignInResult(10L, "ROLE_BUYER", true, "my-access", "my-refresh");
            when(signInUseCase.signIn(any(), any(), any(), any())).thenReturn(signInResult);

            // when
            ResponseEntity<?> response = userController.signInUser(signInRequest, null, httpServletRequest);

            // then
            assertThat(response.getHeaders().getFirst("Set-Cookie")).contains("refresh_token=my-refresh");
        }
    }

    @Nested
    @DisplayName("extractClientIp - 클라이언트 IP 추출")
    class ExtractClientIpTest {

        @Test
        @DisplayName("X-Forwarded-For 헤더가 있으면 첫 번째 IP를 반환한다")
        void shouldExtractFirstIpFromXForwardedFor() {
            // given
            SignUpRequest signUpRequest = mock(SignUpRequest.class);
            when(signUpRequest.getEmail()).thenReturn("test@example.com");
            when(signUpRequest.getPassword()).thenReturn(null);
            when(signUpRequest.getVerificationCode()).thenReturn("123456");
            when(signUpRequest.getNickname()).thenReturn(null);
            when(signUpRequest.getFullName()).thenReturn(null);
            when(signUpRequest.getPhoneNumber()).thenReturn(null);

            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1, 172.16.0.1");

            SignUpResult signUpResult = new SignUpResult(1L, "ROLE_BUYER", true, "access-token-value", "refresh-token-value");
            when(signUpUseCase.signUp(any(), any(), any(), eq("192.168.1.1"))).thenReturn(signUpResult);

            // when
            userController.signUpUser(signUpRequest, null, httpServletRequest);

            // then
            verify(signUpUseCase).signUp(any(), any(), any(), eq("192.168.1.1"));
        }

        @Test
        @DisplayName("X-Forwarded-For가 없고 X-Real-IP가 있으면 해당 값을 반환한다")
        void shouldFallbackToXRealIp() {
            // given
            SignUpRequest signUpRequest = mock(SignUpRequest.class);
            when(signUpRequest.getEmail()).thenReturn("test@example.com");
            when(signUpRequest.getPassword()).thenReturn(null);
            when(signUpRequest.getVerificationCode()).thenReturn("123456");
            when(signUpRequest.getNickname()).thenReturn(null);
            when(signUpRequest.getFullName()).thenReturn(null);
            when(signUpRequest.getPhoneNumber()).thenReturn(null);

            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
            when(httpServletRequest.getHeader("X-Real-IP")).thenReturn("10.0.0.5");

            SignUpResult signUpResult = new SignUpResult(1L, "ROLE_BUYER", true, "access-token-value", "refresh-token-value");
            when(signUpUseCase.signUp(any(), any(), any(), eq("10.0.0.5"))).thenReturn(signUpResult);

            // when
            userController.signUpUser(signUpRequest, null, httpServletRequest);

            // then
            verify(signUpUseCase).signUp(any(), any(), any(), eq("10.0.0.5"));
        }

        @Test
        @DisplayName("프록시 헤더가 없으면 remoteAddr을 반환한다")
        void shouldFallbackToRemoteAddr() {
            // given
            SignUpRequest signUpRequest = mock(SignUpRequest.class);
            when(signUpRequest.getEmail()).thenReturn("test@example.com");
            when(signUpRequest.getPassword()).thenReturn(null);
            when(signUpRequest.getVerificationCode()).thenReturn("123456");
            when(signUpRequest.getNickname()).thenReturn(null);
            when(signUpRequest.getFullName()).thenReturn(null);
            when(signUpRequest.getPhoneNumber()).thenReturn(null);

            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
            when(httpServletRequest.getHeader("X-Real-IP")).thenReturn(null);
            when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

            SignUpResult signUpResult = new SignUpResult(1L, "ROLE_BUYER", true, "access-token-value", "refresh-token-value");
            when(signUpUseCase.signUp(any(), any(), any(), eq("127.0.0.1"))).thenReturn(signUpResult);

            // when
            userController.signUpUser(signUpRequest, null, httpServletRequest);

            // then
            verify(signUpUseCase).signUp(any(), any(), any(), eq("127.0.0.1"));
        }
    }

    @Nested
    @DisplayName("resolveVendorFromIssuer - 벤더 해석")
    class ResolveVendorFromIssuerTest {

        @Test
        @DisplayName("Google issuer를 GOOGLE 벤더로 해석한다")
        void shouldResolveGoogleVendor() {
            // given
            SignUpRequest signUpRequest = mock(SignUpRequest.class);
            when(signUpRequest.getEmail()).thenReturn("google@example.com");
            when(signUpRequest.getPassword()).thenReturn(null);
            when(signUpRequest.getVerificationCode()).thenReturn("123456");
            when(signUpRequest.getNickname()).thenReturn(null);
            when(signUpRequest.getFullName()).thenReturn(null);
            when(signUpRequest.getPhoneNumber()).thenReturn(null);

            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getAttribute(SUB_CLAIM_KEY)).thenReturn("google-sub-123");
            when(principal.getAttribute(ISS_CLAIM_KEY)).thenReturn("https://accounts.google.com");

            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");

            SignUpResult signUpResult = new SignUpResult(1L, "ROLE_BUYER", true, "access-token-value", "refresh-token-value");
            when(signUpUseCase.signUp(any(), eq(AuthVendor.GOOGLE), eq("google-sub-123"), any()))
                    .thenReturn(signUpResult);

            // when
            userController.signUpUser(signUpRequest, principal, httpServletRequest);

            // then
            verify(signUpUseCase).signUp(any(), eq(AuthVendor.GOOGLE), eq("google-sub-123"), any());
        }

        @Test
        @DisplayName("Apple issuer를 APPLE 벤더로 해석한다")
        void shouldResolveAppleVendor() {
            // given
            SignUpRequest signUpRequest = mock(SignUpRequest.class);
            when(signUpRequest.getEmail()).thenReturn("apple@example.com");
            when(signUpRequest.getPassword()).thenReturn(null);
            when(signUpRequest.getVerificationCode()).thenReturn("123456");
            when(signUpRequest.getNickname()).thenReturn(null);
            when(signUpRequest.getFullName()).thenReturn(null);
            when(signUpRequest.getPhoneNumber()).thenReturn(null);

            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getAttribute(SUB_CLAIM_KEY)).thenReturn("apple-sub-456");
            when(principal.getAttribute(ISS_CLAIM_KEY)).thenReturn("https://appleid.apple.com");

            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");

            SignUpResult signUpResult = new SignUpResult(1L, "ROLE_BUYER", true, "access-token-value", "refresh-token-value");
            when(signUpUseCase.signUp(any(), eq(AuthVendor.APPLE), eq("apple-sub-456"), any()))
                    .thenReturn(signUpResult);

            // when
            userController.signUpUser(signUpRequest, principal, httpServletRequest);

            // then
            verify(signUpUseCase).signUp(any(), eq(AuthVendor.APPLE), eq("apple-sub-456"), any());
        }
    }
}
