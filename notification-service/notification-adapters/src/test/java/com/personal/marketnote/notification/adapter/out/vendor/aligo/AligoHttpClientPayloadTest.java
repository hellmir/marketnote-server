package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationType;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * AligoHttpClient의 페이로드 조립(maskRequestBody / sanitizeMultiValueEntry / buildResponsePayloadJson) 검증.
 *
 * <p>private 메서드를 직접 호출하지 않고 public 진입점({@code post})에서 dispatcher로 흘러나오는
 * payloadJson을 캡처하여 의미를 검증한다.</p>
 */
class AligoHttpClientPayloadTest {

    private static final String OPERATION = "sendTest";
    private static final String PATH = "/send/";
    private static final String SENSITIVE_PATH = "/auth/login/";

    private AligoHttpClient aligoHttpClient;
    private MockRestServiceServer mockServer;
    private List<RecordedCall> recordedCalls;

    @BeforeEach
    void setUp() {
        AligoProperties properties = new AligoProperties();
        properties.setBaseUrl("http://localhost");
        properties.setApiKey("verysecret-apikey-1234");
        properties.setUserId("user-id-67890");
        properties.setTimeout(5000);
        properties.setTestMode(false);
        AligoProperties.Retry retry = new AligoProperties.Retry();
        retry.setMaxAttempts(1);
        retry.setInitialBackoffMs(10);
        properties.setRetry(retry);
        properties.getApi().setSensitivePaths(Set.of(SENSITIVE_PATH));

        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl("http://localhost").build();

        ObjectMapper objectMapper = new ObjectMapper();
        ExternalCallLogger externalCallLogger = new ExternalCallLogger(new SimpleMeterRegistry());
        recordedCalls = new ArrayList<>();
        aligoHttpClient = AligoHttpClientTestSupport.newClient(
            restClient, objectMapper, properties, externalCallLogger,
            (
                targetType, targetId, vendorName, communicationType, sender,
                exception, payload, payloadJson, attemptCount
            ) ->
                recordedCalls.add(new RecordedCall(communicationType, payloadJson))
        );
    }

    @Test
    @DisplayName("정상 응답 시 RESPONSE 페이로드에 body 키가 응답 본문 문자열로 포함된다")
    void shouldIncludeBodyInResponsePayloadOnSuccess() {
        String responseBody = "{\"result_code\":0,\"message\":\"OK\"}";
        mockServer.expect(requestTo("http://localhost" + PATH))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", "01012345678");

        aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class);

        JsonNode responseJson = findResponsePayloadJson();
        assertThat(responseJson.has("body")).isTrue();
        assertThat(responseJson.get("body").asText()).isEqualTo(responseBody);
        assertThat(responseJson.get("resultCode").asInt()).isZero();
        assertThat(responseJson.get("message").asText()).isEqualTo("OK");
    }

    @Test
    @DisplayName("sensitive path는 RESPONSE 페이로드에 body가 제외되고 masked=true가 포함된다")
    void shouldEmitMaskedFlagWithoutBodyOnSensitivePath() {
        String responseBody = "{\"result_code\":0,\"message\":\"OK\"}";
        mockServer.expect(requestTo("http://localhost" + SENSITIVE_PATH))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", "01012345678");

        aligoHttpClient.post(OPERATION, SENSITIVE_PATH, params, AligoApiResponse.class);

        JsonNode responseJson = findResponsePayloadJson();
        assertThat(responseJson.get("masked").asBoolean()).isTrue();
        assertThat(responseJson.has("body")).isFalse();
    }

    @Test
    @DisplayName("REQUEST 페이로드에서 apikey와 userid는 마스킹되어 평문이 노출되지 않는다")
    void shouldMaskApiKeyAndUserIdInRequestPayload() {
        String responseBody = "{\"result_code\":0,\"message\":\"OK\"}";
        mockServer.expect(requestTo("http://localhost" + PATH))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", "01012345678");

        aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class);

        JsonNode requestJson = findRequestPayloadJson();
        JsonNode paramsNode = requestJson.get("params");
        assertThat(paramsNode).isNotNull();
        String maskedApiKey = paramsNode.get("apikey").asText();
        String maskedUserId = paramsNode.get("userid").asText();
        assertThat(maskedApiKey).doesNotContain("verysecret-apikey-1234");
        assertThat(maskedApiKey).contains("*");
        assertThat(maskedUserId).doesNotContain("user-id-67890");
        assertThat(maskedUserId).contains("*");
    }

    private JsonNode findResponsePayloadJson() {
        return findByType(NotificationVendorCommunicationType.RESPONSE);
    }

    private JsonNode findRequestPayloadJson() {
        return findByType(NotificationVendorCommunicationType.REQUEST);
    }

    private JsonNode findByType(NotificationVendorCommunicationType type) {
        return recordedCalls.stream()
            .filter(call -> call.communicationType() == type)
            .findFirst()
            .orElseThrow(() -> new AssertionError(type + " 통신 이력이 기록되지 않았습니다."))
            .payloadJson();
    }

    private record RecordedCall(
        NotificationVendorCommunicationType communicationType,
        JsonNode payloadJson
    ) {
    }
}
