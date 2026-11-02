package com.personal.marketnote.common.utility.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceCommunicationPayloadGeneratorTest {

    private ServiceCommunicationPayloadGenerator generator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        generator = new ServiceCommunicationPayloadGenerator(objectMapper);
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
    @DisplayName("buildRequestPayloadJson 메서드")
    class BuildRequestPayloadJsonTest {

        @Test
        @DisplayName("method/url/body/attempt가 포함된 요청 페이로드를 생성한다")
        void shouldBuildRequestPayloadWithAllFields() {
            URI uri = URI.create("https://example.com/api");
            Map<String, Object> body = Map.of("key", "value");

            JsonNode result = generator.buildRequestPayloadJson(HttpMethod.POST, uri, body, 1);

            assertThat(result.get("method").asText()).isEqualTo("POST");
            assertThat(result.get("url").asText()).isEqualTo("https://example.com/api");
            assertThat(result.get("body")).isNotNull();
            assertThat(result.get("body").get("key").asText()).isEqualTo("value");
            assertThat(result.get("attempt").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("body가 null이면 body 필드는 생략된다")
        void shouldOmitBodyFieldWhenBodyIsNull() {
            URI uri = URI.create("https://example.com/api");

            JsonNode result = generator.buildRequestPayloadJson(HttpMethod.GET, uri, null, 2);

            assertThat(result.get("method").asText()).isEqualTo("GET");
            assertThat(result.get("url").asText()).isEqualTo("https://example.com/api");
            assertThat(result.has("body")).isFalse();
            assertThat(result.get("attempt").asInt()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("buildResponsePayloadJson 메서드")
    class BuildResponsePayloadJsonTest {

        @Test
        @DisplayName("2xx 응답이면 status와 body와 attempt를 포함한 페이로드를 생성한다")
        void shouldBuildResponsePayloadForSuccess() {
            ResponseEntity<Map<String, Object>> response =
                    ResponseEntity.ok(Map.of("result", "ok"));

            JsonNode result = generator.buildResponsePayloadJson(response, 1);

            assertThat(result.get("status").asInt()).isEqualTo(HttpStatus.OK.value());
            assertThat(result.get("body")).isNotNull();
            assertThat(result.get("body").get("result").asText()).isEqualTo("ok");
            assertThat(result.get("attempt").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("body가 null이면 body 필드는 생략되고 status/attempt만 포함된다")
        void shouldOmitBodyFieldWhenResponseBodyIsNull() {
            ResponseEntity<Object> response = ResponseEntity.noContent().build();

            JsonNode result = generator.buildResponsePayloadJson(response, 1);

            assertThat(result.get("status").asInt()).isEqualTo(HttpStatus.NO_CONTENT.value());
            assertThat(result.has("body")).isFalse();
            assertThat(result.get("attempt").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("response 자체가 null이면 attempt만 포함된 페이로드를 생성한다")
        void shouldIncludeOnlyAttemptWhenResponseIsNull() {
            JsonNode result = generator.buildResponsePayloadJson(null, 3);

            assertThat(result.has("status")).isFalse();
            assertThat(result.has("body")).isFalse();
            assertThat(result.get("attempt").asInt()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("buildErrorPayloadJson 메서드")
    class BuildErrorPayloadJsonTest {

        @Test
        @DisplayName("error와 message와 attempt가 포함된 에러 페이로드를 생성한다")
        void shouldBuildErrorPayloadWithAllFields() {
            JsonNode result = generator.buildErrorPayloadJson("IOException", "timeout", 2);

            assertThat(result.get("error").asText()).isEqualTo("IOException");
            assertThat(result.get("message").asText()).isEqualTo("timeout");
            assertThat(result.get("attempt").asInt()).isEqualTo(2);
        }

        @Test
        @DisplayName("error가 null이면 error 필드는 생략된다")
        void shouldOmitErrorFieldWhenErrorIsNull() {
            JsonNode result = generator.buildErrorPayloadJson(null, "msg", 1);

            assertThat(result.has("error")).isFalse();
            assertThat(result.get("message").asText()).isEqualTo("msg");
            assertThat(result.get("attempt").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("message가 null이면 message 필드는 생략된다")
        void shouldOmitMessageFieldWhenMessageIsNull() {
            JsonNode result = generator.buildErrorPayloadJson("IOException", null, 1);

            assertThat(result.get("error").asText()).isEqualTo("IOException");
            assertThat(result.has("message")).isFalse();
            assertThat(result.get("attempt").asInt()).isEqualTo(1);
        }
    }
}
