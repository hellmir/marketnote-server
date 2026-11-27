package com.personal.marketnote.user.adapter.in.web.user.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.user.adapter.in.web.user.request.SignInRequest;
import com.personal.marketnote.user.adapter.in.web.user.request.SignUpRequest;
import com.personal.marketnote.user.adapter.in.web.user.request.UpdateUserInfoRequest;
import com.personal.marketnote.user.adapter.in.web.user.response.*;
import com.personal.marketnote.user.domain.user.exception.InvalidReferenceCodeException;
import com.personal.marketnote.user.domain.user.exception.ReferenceCodeNoValueException;
import com.personal.marketnote.user.port.in.result.*;
import com.personal.marketnote.user.port.in.usecase.user.*;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.personal.marketnote.common.security.token.utility.TokenConstant.ISS_CLAIM_KEY;
import static com.personal.marketnote.common.security.token.utility.TokenConstant.SUB_CLAIM_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private SkipReferredUserCodeInputUseCase skipReferredUserCodeInputUseCase;

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
    @DisplayName("isReferredUserCodeRegistered - 추천 회원 초대 코드 입력 상태 조회")
    class IsReferredUserCodeRegisteredTest {

        @Test
        @DisplayName("추천 코드가 등록되어 있으면 (isRegistered=true, isSkipped=false)로 응답한다")
        void shouldReturnRegisteredTrueWhenRegistered() {
            // given
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("42");
            when(getUserUseCase.getReferredUserCodeInputStatus(42L))
                .thenReturn(new ReferredUserCodeInputStatusResult(true, false));

            // when
            ResponseEntity<BaseResponse<IsReferredUserCodeRegisteredResponse>> response =
                userController.isReferredUserCodeRegistered(principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().isRegistered()).isTrue();
            assertThat(response.getBody().getContent().isSkipped()).isFalse();
            verify(getUserUseCase).getReferredUserCodeInputStatus(42L);
        }

        @Test
        @DisplayName("추천 코드 입력을 건너뛰었으면 (isRegistered=false, isSkipped=true)로 응답한다")
        void shouldReturnSkippedTrueWhenSkipped() {
            // given
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("11");
            when(getUserUseCase.getReferredUserCodeInputStatus(11L))
                .thenReturn(new ReferredUserCodeInputStatusResult(false, true));

            // when
            ResponseEntity<BaseResponse<IsReferredUserCodeRegisteredResponse>> response =
                userController.isReferredUserCodeRegistered(principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().isRegistered()).isFalse();
            assertThat(response.getBody().getContent().isSkipped()).isTrue();
            verify(getUserUseCase).getReferredUserCodeInputStatus(11L);
        }

        @Test
        @DisplayName("추천 코드를 등록하지도 건너뛰지도 않았으면 (isRegistered=false, isSkipped=false)로 응답한다")
        void shouldReturnBothFalseWhenPending() {
            // given
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("7");
            when(getUserUseCase.getReferredUserCodeInputStatus(7L))
                .thenReturn(new ReferredUserCodeInputStatusResult(false, false));

            // when
            ResponseEntity<BaseResponse<IsReferredUserCodeRegisteredResponse>> response =
                userController.isReferredUserCodeRegistered(principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().isRegistered()).isFalse();
            assertThat(response.getBody().getContent().isSkipped()).isFalse();
            verify(getUserUseCase).getReferredUserCodeInputStatus(7L);
        }
    }

    @Nested
    @DisplayName("skipReferredUserCodeInput - 추천 회원 초대 코드 입력 건너뛰기")
    class SkipReferredUserCodeInputTest {

        @Test
        @DisplayName("인증된 회원의 건너뛰기 요청에 대해 200 OK와 성공 메시지를 반환한다")
        void shouldReturn200WhenSkipSucceeds() {
            // given
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("42");

            // when
            ResponseEntity<BaseResponse<Void>> response =
                userController.skipReferredUserCodeInput(principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("초대 코드 입력 건너뛰기 성공");
            verify(skipReferredUserCodeInputUseCase).skipReferredUserCodeInput(42L);
        }
    }

    @Nested
    @DisplayName("existsReferredUserCode - 초대 코드 존재 여부 조회")
    class ExistsReferredUserCodeTest {

        @Test
        @DisplayName("존재하는 초대 코드면 HTTP 200과 exists=true로 응답한다")
        void shouldReturnTrueWhenCodeExists() {
            // given
            String code = "ABC123";
            when(getUserUseCase.existsUser(code)).thenReturn(true);

            // when
            ResponseEntity<BaseResponse<IsReferredUserCodeExistsResponse>> response =
                userController.existsReferredUserCode(code);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().exists()).isTrue();
            verify(getUserUseCase).existsUser(code);
        }

        @Test
        @DisplayName("존재하지 않는 초대 코드면 HTTP 200과 exists=false로 응답한다")
        void shouldReturnFalseWhenCodeDoesNotExist() {
            // given
            String code = "XYZ789";
            when(getUserUseCase.existsUser(code)).thenReturn(false);

            // when
            ResponseEntity<BaseResponse<IsReferredUserCodeExistsResponse>> response =
                userController.existsReferredUserCode(code);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().exists()).isFalse();
            verify(getUserUseCase).existsUser(code);
        }

        @Test
        @DisplayName("초대 코드 형식이 잘못되면 InvalidReferenceCodeException을 던진다")
        void shouldThrowWhenCodeFormatInvalid() {
            // given - 패턴 ^[A-Z0-9]{6}$ 위반: 소문자 포함
            String invalidCode = "abc123";

            // when & then
            assertThatThrownBy(() -> userController.existsReferredUserCode(invalidCode))
                .isInstanceOf(InvalidReferenceCodeException.class);
            verify(getUserUseCase, never()).existsUser(anyString());
        }

        @Test
        @DisplayName("초대 코드 길이가 6자리가 아니면 InvalidReferenceCodeException을 던진다")
        void shouldThrowWhenCodeLengthInvalid() {
            // given
            String invalidCode = "AB12";

            // when & then
            assertThatThrownBy(() -> userController.existsReferredUserCode(invalidCode))
                .isInstanceOf(InvalidReferenceCodeException.class);
            verify(getUserUseCase, never()).existsUser(anyString());
        }

        @Test
        @DisplayName("초대 코드가 빈 문자열이면 ReferenceCodeNoValueException을 던진다")
        void shouldThrowWhenCodeBlank() {
            // given
            String blankCode = "";

            // when & then
            assertThatThrownBy(() -> userController.existsReferredUserCode(blankCode))
                .isInstanceOf(ReferenceCodeNoValueException.class);
            verify(getUserUseCase, never()).existsUser(anyString());
        }

        @Test
        @DisplayName("초대 코드가 공백 문자열이면 ReferenceCodeNoValueException을 던진다")
        void shouldThrowWhenCodeWhitespace() {
            // given
            String whitespaceCode = "   ";

            // when & then
            assertThatThrownBy(() -> userController.existsReferredUserCode(whitespaceCode))
                .isInstanceOf(ReferenceCodeNoValueException.class);
            verify(getUserUseCase, never()).existsUser(anyString());
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

        @Test
        @DisplayName("알 수 없는 issuer면 NATIVE 벤더로 해석한다")
        void shouldResolveNativeForUnknownIssuer() {
            // given
            SignUpRequest signUpRequest = mock(SignUpRequest.class);
            when(signUpRequest.getEmail()).thenReturn("unknown@example.com");
            when(signUpRequest.getPassword()).thenReturn(null);
            when(signUpRequest.getVerificationCode()).thenReturn("123456");
            when(signUpRequest.getNickname()).thenReturn(null);
            when(signUpRequest.getFullName()).thenReturn(null);
            when(signUpRequest.getPhoneNumber()).thenReturn(null);

            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getAttribute(SUB_CLAIM_KEY)).thenReturn("unknown-sub-789");
            when(principal.getAttribute(ISS_CLAIM_KEY)).thenReturn("https://unknown.example.com");

            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");

            SignUpResult signUpResult = new SignUpResult(1L, "ROLE_BUYER", true, "access-token-value", "refresh-token-value");
            when(signUpUseCase.signUp(any(), eq(AuthVendor.NATIVE), eq("unknown-sub-789"), any()))
                .thenReturn(signUpResult);

            // when
            userController.signUpUser(signUpRequest, principal, httpServletRequest);

            // then
            verify(signUpUseCase).signUp(any(), eq(AuthVendor.NATIVE), eq("unknown-sub-789"), any());
        }
    }

    @Nested
    @DisplayName("registerReferredUserCode - 추천인 초대 코드 등록")
    class RegisterReferredUserCodeTest {

        @Test
        @DisplayName("추천 코드 등록에 성공하면 HTTP 200과 성공 메시지를 반환한다")
        void shouldReturn200WhenRegisterSucceeds() {
            // given
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("100");

            // when
            ResponseEntity<BaseResponse<Void>> response =
                userController.registerReferredUserCode("ABC123", principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("초대 코드 등록 성공");
            verify(registerReferredUserCodeUseCase).registerReferredUserCode(100L, "ABC123");
        }
    }

    @Nested
    @DisplayName("checkNickname - 닉네임 중복 여부 조회")
    class CheckNicknameTest {

        @Test
        @DisplayName("닉네임 조회 결과를 그대로 응답에 담아 HTTP 200으로 반환한다")
        void shouldReturnNicknameCheckResult() {
            // given
            String nickname = "테스트닉";
            when(checkNicknameUseCase.checkNickname(nickname))
                .thenReturn(CheckNicknameResult.of(true, false));

            // when
            ResponseEntity<BaseResponse<CheckNicknameResponse>> response =
                userController.checkNickname(nickname);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().isDuplicated()).isTrue();
            assertThat(response.getBody().getContent().containsProfanity()).isFalse();
            verify(checkNicknameUseCase).checkNickname(nickname);
        }
    }

    @Nested
    @DisplayName("getMyKey - 자신의 회원 키 조회")
    class GetMyKeyTest {

        @Test
        @DisplayName("회원 키 조회 결과를 응답에 담아 HTTP 200으로 반환한다")
        void shouldReturnUserKey() {
            // given
            UUID userKey = UUID.randomUUID();
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("55");
            when(getUserUseCase.getUserKey(55L)).thenReturn(userKey);

            // when
            ResponseEntity<BaseResponse<GetUserKeyResponse>> response =
                userController.getMyKey(principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().userKey()).isEqualTo(userKey);
            verify(getUserUseCase).getUserKey(55L);
        }
    }

    @Nested
    @DisplayName("getMyInfo - 자신의 정보 조회")
    class GetMyInfoTest {

        @Test
        @DisplayName("회원 정보 조회 결과를 응답에 담아 HTTP 200으로 반환한다")
        void shouldReturnMyInfo() {
            // given
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("77");

            GetUserInfoResult userInfoResult = mock(GetUserInfoResult.class);
            when(getUserUseCase.getUserInfo(77L)).thenReturn(userInfoResult);

            // when
            ResponseEntity<BaseResponse<GetUserInfoResponse>> response =
                userController.getMyInfo(principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().userInfo()).isSameAs(userInfoResult);
            verify(getUserUseCase).getUserInfo(77L);
        }
    }

    @Nested
    @DisplayName("updateMyInfo - 자신의 정보 수정")
    class UpdateMyInfoTest {

        @Test
        @DisplayName("관리자 여부는 false로 고정하고 회원 정보 수정 후 HTTP 200을 반환한다")
        void shouldUpdateMyInfoWithIsAdminFalse() {
            // given
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("88");

            UpdateUserInfoRequest request = new UpdateUserInfoRequest();

            // when
            ResponseEntity<BaseResponse<Void>> response =
                userController.updateMyInfo(request, principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("자신의 정보 수정 성공");
            verify(updateUserUseCase).updateUserInfo(eq(false), eq(88L), any());
        }
    }

    @Nested
    @DisplayName("signOutUser - 회원 로그아웃")
    class SignOutUserTest {

        @Test
        @DisplayName("로그아웃 성공 시 SignOutUseCase가 반환한 헤더와 HTTP 200을 함께 반환한다")
        void shouldReturn200WithSignOutHeaders() {
            // given
            HttpHeaders signOutHeaders = new HttpHeaders();
            signOutHeaders.add("Set-Cookie", "refresh_token=; Max-Age=0");
            when(signOutUseCase.signOut("refresh-token-value")).thenReturn(signOutHeaders);

            // when
            ResponseEntity<BaseResponse<Void>> response =
                userController.signOutUser("refresh-token-value");

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getFirst("Set-Cookie")).contains("Max-Age=0");
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("회원 로그아웃 성공");
            verify(signOutUseCase).signOut("refresh-token-value");
        }
    }

    @Nested
    @DisplayName("withdrawUser - 회원 탈퇴")
    class WithdrawUserTest {

        @Test
        @DisplayName("Google 액세스 토큰이 있으면 vendorCredentials에 GOOGLE 키로 담아 호출한다")
        void shouldIncludeGoogleAccessTokenInVendorCredentials() {
            // given
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("200");

            Map<AuthVendor, Boolean> disconnectResults = new HashMap<>();
            disconnectResults.put(AuthVendor.GOOGLE, true);
            disconnectResults.put(AuthVendor.KAKAO, false);
            disconnectResults.put(AuthVendor.APPLE, true);
            WithdrawResult result = new WithdrawResult(disconnectResults);

            ArgumentCaptor<Map<AuthVendor, String>> captor = ArgumentCaptor.forClass(Map.class);
            when(withdrawUseCase.withdrawUser(eq(200L), captor.capture())).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<WithdrawResponse>> response =
                userController.withdrawUser(principal, "google-access-token-value");

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().isGoogleDisconnected()).isTrue();
            assertThat(response.getBody().getContent().isKakaoDisconnected()).isFalse();
            assertThat(response.getBody().getContent().isAppleDisconnected()).isTrue();

            Map<AuthVendor, String> captured = captor.getValue();
            assertThat(captured).containsEntry(AuthVendor.GOOGLE, "google-access-token-value");
            assertThat(captured).hasSize(1);
        }

        @Test
        @DisplayName("Google 액세스 토큰이 없으면 vendorCredentials를 빈 맵으로 호출한다")
        void shouldUseEmptyVendorCredentialsWhenNoGoogleToken() {
            // given
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("201");

            Map<AuthVendor, Boolean> disconnectResults = new HashMap<>();
            WithdrawResult result = new WithdrawResult(disconnectResults);

            ArgumentCaptor<Map<AuthVendor, String>> captor = ArgumentCaptor.forClass(Map.class);
            when(withdrawUseCase.withdrawUser(eq(201L), captor.capture())).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<WithdrawResponse>> response =
                userController.withdrawUser(principal, null);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(captor.getValue()).isEmpty();
            // disconnectResults 비어있을 때 getOrDefault → 모두 true 기본값
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().isKakaoDisconnected()).isTrue();
            assertThat(response.getBody().getContent().isGoogleDisconnected()).isTrue();
            assertThat(response.getBody().getContent().isAppleDisconnected()).isTrue();
        }
    }
}
