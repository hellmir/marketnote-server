package com.personal.marketnote.user.adapter.out.oauth;

import com.personal.marketnote.common.utility.http.client.restclient.RestClientErrorHandler;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import com.personal.marketnote.user.service.exception.UnlinkOauth2AccountFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class Oauth2AccountClientTest {

    private static final String KAKAO_ADMIN_KEY = "test-admin-key";

    private MockRestServiceServer mockServer;
    private Oauth2AccountClient oauth2AccountClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .defaultStatusHandler(RestClientErrorHandler::isError, RestClientErrorHandler.noOpErrorHandler());
        mockServer = MockRestServiceServer.bindTo(builder).build();
        oauth2AccountClient = new Oauth2AccountClient(builder, KAKAO_ADMIN_KEY);
    }

    @Nested
    @DisplayName("unlinkKakaoAccount")
    class UnlinkKakaoAccount {

        @Test
        @DisplayName("카카오 계정 연결 해제 성공 시 예외가 발생하지 않는다")
        void shouldUnlinkKakaoAccountSuccessfully() {
            mockServer.expect(requestTo("https://kapi.kakao.com/v1/user/unlink"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(header("Authorization", "KakaoAK " + KAKAO_ADMIN_KEY))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().string(containsString("target_id_type=user_id")))
                    .andExpect(content().string(containsString("target_id=12345")))
                    .andRespond(withSuccess());

            assertThatCode(() -> oauth2AccountClient.unlinkAccount(AuthVendor.KAKAO, "12345"))
                    .doesNotThrowAnyException();

            mockServer.verify();
        }

        @Test
        @DisplayName("카카오 계정 연결 해제 실패 시 UnlinkOauth2AccountFailedException이 발생한다")
        void shouldThrowExceptionWhenKakaoUnlinkFails() {
            mockServer.expect(requestTo("https://kapi.kakao.com/v1/user/unlink"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(header("Authorization", "KakaoAK " + KAKAO_ADMIN_KEY))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andRespond(withBadRequest());

            assertThatThrownBy(() -> oauth2AccountClient.unlinkAccount(AuthVendor.KAKAO, "12345"))
                    .isInstanceOf(UnlinkOauth2AccountFailedException.class);

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("unlinkGoogleAccount")
    class UnlinkGoogleAccount {

        @Test
        @DisplayName("구글 계정 연결 해제 성공 시 예외가 발생하지 않는다")
        void shouldUnlinkGoogleAccountSuccessfully() {
            mockServer.expect(requestTo("https://oauth2.googleapis.com/revoke"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().string(containsString("token=google-access-token")))
                    .andRespond(withSuccess());

            assertThatCode(() -> oauth2AccountClient.unlinkAccount(AuthVendor.GOOGLE, "google-access-token"))
                    .doesNotThrowAnyException();

            mockServer.verify();
        }

        @Test
        @DisplayName("구글 계정 연결 해제 실패 시 UnlinkOauth2AccountFailedException이 발생한다")
        void shouldThrowExceptionWhenGoogleRevokeFails() {
            mockServer.expect(requestTo("https://oauth2.googleapis.com/revoke"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andRespond(withServerError());

            assertThatThrownBy(() -> oauth2AccountClient.unlinkAccount(AuthVendor.GOOGLE, "google-access-token"))
                    .isInstanceOf(UnlinkOauth2AccountFailedException.class);

            mockServer.verify();
        }
    }
}
