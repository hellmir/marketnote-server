package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.utility.audit.VendorCommunicationHistoryDispatcher;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.notification.adapter.utility.AligoVendorCommunicationFailureHandler;
import com.personal.marketnote.notification.adapter.utility.AligoVendorCommunicationRecorder;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoApiRequestFailedException;
import com.personal.marketnote.notification.domain.aligo.AligoAuthenticationException;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationSenderType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationTargetType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorName;
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

class AligoHttpClientVendorCommunicationRecordTest {

    private static final String OPERATION = "sendAlimTalk";
    private static final String PATH = "/akv10/alimtalk/send/";
    private static final String SENSITIVE_PATH = "/auth/login/";

    private AligoHttpClient aligoHttpClient;
    private MockRestServiceServer mockServer;
    private List<RecordCall> recordCalls;
    private List<HttpFailureCall> httpFailureCalls;

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
        ExternalCallLogger externalCallLogger = new ExternalCallLogger(new SimpleMeterRegistry());
        recordCalls = new ArrayList<>();
        httpFailureCalls = new ArrayList<>();

        VendorCommunicationHistoryDispatcher<
                NotificationVendorCommunicationTargetType,
                NotificationVendorName,
                NotificationVendorCommunicationType,
                NotificationVendorCommunicationSenderType
                > dispatcher = (
                        targetType, targetId, vendorName, communicationType, sender,
                        exception, payload, payloadJson, attemptCount
                ) ->
                recordCalls.add(new RecordCall(
                        targetType, targetId, vendorName, communicationType, sender, exception, payload
                ));

        AligoVendorCommunicationRecorder recorder = new AligoVendorCommunicationRecorder(dispatcher);
        VendorCommunicationPayloadGenerator payloadGenerator = new VendorCommunicationPayloadGenerator(objectMapper);
        AligoVendorCommunicationFailureHandler failureHandler =
                new AligoVendorCommunicationFailureHandler(recorder, payloadGenerator) {
                    @Override
                    public void handleHttpFailure(
                            NotificationVendorCommunicationTargetType targetType,
                            String requestPayload,
                            JsonNode requestPayloadJson,
                            int statusCode,
                            String responseBody,
                            boolean masked,
                            int attemptCount,
                            List<AttemptResult> attempts
                    ) {
                        httpFailureCalls.add(new HttpFailureCall(
                                targetType, requestPayload, statusCode, responseBody, masked
                        ));
                        super.handleHttpFailure(
                                targetType, requestPayload, requestPayloadJson, statusCode, responseBody, masked,
                                attemptCount, attempts
                        );
                    }
                };
        AligoOperationTargetTypeResolver resolver = new AligoOperationTargetTypeResolver();

