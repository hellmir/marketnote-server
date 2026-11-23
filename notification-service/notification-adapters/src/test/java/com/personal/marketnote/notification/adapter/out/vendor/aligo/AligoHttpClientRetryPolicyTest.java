package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.configuration.ObjectMapperConfig;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoApiRequestFailedException;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationType;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * AligoHttpClient retry 정책 검증 — record 발행이 시도별로 1회씩 폭발하지 않고
 * 호출 단위로 REQUEST + RESPONSE 2건만 발행되는지(attemptCount/attempts 메타 포함) 확인.
 */
class AligoHttpClientRetryPolicyTest {

    private static final String OPERATION = "sendAlimTalk";
    private static final String PATH = "/akv10/alimtalk/send/";
    private static final String SENSITIVE_PATH = "/auth/login/";

    private AligoHttpClient aligoHttpClient;
    private MockRestServiceServer mockServer;
    private List<RecordedCall> recordedCalls;

    @BeforeEach
    void setUp() {
        AligoProperties properties = new AligoProperties();
        properties.setBaseUrl("http://localhost");
        properties.setApiKey("test-api-key");
        properties.setUserId("test-user-id");
        properties.setTimeout(5000);
        properties.setTestMode(true);
        AligoProperties.Retry retry = new AligoProperties.Retry();
        retry.setMaxAttempts(3);
        retry.setInitialBackoffMs(1);
        properties.setRetry(retry);
        properties.getApi().setSensitivePaths(Set.of(SENSITIVE_PATH));

        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl("http://localhost").build();

        ObjectMapper objectMapper = new ObjectMapperConfig().objectMapper();
        ExternalCallLogger externalCallLogger = new ExternalCallLogger(new SimpleMeterRegistry());
        recordedCalls = new ArrayList<>();
        aligoHttpClient = AligoHttpClientTestSupport.newClient(
                restClient, objectMapper, properties, externalCallLogger,
                (
                        targetType, targetId, vendorName, communicationType, sender,
                        exception, payload, payloadJson, attemptCount
                ) ->
                        recordedCalls.add(new RecordedCall(
                                communicationType, payload, payloadJson, attemptCount, exception
                        ))
        );
    }

