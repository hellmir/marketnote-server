package com.personal.marketnote.user.adapter.out.oauth;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.utility.http.client.restclient.RestClientErrorHandler;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import com.personal.marketnote.user.service.exception.UnlinkOauth2AccountFailedException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class Oauth2AccountClientTest {

    private static final String KAKAO_ADMIN_KEY = "test-admin-key";

    private MockRestServiceServer mockServer;
    private Oauth2AccountClient oauth2AccountClient;
    private Logger kakaoLogger;
    private Logger googleLogger;
    private ListAppender<ILoggingEvent> kakaoAppender;
    private ListAppender<ILoggingEvent> googleAppender;
    private boolean kakaoPreviousAdditive;
    private Level kakaoPreviousLevel;
    private boolean googlePreviousAdditive;
    private Level googlePreviousLevel;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .defaultStatusHandler(RestClientErrorHandler::isError, RestClientErrorHandler.noOpErrorHandler());
        mockServer = MockRestServiceServer.bindTo(builder).build();
        ExternalCallLogger externalCallLogger = new ExternalCallLogger(new SimpleMeterRegistry());
        oauth2AccountClient = new Oauth2AccountClient(builder, KAKAO_ADMIN_KEY, externalCallLogger);

        kakaoLogger = (Logger) LoggerFactory.getLogger(ExternalSystem.KAKAO_OAUTH.loggerName());
        kakaoPreviousAdditive = kakaoLogger.isAdditive();
        kakaoPreviousLevel = kakaoLogger.getLevel();
        kakaoLogger.setAdditive(false);
        kakaoLogger.setLevel(Level.DEBUG);
        kakaoAppender = new ListAppender<>();
        kakaoAppender.start();
        kakaoLogger.addAppender(kakaoAppender);

        googleLogger = (Logger) LoggerFactory.getLogger(ExternalSystem.GOOGLE_OAUTH.loggerName());
        googlePreviousAdditive = googleLogger.isAdditive();
        googlePreviousLevel = googleLogger.getLevel();
        googleLogger.setAdditive(false);
        googleLogger.setLevel(Level.DEBUG);
        googleAppender = new ListAppender<>();
        googleAppender.start();
        googleLogger.addAppender(googleAppender);
    }

    @AfterEach
    void tearDown() {
        kakaoLogger.detachAppender(kakaoAppender);
        kakaoAppender.stop();
        kakaoLogger.setAdditive(kakaoPreviousAdditive);
        kakaoLogger.setLevel(kakaoPreviousLevel);

        googleLogger.detachAppender(googleAppender);
        googleAppender.stop();
        googleLogger.setAdditive(googlePreviousAdditive);
        googleLogger.setLevel(googlePreviousLevel);
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

    @Nested
    @DisplayName("ExternalCallLogger 통합 - Kakao")
    class KakaoExternalCallLoggerIntegration {

        @Test
        @DisplayName("카카오 unlink 성공 시 KAKAO_OAUTH 로거에 operation과 provider 태그가 기록된다")
        void shouldLogOperationAndProviderTagOnKakaoUnlink() throws UnlinkOauth2AccountFailedException {
            mockServer.expect(requestTo("https://kapi.kakao.com/v1/user/unlink"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess());

            oauth2AccountClient.unlinkAccount(AuthVendor.KAKAO, "12345");

            boolean hasOperation = kakaoAppender.list.stream()
                    .anyMatch(event -> event.getFormattedMessage().contains("unlinkAccount"));
            boolean hasProvider = kakaoAppender.list.stream()
                    .anyMatch(event -> event.getFormattedMessage().contains("kakao"));
            assertThat(hasOperation).isTrue();
            assertThat(hasProvider).isTrue();
        }

        @Test
        @DisplayName("카카오 unlink 실패 시 ERROR 로그에 cause 예외 클래스명이 기록되고 동일 예외가 rethrow된다")
        void shouldLogErrorWithCauseAndRethrowOnKakaoUnlinkFailure() {
            mockServer.expect(requestTo("https://kapi.kakao.com/v1/user/unlink"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withBadRequest());

            assertThatThrownBy(() -> oauth2AccountClient.unlinkAccount(AuthVendor.KAKAO, "12345"))
                    .isInstanceOf(UnlinkOauth2AccountFailedException.class);

            boolean hasErrorLog = kakaoAppender.list.stream()
                    .anyMatch(event -> event.getLevel() == Level.ERROR
                            && event.getFormattedMessage().contains("UnlinkOauth2AccountFailed"));
            assertThat(hasErrorLog).isTrue();
        }
    }

    @Nested
    @DisplayName("ExternalCallLogger 통합 - Google")
    class GoogleExternalCallLoggerIntegration {

        @Test
        @DisplayName("구글 revoke 성공 시 GOOGLE_OAUTH 로거에 operation과 provider 태그가 기록된다")
        void shouldLogOperationAndProviderTagOnGoogleRevoke() throws UnlinkOauth2AccountFailedException {
            mockServer.expect(requestTo("https://oauth2.googleapis.com/revoke"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess());

            oauth2AccountClient.unlinkAccount(AuthVendor.GOOGLE, "google-access-token");

            boolean hasOperation = googleAppender.list.stream()
                    .anyMatch(event -> event.getFormattedMessage().contains("unlinkAccount"));
            boolean hasProvider = googleAppender.list.stream()
                    .anyMatch(event -> event.getFormattedMessage().contains("google"));
            assertThat(hasOperation).isTrue();
            assertThat(hasProvider).isTrue();
        }

        @Test
        @DisplayName("구글 revoke 로그에 평문 액세스 토큰이 노출되지 않는다 (보안 회귀 검증)")
        void shouldNotLeakPlaintextAccessTokenInGoogleRevokeLog() throws UnlinkOauth2AccountFailedException {
            String secretToken = "super-secret-google-revoke-token-do-not-leak";

            mockServer.expect(requestTo("https://oauth2.googleapis.com/revoke"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess());

            oauth2AccountClient.unlinkAccount(AuthVendor.GOOGLE, secretToken);

            boolean leaked = googleAppender.list.stream()
                    .anyMatch(event -> event.getFormattedMessage().contains(secretToken));
            assertThat(leaked).isFalse();
        }
    }
}
