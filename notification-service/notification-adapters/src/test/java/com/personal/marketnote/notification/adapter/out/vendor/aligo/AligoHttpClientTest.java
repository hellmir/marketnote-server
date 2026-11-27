package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoApiRequestFailedException;
import com.personal.marketnote.notification.domain.aligo.AligoAuthenticationException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AligoHttpClientTest {

    private static final String OPERATION = "sendTest";
    private static final String PATH = "/send/";
    private static final String SENSITIVE_PATH = "/auth/login/";
    private static final String RECIPIENT_PHONE = "01012345678";

    private AligoHttpClient aligoHttpClient;
    private MockRestServiceServer mockServer;
    private ExternalCallLogger externalCallLogger;
    private Logger aligoLogger;
    private ListAppender<ILoggingEvent> appender;
    private boolean previousAdditive;
    private Level previousLevel;

    @BeforeEach
    void setUp() {
        AligoProperties properties = new AligoProperties();
        properties.setBaseUrl("http://localhost");
        properties.setApiKey("test-api-key");
        properties.setUserId("test-user-id");
        properties.setTimeout(5000);
        properties.setTestMode(true);
        AligoProperties.Retry retry = new AligoProperties.Retry();
        retry.setMaxAttempts(1);
        retry.setInitialBackoffMs(10);
        properties.setRetry(retry);
        properties.getApi().setSensitivePaths(Set.of(SENSITIVE_PATH));

        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl("http://localhost").build();

        ObjectMapper objectMapper = new ObjectMapper();
        externalCallLogger = spy(new ExternalCallLogger(new SimpleMeterRegistry()));
        aligoHttpClient = AligoHttpClientTestSupport.newClient(
            restClient, objectMapper, properties, externalCallLogger
        );

        aligoLogger = (Logger) LoggerFactory.getLogger(ExternalSystem.ALIGO.loggerName());
        previousAdditive = aligoLogger.isAdditive();
        previousLevel = aligoLogger.getLevel();
        aligoLogger.setAdditive(false);
        aligoLogger.setLevel(Level.DEBUG);
        appender = new ListAppender<>();
        appender.start();
        aligoLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        aligoLogger.detachAppender(appender);
        appender.stop();
        aligoLogger.setAdditive(previousAdditive);
        aligoLogger.setLevel(previousLevel);
    }

    @Test
    @DisplayName("AligoHttpClient가 apikey/userid 공통 파라미터를 자동 주입한다")
    void shouldInjectCommonParameters() {
        String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
        mockServer.expect(requestTo("http://localhost/send/"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", RECIPIENT_PHONE);

        AligoApiResponse response = aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class);

        assertThat(response.resultCode()).isZero();
        assertThat(response.message()).isEqualTo("성공");
        mockServer.verify();
    }

    @Test
    @DisplayName("AligoHttpClient가 form-urlencoded POST 본문을 올바르게 생성한다")
    void shouldSendFormEncodedPostBody() {
        String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
        mockServer.expect(requestTo("http://localhost/send/"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", RECIPIENT_PHONE);
        params.add("msg", "테스트 메시지");

        AligoApiResponse response = aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class);

        assertThat(response.resultCode()).isZero();
        mockServer.verify();
    }

    @Test
    @DisplayName("알리고 API가 에러 코드를 반환하면 도메인 예외를 던진다")
    void shouldThrowDomainExceptionOnApiError() {
        String responseBody = "{\"result_code\":-101,\"message\":\"인증 오류\"}";
        mockServer.expect(requestTo("http://localhost/send/"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        assertThatThrownBy(() -> aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class))
            .isInstanceOf(AligoAuthenticationException.class);
        mockServer.verify();
    }

    @Test
    @DisplayName("응답 파싱 실패 시 AligoApiRequestFailedException으로 래핑한다")
    void shouldWrapParsingErrorAsAligoApiRequestFailedException() {
        String responseBody = "invalid json";
        mockServer.expect(requestTo("http://localhost/send/"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        assertThatThrownBy(() -> aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class))
            .isInstanceOf(AligoApiRequestFailedException.class);
        mockServer.verify();
    }

    @Test
    @DisplayName("정상 호출 시 external.aligo 로거에 operation/path가 포함된 DEBUG 로그를 emit한다")
    void shouldEmitDebugLogWithOperationAndPathOnSuccess() {
        String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
        mockServer.expect(requestTo("http://localhost/send/"))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", RECIPIENT_PHONE);

        aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class);

        boolean hasDebugWithOperationAndPath = appender.list.stream()
            .anyMatch(event -> event.getLevel() == Level.DEBUG
                && event.getFormattedMessage().contains(OPERATION)
                && event.getFormattedMessage().contains(PATH));
        assertThat(hasDebugWithOperationAndPath).isTrue();
    }

    @Test
    @DisplayName("정상 호출 시 external.aligo 로거 메시지에 평문 수신자 전화번호가 포함되지 않는다")
    void shouldNotLeakRecipientPhoneInAligoLog() {
        String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
        mockServer.expect(requestTo("http://localhost/send/"))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", RECIPIENT_PHONE);

        aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class);

        boolean leaksPhone = appender.list.stream()
            .anyMatch(event -> event.getFormattedMessage().contains(RECIPIENT_PHONE));
        assertThat(leaksPhone).isFalse();
    }

    @Test
    @DisplayName("post 호출 시 sensitive path는 ExternalCallLogger.recordSensitive로 위임된다")
    void shouldDelegateToRecordSensitiveOnPostWithSensitivePath() {
        String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
        mockServer.expect(requestTo("http://localhost" + SENSITIVE_PATH))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", RECIPIENT_PHONE);

        aligoHttpClient.post(OPERATION, SENSITIVE_PATH, params, AligoApiResponse.class);

        verify(externalCallLogger).recordSensitive(
            eq(ExternalSystem.ALIGO), eq(OPERATION), any(), any()
        );
        verify(externalCallLogger, never()).record(
            eq(ExternalSystem.ALIGO), eq(OPERATION), any(), any()
        );
    }

    @Test
    @DisplayName("post 호출 시 non-sensitive path는 ExternalCallLogger.record로 위임된다")
    void shouldDelegateToRecordOnPostWithNonSensitivePath() {
        String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
        mockServer.expect(requestTo("http://localhost" + PATH))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", RECIPIENT_PHONE);

        aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class);

        verify(externalCallLogger).record(
            eq(ExternalSystem.ALIGO), eq(OPERATION), any(), any()
        );
        verify(externalCallLogger, never()).recordSensitive(
            eq(ExternalSystem.ALIGO), eq(OPERATION), any(), any()
        );
    }

    @Test
    @DisplayName("postMultipart 호출 시 sensitive path는 ExternalCallLogger.recordSensitive로 위임된다")
    void shouldDelegateToRecordSensitiveOnPostMultipartWithSensitivePath() {
        String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
        mockServer.expect(requestTo("http://localhost" + SENSITIVE_PATH))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("receiver", RECIPIENT_PHONE);

        aligoHttpClient.postMultipart(OPERATION, SENSITIVE_PATH, params, AligoApiResponse.class);

        verify(externalCallLogger).recordSensitive(
            eq(ExternalSystem.ALIGO), eq(OPERATION), any(), any()
        );
        verify(externalCallLogger, never()).record(
            eq(ExternalSystem.ALIGO), eq(OPERATION), any(), any()
        );
    }

    @Test
    @DisplayName("postMultipart 호출 시 non-sensitive path는 ExternalCallLogger.record로 위임된다")
    void shouldDelegateToRecordOnPostMultipartWithNonSensitivePath() {
        String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
        mockServer.expect(requestTo("http://localhost" + PATH))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("receiver", RECIPIENT_PHONE);

        aligoHttpClient.postMultipart(OPERATION, PATH, params, AligoApiResponse.class);

        verify(externalCallLogger).record(
            eq(ExternalSystem.ALIGO), eq(OPERATION), any(), any()
        );
        verify(externalCallLogger, never()).recordSensitive(
            eq(ExternalSystem.ALIGO), eq(OPERATION), any(), any()
        );
    }

    @Test
    @DisplayName("API 에러 시 external.aligo 로거에 ERROR 로그 + resultCode가 포함된다")
    void shouldEmitErrorLogWithResultCodeOnApiError() {
        String responseBody = "{\"result_code\":-101,\"message\":\"인증 오류\"}";
        mockServer.expect(requestTo("http://localhost/send/"))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        assertThatThrownBy(() -> aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class))
            .isInstanceOf(AligoAuthenticationException.class);

        boolean hasErrorWithResultCode = appender.list.stream()
            .anyMatch(event -> event.getLevel() == Level.ERROR
                && event.getFormattedMessage().contains(OPERATION)
                && event.getFormattedMessage().contains("-101"));
        assertThat(hasErrorWithResultCode).isTrue();
    }
}