    @Test
    @DisplayName("즉시 성공 시 REQUEST + RESPONSE record 2건 발행되며 attemptCount=1, attempts.size=1")
    void shouldRecordOnceWithAttemptCountOneOnImmediateSuccess() {
        String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
        mockServer.expect(requestTo("http://localhost" + PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", "01012345678");

        aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class);

        assertThat(recordedCalls).hasSize(2);
        RecordedCall request = findByType(NotificationVendorCommunicationType.REQUEST);
        RecordedCall response = findByType(NotificationVendorCommunicationType.RESPONSE);

        assertThat(request.attemptCount()).isEqualTo(1);
        assertThat(response.attemptCount()).isEqualTo(1);

        JsonNode requestAttempts = request.payloadJson().get("attempts");
        JsonNode responseAttempts = response.payloadJson().get("attempts");
        assertThat(request.payloadJson().get("attemptCount").asInt()).isEqualTo(1);
        assertThat(response.payloadJson().get("attemptCount").asInt()).isEqualTo(1);
        assertThat(requestAttempts.isArray()).isTrue();
        assertThat(requestAttempts).hasSize(1);
        assertThat(responseAttempts.isArray()).isTrue();
        assertThat(responseAttempts).hasSize(1);
        assertThat(requestAttempts.get(0).get("status").asText()).isEqualTo("SUCCESS");
        assertThat(responseAttempts.get(0).get("status").asText()).isEqualTo("SUCCESS");
        assertThat(requestAttempts.get(0).get("number").asInt()).isEqualTo(1);
    }

    @Test
    @DisplayName("1차 HTTP 실패 후 2차 성공 시 REQUEST + RESPONSE record 2건 발행되며 attemptCount=2, attempts=[HTTP_FAILURE, SUCCESS]")
    void shouldRecordOnceWithAttemptCountTwoWhenSecondAttemptSucceeds() {
        String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
        mockServer.expect(requestTo("http://localhost" + PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                        .body("{\"err\":\"upstream\"}")
                        .contentType(MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo("http://localhost" + PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", "01012345678");

        aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class);

        assertThat(recordedCalls).hasSize(2);
        RecordedCall request = findByType(NotificationVendorCommunicationType.REQUEST);
        RecordedCall response = findByType(NotificationVendorCommunicationType.RESPONSE);

        assertThat(request.attemptCount()).isEqualTo(2);
        assertThat(response.attemptCount()).isEqualTo(2);
        assertThat(request.payloadJson().get("attemptCount").asInt()).isEqualTo(2);
        assertThat(response.payloadJson().get("attemptCount").asInt()).isEqualTo(2);

        JsonNode attempts = response.payloadJson().get("attempts");
        assertThat(attempts).hasSize(2);
        assertThat(attempts.get(0).get("number").asInt()).isEqualTo(1);
        assertThat(attempts.get(0).get("status").asText()).isEqualTo("HTTP_FAILURE");
        assertThat(attempts.get(0).get("httpStatus").asInt()).isEqualTo(502);
        assertThat(attempts.get(1).get("number").asInt()).isEqualTo(2);
        assertThat(attempts.get(1).get("status").asText()).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("3회 모두 HTTP 실패 시 REQUEST + 마지막 실패 RESPONSE 2건만 발행되고 attemptCount=3, attempts.size=3 모두 HTTP_FAILURE")
    void shouldRecordOnceWithAllHttpFailuresWhenAllAttemptsFail() {
        for (int i = 0; i < 3; i++) {
            mockServer.expect(requestTo("http://localhost" + PATH))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("{\"err\":\"down\"}")
                            .contentType(MediaType.APPLICATION_JSON));
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", "01012345678");

        assertThatThrownBy(() -> aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class))
                .isInstanceOf(AligoApiRequestFailedException.class);

        // failure handler 경유로 REQUEST + RESPONSE 2건만 발행되어야 함 (시도별 중복 발행 금지)
        assertThat(recordedCalls).hasSize(2);
        RecordedCall request = findByType(NotificationVendorCommunicationType.REQUEST);
        RecordedCall response = findByType(NotificationVendorCommunicationType.RESPONSE);

        assertThat(request.attemptCount()).isEqualTo(3);
        assertThat(response.attemptCount()).isEqualTo(3);
        assertThat(request.payloadJson().get("attemptCount").asInt()).isEqualTo(3);
        assertThat(response.payloadJson().get("attemptCount").asInt()).isEqualTo(3);

        JsonNode attempts = response.payloadJson().get("attempts");
        assertThat(attempts).hasSize(3);
        for (int i = 0; i < 3; i++) {
            assertThat(attempts.get(i).get("number").asInt()).isEqualTo(i + 1);
            assertThat(attempts.get(i).get("status").asText()).isEqualTo("HTTP_FAILURE");
            assertThat(attempts.get(i).get("httpStatus").asInt()).isEqualTo(500);
        }
    }

    @Test
    @DisplayName("모든 시도가 parse 실패 시 REQUEST + 마지막 실패 RESPONSE 2건만 발행되고 attempts 모두 PARSE_FAILURE")
    void shouldRecordOnceWithAllParseFailuresWhenAllAttemptsHaveBrokenJson() {
        for (int i = 0; i < 3; i++) {
            mockServer.expect(requestTo("http://localhost" + PATH))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("invalid json", MediaType.APPLICATION_JSON));
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", "01012345678");

        assertThatThrownBy(() -> aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class))
                .isInstanceOf(AligoApiRequestFailedException.class);

        assertThat(recordedCalls).hasSize(2);
        RecordedCall request = findByType(NotificationVendorCommunicationType.REQUEST);
        RecordedCall response = findByType(NotificationVendorCommunicationType.RESPONSE);

        assertThat(request.attemptCount()).isEqualTo(3);
        assertThat(response.attemptCount()).isEqualTo(3);

        JsonNode attempts = response.payloadJson().get("attempts");
        assertThat(attempts).hasSize(3);
        for (int i = 0; i < 3; i++) {
            assertThat(attempts.get(i).get("status").asText()).isEqualTo("PARSE_FAILURE");
            assertThat(attempts.get(i).get("number").asInt()).isEqualTo(i + 1);
        }
    }

    private RecordedCall findByType(NotificationVendorCommunicationType type) {
        return recordedCalls.stream()
                .filter(call -> call.communicationType() == type)
                .findFirst()
                .orElseThrow(() -> new AssertionError(type + " 통신 이력이 기록되지 않았습니다."));
    }

    private record RecordedCall(
            NotificationVendorCommunicationType communicationType,
            String payload,
            JsonNode payloadJson,
            int attemptCount,
            String exception
    ) {
    }
}