        aligoHttpClient = new AligoHttpClient(
                restClient, objectMapper, properties, externalCallLogger,
                recorder, failureHandler, payloadGenerator, resolver
        );
    }

    @Test
    @DisplayName("정상 응답 시 REQUEST/RESPONSE 2건의 통신 이력을 기록한다")
    void shouldRecordRequestAndResponseOnSuccess() {
        String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
        mockServer.expect(requestTo("http://localhost" + PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", "01012345678");

        aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class);

        assertThat(recordCalls).hasSize(2);
        assertThat(recordCalls.get(0).communicationType).isEqualTo(NotificationVendorCommunicationType.REQUEST);
        assertThat(recordCalls.get(0).sender).isEqualTo(NotificationVendorCommunicationSenderType.SERVER);
        assertThat(recordCalls.get(0).vendorName).isEqualTo(NotificationVendorName.ALIGO);
        assertThat(recordCalls.get(0).targetType).isEqualTo(NotificationVendorCommunicationTargetType.ALIMTALK);
        assertThat(recordCalls.get(1).communicationType).isEqualTo(NotificationVendorCommunicationType.RESPONSE);
        assertThat(recordCalls.get(1).sender).isEqualTo(NotificationVendorCommunicationSenderType.VENDOR);
        assertThat(recordCalls.get(1).exception).isNull();
    }

    @Test
    @DisplayName("HTTP 4xx 실패 시 failureHandler가 호출된다")
    void shouldDelegateToFailureHandlerOnHttp4xx() {
        mockServer.expect(requestTo("http://localhost" + PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .body("{\"result_code\":-1,\"message\":\"bad\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", "01012345678");

        assertThatThrownBy(() -> aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class))
                .isInstanceOf(AligoApiRequestFailedException.class);

        assertThat(httpFailureCalls).hasSize(1);
        assertThat(httpFailureCalls.get(0).statusCode).isEqualTo(400);
        assertThat(httpFailureCalls.get(0).masked).isFalse();
        // failure handler가 REQUEST + RESPONSE 두건을 record에 위임
        assertThat(recordCalls).hasSize(2);
        assertThat(recordCalls.get(0).exception).isNotNull();
        assertThat(recordCalls.get(1).exception).isNotNull();
    }

    @Test
    @DisplayName("Aligo resultCode가 0이 아닐 때 RESPONSE에 exception이 채워져 record된다")
    void shouldRecordResponseWithExceptionOnNonZeroResultCode() {
        String responseBody = "{\"result_code\":-101,\"message\":\"인증 오류\"}";
        mockServer.expect(requestTo("http://localhost" + PATH))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", "01012345678");

        assertThatThrownBy(() -> aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class))
                .isInstanceOf(AligoAuthenticationException.class);

        assertThat(recordCalls).hasSize(2);
        RecordCall responseRecord = recordCalls.get(1);
        assertThat(responseRecord.communicationType).isEqualTo(NotificationVendorCommunicationType.RESPONSE);
        assertThat(responseRecord.exception).isNotNull();
        assertThat(responseRecord.exception).contains("-101");
    }

    @Test
    @DisplayName("응답 파싱 실패 시 RESPONSE에 exception이 채워져 record된다")
    void shouldRecordResponseWithExceptionOnParseFailure() {
        String responseBody = "invalid json";
        mockServer.expect(requestTo("http://localhost" + PATH))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", "01012345678");

        assertThatThrownBy(() -> aligoHttpClient.post(OPERATION, PATH, params, AligoApiResponse.class))
                .isInstanceOf(AligoApiRequestFailedException.class);

        // 최소 1건 RESPONSE 기록되어야 함 + exception 채워짐
        boolean responseWithException = recordCalls.stream()
                .anyMatch(c -> c.communicationType == NotificationVendorCommunicationType.RESPONSE
                        && c.exception != null);
        assertThat(responseWithException).isTrue();
    }

    @Test
    @DisplayName("sensitive path + 4xx 시 응답 body는 마스킹 메타로 대체되어 record된다")
    void shouldRecordMaskedMetadataOnSensitivePathHttpFailure() {
        mockServer.expect(requestTo("http://localhost" + SENSITIVE_PATH))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .body("sensitive secret response")
                        .contentType(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", "01012345678");

        assertThatThrownBy(() -> aligoHttpClient.post(OPERATION, SENSITIVE_PATH, params, AligoApiResponse.class))
                .isInstanceOf(AligoApiRequestFailedException.class);

        assertThat(httpFailureCalls).hasSize(1);
        assertThat(httpFailureCalls.get(0).masked).isTrue();

        // RESPONSE record에 sensitive secret response 평문이 들어가지 않고 masked=true 메타가 들어가야 함
        boolean responseMaskedAndNoSecret = recordCalls.stream()
                .filter(c -> c.communicationType == NotificationVendorCommunicationType.RESPONSE)
                .anyMatch(c -> c.payload.contains("\"masked\":true")
                        && !c.payload.contains("sensitive secret response"));
        assertThat(responseMaskedAndNoSecret).isTrue();
    }

    @Test
    @DisplayName("postMultipart 정상 응답 시 REQUEST/RESPONSE 2건의 통신 이력을 기록한다")
    void shouldRecordRequestAndResponseOnPostMultipartSuccess() {
        String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
        mockServer.expect(requestTo("http://localhost" + PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("receiver", "01012345678");

        aligoHttpClient.postMultipart(OPERATION, PATH, params, AligoApiResponse.class);

        assertThat(recordCalls).hasSize(2);
        assertThat(recordCalls.get(0).communicationType).isEqualTo(NotificationVendorCommunicationType.REQUEST);
        assertThat(recordCalls.get(1).communicationType).isEqualTo(NotificationVendorCommunicationType.RESPONSE);
    }

    @Test
    @DisplayName("postMultipart sensitive path + 4xx 시 응답 body는 마스킹 메타로 대체되어 record된다")
    void shouldRecordMaskedMetadataOnPostMultipartSensitivePathHttpFailure() {
        mockServer.expect(requestTo("http://localhost" + SENSITIVE_PATH))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .body("sensitive multipart secret")
                        .contentType(MediaType.APPLICATION_JSON));

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("receiver", "01012345678");

        assertThatThrownBy(() -> aligoHttpClient.postMultipart(
                OPERATION, SENSITIVE_PATH, params, AligoApiResponse.class
        ))
                .isInstanceOf(AligoApiRequestFailedException.class);

        assertThat(httpFailureCalls).hasSize(1);
        assertThat(httpFailureCalls.get(0).masked).isTrue();
        boolean responseMasked = recordCalls.stream()
                .filter(c -> c.communicationType == NotificationVendorCommunicationType.RESPONSE)
                .anyMatch(c -> c.payload.contains("\"masked\":true")
                        && !c.payload.contains("sensitive multipart secret"));
        assertThat(responseMasked).isTrue();
    }

    private record RecordCall(
            NotificationVendorCommunicationTargetType targetType,
            String targetId,
            NotificationVendorName vendorName,
            NotificationVendorCommunicationType communicationType,
            NotificationVendorCommunicationSenderType sender,
            String exception,
            String payload
    ) {
    }

    private record HttpFailureCall(
            NotificationVendorCommunicationTargetType targetType,
            String requestPayload,
            int statusCode,
            String responseBody,
            boolean masked
    ) {
    }
}
