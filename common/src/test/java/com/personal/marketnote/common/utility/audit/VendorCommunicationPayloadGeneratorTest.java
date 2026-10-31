package com.personal.marketnote.common.utility.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VendorCommunicationPayloadGeneratorTest {

    private VendorCommunicationPayloadGenerator generator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        generator = new VendorCommunicationPayloadGenerator(objectMapper);
    }

    @Nested
    @DisplayName("buildPayloadJson 메서드")
    class BuildPayloadJsonTest {

        @Test
        @DisplayName("payload가 null이면 빈 ObjectNode를 반환한다")
        void shouldReturnEmptyObjectNodeWhenPayloadIsNull() {
            JsonNode result = generator.buildPayloadJson(null);

            assertThat(result).isNotNull();
            assertThat(result.isObject()).isTrue();
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("payload가 JsonNode이면 동일 인스턴스를 그대로 반환한다")
        void shouldReturnSameJsonNodeWhenPayloadIsJsonNode() {
            JsonNode input = objectMapper.createObjectNode().put("key", "value");

            JsonNode result = generator.buildPayloadJson(input);

            assertThat(result).isSameAs(input);
        }

        @Test
        @DisplayName("payload가 일반 POJO이면 JsonNode 트리로 변환한다")
        void shouldConvertPojoToJsonNodeTree() {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("name", "테스트");
            payload.put("count", 3);

            JsonNode result = generator.buildPayloadJson(payload);

            assertThat(result.get("name").asText()).isEqualTo("테스트");
            assertThat(result.get("count").asInt()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("buildErrorPayloadJson 메서드")
    class BuildErrorPayloadJsonTest {

        @Test
        @DisplayName("error와 message가 모두 포함된 에러 페이로드를 생성한다")
        void shouldBuildErrorPayloadWithAllFields() {
            JsonNode result = generator.buildErrorPayloadJson("IOException", "timeout");

            assertThat(result.get("error").asText()).isEqualTo("IOException");
            assertThat(result.get("message").asText()).isEqualTo("timeout");
        }

        @Test
        @DisplayName("error가 null이면 error 필드는 생략된다")
        void shouldOmitErrorFieldWhenErrorIsNull() {
            JsonNode result = generator.buildErrorPayloadJson(null, "msg");

            assertThat(result.has("error")).isFalse();
            assertThat(result.get("message").asText()).isEqualTo("msg");
        }

        @Test
        @DisplayName("message가 null이면 message 필드는 생략된다")
        void shouldOmitMessageFieldWhenMessageIsNull() {
            JsonNode result = generator.buildErrorPayloadJson("IOException", null);

            assertThat(result.get("error").asText()).isEqualTo("IOException");
            assertThat(result.has("message")).isFalse();
        }

        @Test
        @DisplayName("error와 message가 모두 null이면 빈 ObjectNode를 반환한다")
        void shouldReturnEmptyObjectNodeWhenBothNull() {
            JsonNode result = generator.buildErrorPayloadJson(null, null);

            assertThat(result).isNotNull();
            assertThat(result.isObject()).isTrue();
            assertThat(result.isEmpty()).isTrue();
        }
    }
}
