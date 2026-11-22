package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoApiRequestFailedException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("AligoHttpClient HTTP 응답 실패 sensitive path 마스킹 테스트")
class AligoHttpClientHttpFailureMaskingTest {

    private static final String BASE_URL = "http://localhost:9999";
    private static final String OPERATION = "testOperation";
    private static final String SENSITIVE_BODY_WITH_PHONE =
            "{\"resultData\":{\"receiver\":\"01012345678\",\"msg\":\"hello\"}}";
    private static final String NON_SENSITIVE_ERROR_BODY =
            "{\"resultCode\":\"-1\",\"message\":\"server error\"}";
    private static final String FORBIDDEN_TOKEN = "01012345678";

    private MockRestServiceServer mockServer;
    private AligoHttpClient aligoHttpClient;
    private AligoProperties properties;
    private ListAppender<ILoggingEvent> clientAppender;
    private ListAppender<ILoggingEvent> externalAppender;
    private Logger clientLogger;
    private Logger externalLogger;
    private boolean clientPreviousAdditive;
    private Level clientPreviousLevel;
    private boolean externalPreviousAdditive;
    private Level externalPreviousLevel;

    @BeforeEach
    void setUp() {
        properties = new AligoProperties();
        properties.setBaseUrl(BASE_URL);
        properties.setApiKey("test-api-key");
        properties.setUserId("test-user-id");
        properties.setTestMode(true);
        AligoProperties.Retry retry = new AligoProperties.Retry();
        retry.setMaxAttempts(1);
        retry.setInitialBackoffMs(10);
        properties.setRetry(retry);
        AligoTestPathConfigurer.configure(properties);

        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();

        ObjectMapper objectMapper = new ObjectMapper();
        ExternalCallLogger externalCallLogger = new ExternalCallLogger(new SimpleMeterRegistry());
        aligoHttpClient = AligoHttpClientTestSupport.newClient(
                builder.build(), objectMapper, properties, externalCallLogger
        );

        clientLogger = (Logger) LoggerFactory.getLogger(AligoHttpClient.class);
        clientPreviousAdditive = clientLogger.isAdditive();
        clientPreviousLevel = clientLogger.getLevel();
        clientLogger.setAdditive(false);
        clientAppender = new ListAppender<>();
        clientAppender.start();
        clientLogger.addAppender(clientAppender);

        externalLogger = (Logger) LoggerFactory.getLogger(ExternalSystem.ALIGO.loggerName());
        externalPreviousAdditive = externalLogger.isAdditive();
        externalPreviousLevel = externalLogger.getLevel();
        externalLogger.setAdditive(false);
        externalAppender = new ListAppender<>();
        externalAppender.start();
        externalLogger.addAppender(externalAppender);
    }

    @AfterEach
    void tearDown() {
        clientLogger.detachAppender(clientAppender);
        clientAppender.stop();
        clientLogger.setAdditive(clientPreviousAdditive);
        clientLogger.setLevel(clientPreviousLevel);

        externalLogger.detachAppender(externalAppender);
        externalAppender.stop();
        externalLogger.setAdditive(externalPreviousAdditive);
        externalLogger.setLevel(externalPreviousLevel);
    }

    @Test
    @DisplayName("sensitive path /akv10/alimtalk/send/ 4xx 응답 시 raw body가 예외 메시지/로그에 노출되지 않고 cause를 제거한다")
    void shouldMaskRawBodyWhenSensitivePathReturns4xx() {
        // given
        String path = properties.getApi().getAlimtalkSendPath();
        mockServer.expect(requestTo(BASE_URL + path))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(SENSITIVE_BODY_WITH_PHONE));

        // when & then
        assertThatThrownBy(() -> aligoHttpClient.post(
                OPERATION, path, emptyParams(), AligoApiResponse.class
        ))
                .isInstanceOf(AligoApiRequestFailedException.class)
                .hasMessageContaining("HTTP 응답 실패")
                .hasMessageContaining(path)
                .hasMessageContaining("status=401")
                .hasMessageNotContaining(FORBIDDEN_TOKEN)
                .hasNoCause();

        // client logger (sensitive 분기): raw body 없어야 하고 throwable 자체가 첨부되지 않아야 함
        assertThat(clientAppender.list)
                .filteredOn(event -> event.getLevel() == Level.ERROR)
                .allSatisfy(event -> {
                    assertThat(event.getFormattedMessage()).doesNotContain(FORBIDDEN_TOKEN);
                    assertThat(event.getThrowableProxy()).isNull();
                });

        // external logger (ExternalCallLogger.recordFailure): AligoApiRequestFailedException 자체는 첨부되지만,
        // 메시지/스택/cause 어디에도 raw body가 포함되어선 안 된다.
        assertThat(externalAppender.list)
                .filteredOn(event -> event.getLevel() == Level.ERROR)
                .allSatisfy(event -> {
                    assertThat(event.getFormattedMessage()).doesNotContain(FORBIDDEN_TOKEN);
                    assertNoRawBodyInThrowableChain(event, FORBIDDEN_TOKEN);
                });

