package com.personal.marketnote.common.saga.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.saga.SagaSlackProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("SagaFailureSlackAlertAdapter 테스트")
class SagaFailureSlackAlertAdapterTest {

    private MockWebServer mockWebServer;
    private SagaSlackProperties sagaSlackProperties;
    private SagaFailureSlackAlertAdapter adapter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        sagaSlackProperties = new SagaSlackProperties();
        sagaSlackProperties.setWebhookUrl(mockWebServer.url("/webhook").toString());

        adapter = new SagaFailureSlackAlertAdapter(sagaSlackProperties, objectMapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("정상 응답(200) 수신 시 웹훅 엔드포인트로 JSON 페이로드를 POST 전송한다")
    void sendFailureAlert_sendsJsonPayloadToWebhook() throws Exception {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        String sagaId = "saga-123";
        String sagaType = "ORDER_SAGA";
        String failedStepName = "PAYMENT_APPROVE";
        String reason = "결제 승인 실패";
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 20, 12, 34, 56);

        // when
        adapter.sendFailureAlert(sagaId, sagaType, failedStepName, reason, occurredAt);

        // then
        RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/webhook");
        assertThat(request.getHeader("Content-Type")).contains("application/json");

        String body = request.getBody().readUtf8();
        Map<String, String> payload = objectMapper.readValue(body, new TypeReference<Map<String, String>>() {});
        String text = payload.get("text");
        assertThat(text).contains("[SAGA 실패 알림]");
        assertThat(text).contains("SAGA ID: " + sagaId);
        assertThat(text).contains("SAGA 타입: " + sagaType);
        assertThat(text).contains("실패 스텝: " + failedStepName);
        assertThat(text).contains("사유: " + reason);
        assertThat(text).contains("감지 시각: 2026-10-31 12:34:56");
    }

    @Test
    @DisplayName("웹훅 URL이 비어있으면 HTTP 요청을 보내지 않는다")
    void sendFailureAlert_skipsWhenWebhookUrlIsBlank() throws Exception {
        // given
        sagaSlackProperties.setWebhookUrl(null);
        adapter = new SagaFailureSlackAlertAdapter(sagaSlackProperties, objectMapper);

        // when
        adapter.sendFailureAlert(
                "saga-1", "ORDER_SAGA", "STEP", "reason", LocalDateTime.now()
        );

        // then
        RecordedRequest request = mockWebServer.takeRequest(500, TimeUnit.MILLISECONDS);
        assertThat(request).isNull();
    }

    @Test
    @DisplayName("HTTP 전송 실패(커넥션 거부) 시 예외를 삼키고 호출자에게 전파하지 않는다")
    void sendFailureAlert_swallowsExceptionOnConnectionFailure() throws Exception {
        // given - shutdown MockWebServer to force connection refused
        String deadUrl = mockWebServer.url("/webhook").toString();
        mockWebServer.shutdown();

        SagaSlackProperties deadProperties = new SagaSlackProperties();
        deadProperties.setWebhookUrl(deadUrl);
        SagaFailureSlackAlertAdapter deadAdapter = new SagaFailureSlackAlertAdapter(deadProperties, objectMapper);

        // when & then - 예외가 전파되지 않아야 한다
        assertThatCode(() -> deadAdapter.sendFailureAlert(
                "saga-999", "ORDER_SAGA", "PAYMENT",
                "broker timeout", LocalDateTime.of(2026, 4, 20, 10, 0, 0)
        )).doesNotThrowAnyException();
    }
}
