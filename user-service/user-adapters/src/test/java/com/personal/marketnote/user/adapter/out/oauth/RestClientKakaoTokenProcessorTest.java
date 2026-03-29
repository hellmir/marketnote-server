package com.personal.marketnote.user.adapter.out.oauth;

import com.personal.marketnote.common.domain.exception.token.InvalidAccessTokenException;
import com.personal.marketnote.common.domain.exception.token.InvalidRefreshTokenException;
import com.personal.marketnote.common.domain.exception.token.UnsupportedCodeException;
import com.personal.marketnote.common.utility.http.client.restclient.RestClientErrorHandler;
import com.personal.marketnote.user.security.token.dto.GrantedTokenInfo;
import com.personal.marketnote.user.security.token.dto.OAuth2AuthenticationInfo;
import com.personal.marketnote.user.security.token.dto.OAuth2UserInfo;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestClientKakaoTokenProcessorTest {

    private static final String CLIENT_ID = "test-kakao-client-id";
    private static final String CLIENT_SECRET = "test-kakao-client-secret";
    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_OIDC_USER_INFO_URL = "https://kapi.kakao.com/v1/oidc/userinfo";
    private static final String KAKAO_ME_URL = "https://kapi.kakao.com/v2/user/me";

    private MockRestServiceServer mockServer;
    private RestClientKakaoTokenProcessor processor;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .defaultStatusHandler(RestClientErrorHandler::isError, RestClientErrorHandler.noOpErrorHandler());
        mockServer = MockRestServiceServer.bindTo(builder).build();
        processor = new RestClientKakaoTokenProcessor(builder, CLIENT_ID, CLIENT_SECRET);
    }

    @Nested
    @DisplayName("grantToken")
    class GrantToken {

        @Test
        @DisplayName("유효한 인가코드로 토큰 발급에 성공하면 GrantedTokenInfo를 반환한다")
        void shouldReturnGrantedTokenInfoWhenCodeIsValid() {
            String idTokenPayload = java.util.Base64.getEncoder().encodeToString("{\"sub\":\"kakao-user-123\"}".getBytes());
            String idToken = "header." + idTokenPayload + ".signature";

            mockServer.expect(requestTo(KAKAO_TOKEN_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().string(containsString("grant_type=authorization_code")))
                    .andExpect(content().string(containsString("code=test-code")))
                    .andExpect(content().string(containsString("redirect_uri=http%3A%2F%2Flocalhost%2Fcallback")))
                    .andExpect(content().string(containsString("client_id=" + CLIENT_ID)))
                    .andRespond(withSuccess("""
                            {"access_token":"kakao-access-token","refresh_token":"kakao-refresh-token","id_token":"%s"}
                            """.formatted(idToken), MediaType.APPLICATION_JSON));

            GrantedTokenInfo result = processor.grantToken("test-code", "http://localhost/callback");

            assertThat(result.accessToken()).isEqualTo("kakao-access-token");
            assertThat(result.refreshToken()).isEqualTo("kakao-refresh-token");
            assertThat(result.id()).isEqualTo("kakao-user-123");
            assertThat(result.authVendor()).isEqualTo(AuthVendor.KAKAO);

            mockServer.verify();
        }

        @Test
        @DisplayName("유효하지 않은 인가코드로 토큰 발급 시 UnsupportedCodeException이 발생한다")
        void shouldThrowUnsupportedCodeExceptionWhenCodeIsInvalid() {
            mockServer.expect(requestTo(KAKAO_TOKEN_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withBadRequest());

            assertThatThrownBy(() -> processor.grantToken("invalid-code", "http://localhost/callback"))
                    .isInstanceOf(UnsupportedCodeException.class);

            mockServer.verify();
        }

        @Test
        @DisplayName("토큰 발급 응답 바디가 null이면 빈 GrantedTokenInfo를 반환한다")
        void shouldReturnEmptyGrantedTokenInfoWhenResponseBodyIsNull() {
            mockServer.expect(requestTo(KAKAO_TOKEN_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess());

            GrantedTokenInfo result = processor.grantToken("test-code", "http://localhost/callback");

            assertThat(result.accessToken()).isNull();

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("grantToken - client_secret이 없는 경우")
    class GrantTokenWithoutClientSecret {

        private MockRestServiceServer noSecretMockServer;
        private RestClientKakaoTokenProcessor noSecretProcessor;

        @BeforeEach
        void setUp() {
            RestClient.Builder builder = RestClient.builder()
                    .defaultStatusHandler(RestClientErrorHandler::isError, RestClientErrorHandler.noOpErrorHandler());
            noSecretMockServer = MockRestServiceServer.bindTo(builder).build();
            noSecretProcessor = new RestClientKakaoTokenProcessor(builder, CLIENT_ID, "empty");
        }

        @Test
        @DisplayName("client_secret이 empty이면 요청에 client_secret을 포함하지 않는다")
        void shouldNotIncludeClientSecretWhenEmpty() {
            String idTokenPayload = java.util.Base64.getEncoder().encodeToString("{\"sub\":\"kakao-user-456\"}".getBytes());
            String idToken = "header." + idTokenPayload + ".signature";

            noSecretMockServer.expect(requestTo(KAKAO_TOKEN_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().string(containsString("grant_type=authorization_code")))
                    .andExpect(content().string(containsString("client_id=" + CLIENT_ID)))
                    .andRespond(withSuccess("""
                            {"access_token":"kakao-access-token","refresh_token":"kakao-refresh-token","id_token":"%s"}
                            """.formatted(idToken), MediaType.APPLICATION_JSON));

            GrantedTokenInfo result = noSecretProcessor.grantToken("test-code", "http://localhost/callback");

            assertThat(result.accessToken()).isEqualTo("kakao-access-token");
            assertThat(result.authVendor()).isEqualTo(AuthVendor.KAKAO);

            noSecretMockServer.verify();
        }
    }

    @Nested
    @DisplayName("authenticate")
    class Authenticate {

        @Test
        @DisplayName("유효한 액세스 토큰으로 인증에 성공하면 OAuth2AuthenticationInfo를 반환한다")
        void shouldReturnAuthenticationInfoWhenAccessTokenIsValid() {
            mockServer.expect(requestTo(KAKAO_OIDC_USER_INFO_URL))
                    .andExpect(method(HttpMethod.GET))
                    .andExpect(header("Authorization", "Bearer test-access-token"))
                    .andRespond(withSuccess("""
                            {"sub":"kakao-user-123"}
                            """, MediaType.APPLICATION_JSON));

            OAuth2AuthenticationInfo result = processor.authenticate("test-access-token");

            assertThat(result.id()).isEqualTo("kakao-user-123");
            assertThat(result.authVendor()).isEqualTo(AuthVendor.KAKAO);

            mockServer.verify();
        }

        @Test
        @DisplayName("유효하지 않은 액세스 토큰으로 인증 시 InvalidAccessTokenException이 발생한다")
        void shouldThrowInvalidAccessTokenExceptionWhenTokenIsInvalid() {
            mockServer.expect(requestTo(KAKAO_OIDC_USER_INFO_URL))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withBadRequest());

            assertThatThrownBy(() -> processor.authenticate("invalid-token"))
                    .isInstanceOf(InvalidAccessTokenException.class);

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("retrieveUserInfo")
    class RetrieveUserInfo {

        @Test
        @DisplayName("유효한 액세스 토큰으로 사용자 정보 조회에 성공하면 OAuth2UserInfo를 반환한다")
        void shouldReturnUserInfoWhenAccessTokenIsValid() {
            mockServer.expect(requestTo(KAKAO_ME_URL))
                    .andExpect(method(HttpMethod.GET))
                    .andExpect(header("Authorization", "Bearer test-access-token"))
                    .andRespond(withSuccess("""
                            {"id":12345,"kakao_account":{"name":"Test User"}}
                            """, MediaType.APPLICATION_JSON));

            OAuth2UserInfo result = processor.retrieveUserInfo("test-access-token");

            assertThat(result.id()).isEqualTo("12345");
            assertThat(result.name()).isEqualTo("Test User");

            mockServer.verify();
        }

        @Test
        @DisplayName("카카오 계정에 이름이 없으면 name이 null인 OAuth2UserInfo를 반환한다")
        void shouldReturnNullNameWhenKakaoAccountNameIsNull() {
            mockServer.expect(requestTo(KAKAO_ME_URL))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {"id":12345,"kakao_account":{"name":null}}
                            """, MediaType.APPLICATION_JSON));

            OAuth2UserInfo result = processor.retrieveUserInfo("test-access-token");

            assertThat(result.id()).isEqualTo("12345");
            assertThat(result.name()).isNull();

            mockServer.verify();
        }

        @Test
        @DisplayName("유효하지 않은 액세스 토큰으로 사용자 정보 조회 시 InvalidAccessTokenException이 발생한다")
        void shouldThrowInvalidAccessTokenExceptionWhenTokenIsInvalid() {
            mockServer.expect(requestTo(KAKAO_ME_URL))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withBadRequest());

            assertThatThrownBy(() -> processor.retrieveUserInfo("invalid-token"))
                    .isInstanceOf(InvalidAccessTokenException.class);

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("refreshToken")
    class RefreshToken {

        @Test
        @DisplayName("유효한 갱신 토큰으로 토큰 갱신에 성공하면 GrantedTokenInfo를 반환한다")
        void shouldReturnGrantedTokenInfoWhenRefreshTokenIsValid() {
            String idTokenPayload = java.util.Base64.getEncoder().encodeToString("{\"sub\":\"kakao-user-123\"}".getBytes());
            String idToken = "header." + idTokenPayload + ".signature";

            mockServer.expect(requestTo(KAKAO_TOKEN_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().string(containsString("grant_type=refresh_token")))
                    .andExpect(content().string(containsString("client_id=" + CLIENT_ID)))
                    .andExpect(content().string(containsString("refresh_token=test-refresh-token")))
                    .andRespond(withSuccess("""
                            {"access_token":"new-access-token","refresh_token":"new-refresh-token","id_token":"%s"}
                            """.formatted(idToken), MediaType.APPLICATION_JSON));

            GrantedTokenInfo result = processor.refreshToken("test-refresh-token");

            assertThat(result.accessToken()).isEqualTo("new-access-token");
            assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
            assertThat(result.id()).isEqualTo("kakao-user-123");
            assertThat(result.authVendor()).isEqualTo(AuthVendor.KAKAO);

            mockServer.verify();
        }

        @Test
        @DisplayName("유효하지 않은 갱신 토큰으로 토큰 갱신 시 InvalidRefreshTokenException이 발생한다")
        void shouldThrowInvalidRefreshTokenExceptionWhenTokenIsInvalid() {
            mockServer.expect(requestTo(KAKAO_TOKEN_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withBadRequest());

            assertThatThrownBy(() -> processor.refreshToken("invalid-refresh-token"))
                    .isInstanceOf(InvalidRefreshTokenException.class);

            mockServer.verify();
        }
    }

    @Test
    @DisplayName("getAuthVendor는 KAKAO를 반환한다")
    void shouldReturnKakaoAuthVendor() {
        assertThat(processor.getAuthVendor()).isEqualTo(AuthVendor.KAKAO);
    }
}
