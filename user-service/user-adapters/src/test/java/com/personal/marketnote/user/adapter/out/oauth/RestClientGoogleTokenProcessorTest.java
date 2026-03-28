package com.personal.marketnote.user.adapter.out.oauth;

import com.personal.marketnote.common.domain.exception.token.InvalidAccessTokenException;
import com.personal.marketnote.common.domain.exception.token.InvalidRefreshTokenException;
import com.personal.marketnote.common.domain.exception.token.UnsupportedCodeException;
import com.personal.marketnote.common.utility.http.client.restclient.RestClientErrorHandler;
import com.personal.marketnote.user.exception.GoogleOAuth2ResponseParsingException;
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
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class RestClientGoogleTokenProcessorTest {

    private static final String CLIENT_ID = "test-google-client-id";
    private static final String CLIENT_SECRET = "test-google-client-secret";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_ME_URL = "https://www.googleapis.com/userinfo/v2/me";
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    private MockRestServiceServer mockServer;
    private RestClientGoogleTokenProcessor processor;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .defaultStatusHandler(RestClientErrorHandler::isError, RestClientErrorHandler.noOpErrorHandler());
        mockServer = MockRestServiceServer.bindTo(builder).build();
        processor = new RestClientGoogleTokenProcessor(builder, CLIENT_ID, CLIENT_SECRET);
    }

    @Nested
    @DisplayName("grantToken")
    class GrantToken {

        @Test
        @DisplayName("유효한 인가코드로 토큰 발급에 성공하면 GrantedTokenInfo를 반환한다")
        void shouldReturnGrantedTokenInfoWhenCodeIsValid() {
            String idTokenPayload = java.util.Base64.getEncoder().encodeToString("{\"sub\":\"google-user-123\"}".getBytes());
            String idToken = "header." + idTokenPayload + ".signature";

            mockServer.expect(requestTo(GOOGLE_TOKEN_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().string(containsString("grant_type=authorization_code")))
                    .andExpect(content().string(containsString("code=test-code")))
                    .andExpect(content().string(containsString("redirect_uri=http%3A%2F%2Flocalhost%2Fcallback")))
                    .andExpect(content().string(containsString("client_id=" + CLIENT_ID)))
                    .andExpect(content().string(containsString("client_secret=" + CLIENT_SECRET)))
                    .andRespond(withSuccess("""
                            {"access_token":"google-access-token","refresh_token":"google-refresh-token","id_token":"%s"}
                            """.formatted(idToken), MediaType.APPLICATION_JSON));

            mockServer.expect(requestTo("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {"sub":"google-user-123"}
                            """, MediaType.APPLICATION_JSON));

            GrantedTokenInfo result = processor.grantToken("test-code", "http://localhost/callback");

            assertThat(result.accessToken()).isEqualTo("google-access-token");
            assertThat(result.refreshToken()).isEqualTo("google-refresh-token");
            assertThat(result.id()).isEqualTo("google-user-123");
            assertThat(result.authVendor()).isEqualTo(AuthVendor.GOOGLE);

            mockServer.verify();
        }

        @Test
        @DisplayName("유효하지 않은 인가코드로 토큰 발급 시 UnsupportedCodeException이 발생한다")
        void shouldThrowUnsupportedCodeExceptionWhenCodeIsInvalid() {
            mockServer.expect(requestTo(GOOGLE_TOKEN_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withBadRequest());

            assertThatThrownBy(() -> processor.grantToken("invalid-code", "http://localhost/callback"))
                    .isInstanceOf(UnsupportedCodeException.class);

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("authenticate")
    class Authenticate {

        @Test
        @DisplayName("유효한 액세스 토큰으로 인증에 성공하면 OAuth2AuthenticationInfo를 반환한다")
        void shouldReturnAuthenticationInfoWhenAccessTokenIsValid() {
            mockServer.expect(requestTo(GOOGLE_ME_URL))
                    .andExpect(method(HttpMethod.GET))
                    .andExpect(header("Authorization", "Bearer test-access-token"))
                    .andRespond(withSuccess("""
                            {"id":"google-user-123"}
                            """, MediaType.APPLICATION_JSON));

            OAuth2AuthenticationInfo result = processor.authenticate("test-access-token");

            assertThat(result.id()).isEqualTo("google-user-123");
            assertThat(result.authVendor()).isEqualTo(AuthVendor.GOOGLE);

            mockServer.verify();
        }

        @Test
        @DisplayName("유효하지 않은 액세스 토큰으로 인증 시 InvalidAccessTokenException이 발생한다")
        void shouldThrowInvalidAccessTokenExceptionWhenTokenIsInvalid() {
            mockServer.expect(requestTo(GOOGLE_ME_URL))
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
            mockServer.expect(requestTo(GOOGLE_USER_INFO_URL))
                    .andExpect(method(HttpMethod.GET))
                    .andExpect(header("Authorization", "Bearer test-access-token"))
                    .andRespond(withSuccess("""
                            {"id":"google-user-123","name":"Test User"}
                            """, MediaType.APPLICATION_JSON));

            OAuth2UserInfo result = processor.retrieveUserInfo("test-access-token");

            assertThat(result.id()).isEqualTo("google-user-123");
            assertThat(result.name()).isEqualTo("Test User");

            mockServer.verify();
        }

        @Test
        @DisplayName("유효하지 않은 액세스 토큰으로 사용자 정보 조회 시 InvalidAccessTokenException이 발생한다")
        void shouldThrowInvalidAccessTokenExceptionWhenTokenIsInvalid() {
            mockServer.expect(requestTo(GOOGLE_USER_INFO_URL))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withBadRequest());

            assertThatThrownBy(() -> processor.retrieveUserInfo("invalid-token"))
                    .isInstanceOf(InvalidAccessTokenException.class);

            mockServer.verify();
        }

        @Test
        @DisplayName("응답 JSON에 필수 필드가 없으면 GoogleOAuth2ResponseParsingException이 발생한다")
        void shouldThrowGoogleOAuth2ResponseParsingExceptionWhenRequiredFieldMissing() {
            mockServer.expect(requestTo(GOOGLE_USER_INFO_URL))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {"email":"test@example.com"}
                            """, MediaType.APPLICATION_JSON));

            assertThatThrownBy(() -> processor.retrieveUserInfo("test-access-token"))
                    .isInstanceOf(GoogleOAuth2ResponseParsingException.class);

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("refreshToken")
    class RefreshToken {

        @Test
        @DisplayName("유효한 갱신 토큰으로 토큰 갱신에 성공하면 GrantedTokenInfo를 반환한다")
        void shouldReturnGrantedTokenInfoWhenRefreshTokenIsValid() {
            String idTokenPayload = java.util.Base64.getEncoder().encodeToString("{\"sub\":\"google-user-123\"}".getBytes());
            String idToken = "header." + idTokenPayload + ".signature";

            mockServer.expect(requestTo(GOOGLE_TOKEN_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().string(containsString("grant_type=refresh_token")))
                    .andExpect(content().string(containsString("refresh_token=test-refresh-token")))
                    .andExpect(content().string(containsString("client_id=" + CLIENT_ID)))
                    .andExpect(content().string(containsString("client_secret=" + CLIENT_SECRET)))
                    .andRespond(withSuccess("""
                            {"access_token":"new-access-token","refresh_token":"new-refresh-token","id_token":"%s"}
                            """.formatted(idToken), MediaType.APPLICATION_JSON));

            mockServer.expect(requestTo("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {"sub":"google-user-123"}
                            """, MediaType.APPLICATION_JSON));

            GrantedTokenInfo result = processor.refreshToken("test-refresh-token");

            assertThat(result.accessToken()).isEqualTo("new-access-token");
            assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
            assertThat(result.id()).isEqualTo("google-user-123");
            assertThat(result.authVendor()).isEqualTo(AuthVendor.GOOGLE);

            mockServer.verify();
        }

        @Test
        @DisplayName("유효하지 않은 갱신 토큰으로 토큰 갱신 시 InvalidRefreshTokenException이 발생한다")
        void shouldThrowInvalidRefreshTokenExceptionWhenTokenIsInvalid() {
            mockServer.expect(requestTo(GOOGLE_TOKEN_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withBadRequest());

            assertThatThrownBy(() -> processor.refreshToken("invalid-refresh-token"))
                    .isInstanceOf(InvalidRefreshTokenException.class);

            mockServer.verify();
        }
    }

    @Test
    @DisplayName("getAuthVendor는 GOOGLE을 반환한다")
    void shouldReturnGoogleAuthVendor() {
        assertThat(processor.getAuthVendor()).isEqualTo(AuthVendor.GOOGLE);
    }
}