        mockServer.verify();
    }

    @Test
    @DisplayName("sensitive path /akv10/history/detail/ 5xx 응답 시에도 raw body가 노출되지 않고 cause를 제거한다")
    void shouldMaskRawBodyWhenSensitivePathReturns5xx() {
        // given
        String path = properties.getApi().getHistoryDetailPath();
        mockServer.expect(requestTo(BASE_URL + path))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(SENSITIVE_BODY_WITH_PHONE));

        // when & then
        assertThatThrownBy(() -> aligoHttpClient.post(
                OPERATION, path, emptyParams(), AligoApiResponse.class
        ))
                .isInstanceOf(AligoApiRequestFailedException.class)
                .hasMessageContaining("HTTP 응답 실패")
                .hasMessageContaining(path)
                .hasMessageContaining("status=500")
                .hasMessageNotContaining(FORBIDDEN_TOKEN)
                .hasNoCause();

        assertThat(clientAppender.list)
                .filteredOn(event -> event.getLevel() == Level.ERROR)
                .allSatisfy(event -> {
                    assertThat(event.getFormattedMessage()).doesNotContain(FORBIDDEN_TOKEN);
                    assertThat(event.getThrowableProxy()).isNull();
                });

        assertThat(externalAppender.list)
                .filteredOn(event -> event.getLevel() == Level.ERROR)
                .allSatisfy(event -> {
                    assertThat(event.getFormattedMessage()).doesNotContain(FORBIDDEN_TOKEN);
                    assertNoRawBodyInThrowableChain(event, FORBIDDEN_TOKEN);
                });

        mockServer.verify();
    }

    @Test
    @DisplayName("sensitive path /brandtalk/template/send/ 4xx 응답 시 client 로그에 path/status/exception 클래스명만 기록된다")
    void shouldLogPathAndStatusWhenSensitivePathHttpFailure() {
        // given
        String path = properties.getApi().getBrandTalkSendPath();
        mockServer.expect(requestTo(BASE_URL + path))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(SENSITIVE_BODY_WITH_PHONE));

        // when
        assertThatThrownBy(() -> aligoHttpClient.post(
                OPERATION, path, emptyParams(), AligoApiResponse.class
        ))
                .isInstanceOf(AligoApiRequestFailedException.class);

        // then
        ILoggingEvent errorEvent = findClientErrorEvent();
        String formattedMessage = errorEvent.getFormattedMessage();
        assertThat(formattedMessage).contains(path);
        assertThat(formattedMessage).contains("status=400");
        assertThat(formattedMessage).containsAnyOf(
                "HttpClientErrorException",
                "RestClientResponseException",
                "BadRequest"
        );

        mockServer.verify();
    }

    @Test
    @DisplayName("non-sensitive path /akv10/heartinfo/ 5xx 응답 시 cause는 보존되고 stack trace가 client 로그에 포함된다")
    void shouldPreserveCauseWhenNonSensitivePathHttpFailure() {
        // given
        String path = properties.getApi().getHeartInfoPath();
        mockServer.expect(requestTo(BASE_URL + path))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(NON_SENSITIVE_ERROR_BODY));

        // when & then
        assertThatThrownBy(() -> aligoHttpClient.post(
                OPERATION, path, emptyParams(), AligoApiResponse.class
        ))
                .isInstanceOf(AligoApiRequestFailedException.class)
                .hasMessageContaining("HTTP 응답 실패")
                .hasMessageContaining(path)
                .hasMessageContaining("status=500")
                .hasCauseInstanceOf(org.springframework.web.client.RestClientResponseException.class);

        ILoggingEvent errorEvent = findClientErrorEvent();
        assertThat(errorEvent.getThrowableProxy()).isNotNull();
        assertThat(errorEvent.getThrowableProxy().getClassName())
                .containsAnyOf(
                        "RestClientResponseException",
                        "HttpServerErrorException",
                        "InternalServerError"
                );

        mockServer.verify();
    }

    @Test
    @DisplayName("non-sensitive path /akv10/cancel/ 5xx 응답 시 cause는 보존되고 stack trace가 client 로그에 포함된다")
    void shouldPreserveCauseWhenCancelPathHttpFailure() {
        // given
        String path = properties.getApi().getCancelPath();
        mockServer.expect(requestTo(BASE_URL + path))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(NON_SENSITIVE_ERROR_BODY));

        // when & then
        assertThatThrownBy(() -> aligoHttpClient.post(
                OPERATION, path, emptyParams(), AligoApiResponse.class
        ))
                .isInstanceOf(AligoApiRequestFailedException.class)
                .hasMessageContaining("HTTP 응답 실패")
                .hasMessageContaining(path)
                .hasMessageContaining("status=500")
                .hasCauseInstanceOf(org.springframework.web.client.RestClientResponseException.class);

        ILoggingEvent errorEvent = findClientErrorEvent();
        assertThat(errorEvent.getThrowableProxy()).isNotNull();
        assertThat(errorEvent.getThrowableProxy().getClassName())
                .containsAnyOf(
                        "RestClientResponseException",
                        "HttpServerErrorException",
                        "InternalServerError"
                );

        mockServer.verify();
    }

    @Test
    @DisplayName("non-sensitive path /akv10/template/add/ 5xx 응답 시 cause는 보존되고 stack trace가 client 로그에 포함된다")
    void shouldPreserveCauseWhenTemplateAddPathHttpFailure() {
        // given
        String path = properties.getApi().getTemplateAddPath();
        mockServer.expect(requestTo(BASE_URL + path))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(NON_SENSITIVE_ERROR_BODY));

        // when & then
        assertThatThrownBy(() -> aligoHttpClient.post(
                OPERATION, path, emptyParams(), AligoApiResponse.class
        ))
                .isInstanceOf(AligoApiRequestFailedException.class)
                .hasMessageContaining("HTTP 응답 실패")
                .hasMessageContaining(path)
                .hasMessageContaining("status=500")
                .hasCauseInstanceOf(org.springframework.web.client.RestClientResponseException.class);

        ILoggingEvent errorEvent = findClientErrorEvent();
        assertThat(errorEvent.getThrowableProxy()).isNotNull();
        assertThat(errorEvent.getThrowableProxy().getClassName())
                .containsAnyOf(
                        "RestClientResponseException",
                        "HttpServerErrorException",
                        "InternalServerError"
                );

        mockServer.verify();
    }

    @Test
    @DisplayName("sensitive path 응답 본문 파싱 실패 시 raw body가 예외 메시지/로그에 노출되지 않고 cause를 제거한다")
    void shouldMaskRawBodyWhenSensitivePathParseFailure() {
        // given — sensitive path (alimtalk/send) 200 응답이지만 raw body에 phone 토큰 포함된 깨진 JSON
        String path = properties.getApi().getAlimtalkSendPath();
        String corruptBodyWithPhone = "{\"resultData\":{\"receiver\":\"01012345678\",broken_json}";
        mockServer.expect(requestTo(BASE_URL + path))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(corruptBodyWithPhone, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> aligoHttpClient.post(
                OPERATION, path, emptyParams(), AligoApiResponse.class
        ))
                .isInstanceOf(AligoApiRequestFailedException.class)
                .hasMessageContaining("응답 처리 실패")
                .hasMessageContaining(path)
                .hasMessageNotContaining(FORBIDDEN_TOKEN)
                .hasNoCause();

        // client logger: raw body 미포함 + throwable 미첨부
        assertThat(clientAppender.list)
                .filteredOn(event -> event.getLevel() == Level.ERROR)
                .allSatisfy(event -> {
                    assertThat(event.getFormattedMessage()).doesNotContain(FORBIDDEN_TOKEN);
                    assertThat(event.getThrowableProxy()).isNull();
                });

        // external logger: throwable chain 어디에도 raw body 미포함
        assertThat(externalAppender.list)
                .filteredOn(event -> event.getLevel() == Level.ERROR)
                .allSatisfy(event -> {
                    assertThat(event.getFormattedMessage()).doesNotContain(FORBIDDEN_TOKEN);
                    assertNoRawBodyInThrowableChain(event, FORBIDDEN_TOKEN);
                });

        mockServer.verify();
    }

    @Test
    @DisplayName("non-sensitive path 응답 본문 파싱 실패 시 cause는 보존되고 stack trace가 client 로그에 포함된다")
    void shouldPreserveCauseWhenNonSensitivePathParseFailure() {
        // given — non-sensitive path (heartinfo) 200 + 깨진 JSON
        String path = properties.getApi().getHeartInfoPath();
        mockServer.expect(requestTo(BASE_URL + path))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{ broken json", MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> aligoHttpClient.post(
                OPERATION, path, emptyParams(), AligoApiResponse.class
        ))
                .isInstanceOf(AligoApiRequestFailedException.class)
                .hasMessageContaining("응답 처리 실패")
                .hasMessageContaining(path)
                .hasCauseInstanceOf(com.fasterxml.jackson.core.JsonProcessingException.class);

        ILoggingEvent errorEvent = findClientErrorEvent();
        assertThat(errorEvent.getThrowableProxy()).isNotNull();

        mockServer.verify();
    }

    private MultiValueMap<String, String> emptyParams() {
        return new LinkedMultiValueMap<>();
    }

    private ILoggingEvent findClientErrorEvent() {
        List<ILoggingEvent> events = clientAppender.list;
        return events.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .findFirst()
                .orElseThrow(() -> new AssertionError("ERROR 레벨 client 로그 이벤트를 찾을 수 없습니다."));
    }

    private void assertNoRawBodyInThrowableChain(ILoggingEvent event, String forbidden) {
        IThrowableProxy proxy = event.getThrowableProxy();
        while (proxy != null) {
            String message = proxy.getMessage();
            if (message != null) {
                assertThat(message).doesNotContain(forbidden);
            }
            StackTraceElementProxy[] stack = proxy.getStackTraceElementProxyArray();
            if (stack != null) {
                for (StackTraceElementProxy element : stack) {
                    assertThat(element.getSTEAsString()).doesNotContain(forbidden);
                }
            }
            proxy = proxy.getCause();
        }
    }
}
