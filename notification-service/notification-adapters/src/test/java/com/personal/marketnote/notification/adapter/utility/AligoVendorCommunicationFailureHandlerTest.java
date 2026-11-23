package com.personal.marketnote.notification.adapter.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationTargetType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AligoVendorCommunicationFailureHandler의 페이로드 조립 로직 검증.
 *
 * <p>private 메서드 직접 호출을 피하고 public 진입점(handleHttpFailure / handleParseFailure)을 통해
 * recorder에 전달되는 payloadJson을 캡처하여 의미를 검증한다.</p>
 */
class AligoVendorCommunicationFailureHandlerTest {

    private static final NotificationVendorCommunicationTargetType TARGET_TYPE =
            NotificationVendorCommunicationTargetType.ALIMTALK;
    private static final String REQUEST_PAYLOAD = "{\"op\":\"send\"}";
    private static final int STATUS_CODE = 500;
    private static final String EXCEPTION_NAME = "JsonParseException";

    private List<RecordedCall> recordedCalls;
    private AligoVendorCommunicationFailureHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        recordedCalls = new ArrayList<>();
        objectMapper = new ObjectMapper();
        AligoVendorCommunicationRecorder recorder = new AligoVendorCommunicationRecorder(
                (
                        targetType, targetId, vendorName, communicationType, sender,
                        exception, payload, payloadJson, attemptCount
                ) ->
                        recordedCalls.add(new RecordedCall(communicationType, payloadJson))
        );
        VendorCommunicationPayloadGenerator payloadGenerator =
                new VendorCommunicationPayloadGenerator(objectMapper);
        handler = new AligoVendorCommunicationFailureHandler(recorder, payloadGenerator);
    }

    @Test
    @DisplayName("handleHttpFailure: responseBody가 null이면 RESPONSE 페이로드에 body 키가 없다")
    void shouldOmitBodyKeyWhenResponseBodyIsNullOnHttpFailure() {
        JsonNode requestPayloadJson = objectMapper.createObjectNode();

        handler.handleHttpFailure(
                TARGET_TYPE, REQUEST_PAYLOAD, requestPayloadJson, STATUS_CODE, null, false
        );

        JsonNode responseJson = findResponsePayloadJson();
        assertThat(responseJson.has("body")).isFalse();
        assertThat(responseJson.get("status").asInt()).isEqualTo(STATUS_CODE);
    }

    @Test
    @DisplayName("handleHttpFailure: responseBody가 빈 문자열이면 RESPONSE 페이로드에 body 키가 빈 문자열로 포함된다")
    void shouldIncludeEmptyBodyKeyWhenResponseBodyIsBlankOnHttpFailure() {
        JsonNode requestPayloadJson = objectMapper.createObjectNode();

        handler.handleHttpFailure(
                TARGET_TYPE, REQUEST_PAYLOAD, requestPayloadJson, STATUS_CODE, "", false
        );

        JsonNode responseJson = findResponsePayloadJson();
        assertThat(responseJson.has("body")).isTrue();
        assertThat(responseJson.get("body").asText()).isEmpty();
    }

    @Test
    @DisplayName("handleHttpFailure: responseBody가 일반 문자열이면 RESPONSE 페이로드에 body 키가 해당 문자열로 포함된다")
    void shouldIncludeBodyKeyWhenResponseBodyIsStringOnHttpFailure() {
        JsonNode requestPayloadJson = objectMapper.createObjectNode();
        String responseBody = "Internal Server Error";

        handler.handleHttpFailure(
                TARGET_TYPE, REQUEST_PAYLOAD, requestPayloadJson, STATUS_CODE, responseBody, false
        );

        JsonNode responseJson = findResponsePayloadJson();
        assertThat(responseJson.get("body").asText()).isEqualTo(responseBody);
    }

    @Test
    @DisplayName("handleHttpFailure: masked=true이면 body/status 외 masked 플래그만 포함된다")
    void shouldEmitMaskedFlagWhenMaskedTrueOnHttpFailure() {
        JsonNode requestPayloadJson = objectMapper.createObjectNode();

        handler.handleHttpFailure(
                TARGET_TYPE, REQUEST_PAYLOAD, requestPayloadJson, STATUS_CODE, "leak", true
        );

        JsonNode responseJson = findResponsePayloadJson();
        assertThat(responseJson.get("masked").asBoolean()).isTrue();
        assertThat(responseJson.get("status").asInt()).isEqualTo(STATUS_CODE);
        assertThat(responseJson.has("body")).isFalse();
    }

    @Test
    @DisplayName("handleParseFailure: responseBody가 null이면 RESPONSE 페이로드에 body 키가 없다")
    void shouldOmitBodyKeyWhenResponseBodyIsNullOnParseFailure() {
        JsonNode requestPayloadJson = objectMapper.createObjectNode();

        handler.handleParseFailure(
                TARGET_TYPE, REQUEST_PAYLOAD, requestPayloadJson, null, EXCEPTION_NAME, false
        );

        JsonNode responseJson = findResponsePayloadJson();
        assertThat(responseJson.has("body")).isFalse();
        assertThat(responseJson.get("exception").asText()).isEqualTo(EXCEPTION_NAME);
    }

    @Test
    @DisplayName("handleParseFailure: responseBody가 빈 문자열이면 RESPONSE 페이로드에 body 키가 빈 문자열로 포함된다")
    void shouldIncludeEmptyBodyKeyWhenResponseBodyIsBlankOnParseFailure() {
        JsonNode requestPayloadJson = objectMapper.createObjectNode();

        handler.handleParseFailure(
                TARGET_TYPE, REQUEST_PAYLOAD, requestPayloadJson, "", EXCEPTION_NAME, false
        );

        JsonNode responseJson = findResponsePayloadJson();
        assertThat(responseJson.has("body")).isTrue();
        assertThat(responseJson.get("body").asText()).isEmpty();
    }

    @Test
    @DisplayName("handleParseFailure: responseBody가 일반 문자열이면 RESPONSE 페이로드에 body 키가 해당 문자열로 포함된다")
    void shouldIncludeBodyKeyWhenResponseBodyIsStringOnParseFailure() {
        JsonNode requestPayloadJson = objectMapper.createObjectNode();
        String responseBody = "broken-json";

        handler.handleParseFailure(
                TARGET_TYPE, REQUEST_PAYLOAD, requestPayloadJson, responseBody, EXCEPTION_NAME, false
        );

        JsonNode responseJson = findResponsePayloadJson();
        assertThat(responseJson.get("body").asText()).isEqualTo(responseBody);
    }

    @Test
    @DisplayName("handleParseFailure: masked=true이면 exception/masked 플래그만 포함되고 body는 제외된다")
    void shouldEmitMaskedFlagWhenMaskedTrueOnParseFailure() {
        JsonNode requestPayloadJson = objectMapper.createObjectNode();

        handler.handleParseFailure(
                TARGET_TYPE, REQUEST_PAYLOAD, requestPayloadJson, "leak", EXCEPTION_NAME, true
        );

        JsonNode responseJson = findResponsePayloadJson();
        assertThat(responseJson.get("masked").asBoolean()).isTrue();
        assertThat(responseJson.get("exception").asText()).isEqualTo(EXCEPTION_NAME);
        assertThat(responseJson.has("body")).isFalse();
    }

    private JsonNode findResponsePayloadJson() {
        return recordedCalls.stream()
                .filter(call -> call.communicationType() == NotificationVendorCommunicationType.RESPONSE)
                .findFirst()
                .orElseThrow(() -> new AssertionError("RESPONSE 통신 이력이 기록되지 않았습니다."))
                .payloadJson();
    }

    private record RecordedCall(
            NotificationVendorCommunicationType communicationType,
            JsonNode payloadJson
    ) {
    }
}
