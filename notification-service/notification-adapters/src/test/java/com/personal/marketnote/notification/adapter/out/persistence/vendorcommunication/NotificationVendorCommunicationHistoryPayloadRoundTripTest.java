package com.personal.marketnote.notification.adapter.out.persistence.vendorcommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.configuration.ObjectMapperConfig;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.notification.adapter.out.persistence.vendorcommunication.entity.NotificationVendorCommunicationHistoryJpaEntity;
import com.personal.marketnote.notification.adapter.out.vendor.aligo.AttemptResult;
import com.personal.marketnote.notification.domain.vendorcommunication.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Aligo retry 정책으로 추가된 payload JSON 메타({@code attemptCount}/{@code attempts})에 대한 라운드트립 검증.
 *
 * <p>본 PR이 변경한 직렬화 정책(메모리 {@code feedback_serialization_policy_roundtrip_required}) 정합 — payload JSON에
 * {@code attempts} 배열을 추가한 뒤, JsonNode → 문자열 → JsonNode 라운드트립 + Mapper 경유 도메인-엔티티 라운드트립까지
 * 검증해 직렬화 회귀를 사전 차단한다.</p>
 *
 * <p>JSONB 컬럼 영속화 자체는 H2 PostgreSQL 모드의 한계(jsonb 미지원)로 별도 IT가 의미 없으므로,
 * Mapper 경유 양방향 변환 + ObjectMapper 라운드트립으로 직렬화 정합을 보장한다.</p>
 */
class NotificationVendorCommunicationHistoryPayloadRoundTripTest {

    private static final NotificationVendorCommunicationTargetType TARGET_TYPE =
        NotificationVendorCommunicationTargetType.ALIMTALK;

    private final ObjectMapper objectMapper = new ObjectMapperConfig().objectMapper();
    private final VendorCommunicationPayloadGenerator payloadGenerator =
        new VendorCommunicationPayloadGenerator(objectMapper);

    @Test
    @DisplayName("attempts 배열을 포함한 payload JSON은 문자열 직렬화 후 동일 JsonNode로 복원된다")
    void shouldPreserveAttemptsArrayThroughJsonStringRoundTrip() throws Exception {
        List<AttemptResult> attempts = List.of(
            AttemptResult.httpFailure(1, 502, "ServerErrorException"),
            AttemptResult.parseFailure(2, "JsonParseException"),
            AttemptResult.success(3, null)
        );
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("resultCode", 0);
        map.put("message", "OK");
        map.put("attemptCount", 3);
        map.put("attempts", attempts);

        JsonNode original = payloadGenerator.buildPayloadJson(map);
        String serialized = original.toString();
        JsonNode restored = objectMapper.readTree(serialized);

        assertThat(restored).isEqualTo(original);
        assertThat(restored.get("attemptCount").asInt()).isEqualTo(3);
        assertThat(restored.get("attempts")).hasSize(3);
        assertThat(restored.get("attempts").get(0).get("status").asText()).isEqualTo("HTTP_FAILURE");
        assertThat(restored.get("attempts").get(0).get("httpStatus").asInt()).isEqualTo(502);
        assertThat(restored.get("attempts").get(1).get("status").asText()).isEqualTo("PARSE_FAILURE");
        assertThat(restored.get("attempts").get(2).get("status").asText()).isEqualTo("SUCCESS");
        assertThat(restored.get("attempts").get(2).get("httpStatus").isNull()).isTrue();
    }

    @Test
    @DisplayName("payload JSON 메타를 가진 도메인은 JpaEntity로 변환 후 다시 도메인으로 복원해도 동등하다")
    void shouldRoundTripDomainThroughJpaEntityMapper() {
        List<AttemptResult> attempts = List.of(
            AttemptResult.httpFailure(1, 500, "ServerErrorException"),
            AttemptResult.success(2, "RESULT_CODE_-101")
        );
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("resultCode", -101);
        map.put("message", "인증 오류");
        map.put("attemptCount", 2);
        map.put("attempts", attempts);

        JsonNode payloadJson = payloadGenerator.buildPayloadJson(map);
        String payload = payloadJson.toString();

        NotificationVendorCommunicationHistory original = NotificationVendorCommunicationHistory.from(
            NotificationVendorCommunicationHistoryCreateState.builder()
                .targetType(TARGET_TYPE)
                .targetId("template-1")
                .vendorName(NotificationVendorName.ALIGO)
                .communicationType(NotificationVendorCommunicationType.RESPONSE)
                .sender(NotificationVendorCommunicationSenderType.VENDOR)
                .exception("RESULT_CODE_-101")
                .payload(payload)
                .payloadJson(payloadJson)
                .attemptCount(2)
                .build()
        );

        NotificationVendorCommunicationHistoryJpaEntity entity =
            NotificationVendorCommunicationHistoryJpaEntity.from(original);
        NotificationVendorCommunicationHistory restored = entity.toDomain();

        assertThat(restored.getAttemptCount()).isEqualTo(2);
        assertThat(restored.getPayload()).isEqualTo(payload);
        assertThat(restored.getPayloadJson()).isEqualTo(payloadJson);
        assertThat(restored.getPayloadJson().get("attemptCount").asInt()).isEqualTo(2);
        assertThat(restored.getPayloadJson().get("attempts")).hasSize(2);
        assertThat(restored.getPayloadJson().get("attempts").get(0).get("httpStatus").asInt()).isEqualTo(500);
        assertThat(restored.getPayloadJson().get("attempts").get(1).get("status").asText()).isEqualTo("SUCCESS");
        assertThat(restored.getPayloadJson().get("attempts").get(1).get("exception").asText())
            .isEqualTo("RESULT_CODE_-101");
    }

    @Test
    @DisplayName("attempts 단일 SUCCESS 메타는 라운드트립 후 number=1 / status=SUCCESS / null exception을 보존한다")
    void shouldPreserveSingleSuccessAttemptThroughRoundTrip() throws Exception {
        List<AttemptResult> attempts = List.of(AttemptResult.success(1, null));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("attemptCount", 1);
        map.put("attempts", attempts);

        JsonNode original = payloadGenerator.buildPayloadJson(map);
        String serialized = original.toString();
        JsonNode restored = objectMapper.readTree(serialized);

        assertThat(restored.get("attempts")).hasSize(1);
        JsonNode attempt = restored.get("attempts").get(0);
        assertThat(attempt.get("number").asInt()).isEqualTo(1);
        assertThat(attempt.get("status").asText()).isEqualTo("SUCCESS");
        assertThat(attempt.get("httpStatus").isNull()).isTrue();
        assertThat(attempt.get("exception").isNull()).isTrue();
    }
}
