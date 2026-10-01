package com.personal.marketnote.product.utility;

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
    @DisplayName("buildPayloadJson")
    class BuildPayloadJson {

        @Test
        @DisplayName("payload가 null이면 빈 ObjectNode를 반환한다")
        void returnsEmptyObjectWhenNull() {
            JsonNode result = generator.buildPayloadJson(null);

            assertThat(result.isObject()).isTrue();
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("payload가 JsonNode면 그대로 반환한다")
        void returnsJsonNodeDirectly() {
            JsonNode node = objectMapper.createObjectNode().put("key", "value");

            JsonNode result = generator.buildPayloadJson(node);

            assertThat(result).isSameAs(node);
        }

        @Test
        @DisplayName("payload가 일반 객체면 valueToTree로 변환한다")
        void convertsPojoToJsonNode() {
            java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
            payload.put("name", "상품");
            payload.put("price", 10_000);

            JsonNode result = generator.buildPayloadJson(payload);

            assertThat(result.get("name").asText()).isEqualTo("상품");
            assertThat(result.get("price").asInt()).isEqualTo(10_000);
        }
    }

    @Nested
    @DisplayName("buildRequestPayloadJson")
    class BuildRequestPayloadJson {

        @Test
        @DisplayName("method/url/body/attempt를 포함한 JSON을 생성한다")
        void buildsRequestPayloadWithBody() {
            URI uri = URI.create("https://api.example.com/orders");
            java.util.Map<String, Object> body = java.util.Map.of("productId", 1L);

            JsonNode result = generator.buildRequestPayloadJson(HttpMethod.POST, uri, body, 2);

            assertThat(result.get("method").asText()).isEqualTo("POST");
            assertThat(result.get("url").asText()).isEqualTo("https://api.example.com/orders");
            assertThat(result.get("body").get("productId").asLong()).isEqualTo(1L);
            assertThat(result.get("attempt").asInt()).isEqualTo(2);
        }

        @Test
        @DisplayName("body가 null이면 body 필드를 제외한다")
        void omitsBodyWhenNull() {
            URI uri = URI.create("https://api.example.com/orders");

            JsonNode result = generator.buildRequestPayloadJson(HttpMethod.GET, uri, null, 1);

            assertThat(result.has("body")).isFalse();
            assertThat(result.get("method").asText()).isEqualTo("GET");
            assertThat(result.get("attempt").asInt()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("buildResponsePayloadJson")
    class BuildResponsePayloadJson {

        @Test
        @DisplayName("응답이 있으면 status/body/attempt를 포함한다")
        void buildsResponsePayloadWithBody() {
            ResponseEntity<java.util.Map<String, Object>> response = ResponseEntity
                    .status(HttpStatus.OK)
                    .body(java.util.Map.of("result", "success"));

            JsonNode result = generator.buildResponsePayloadJson(response, 1);

            assertThat(result.get("status").asInt()).isEqualTo(200);
            assertThat(result.get("body").get("result").asText()).isEqualTo("success");
            assertThat(result.get("attempt").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("응답이 null이면 attempt만 포함한다")
        void omitsStatusWhenResponseNull() {
            JsonNode result = generator.buildResponsePayloadJson(null, 3);

            assertThat(result.has("status")).isFalse();
            assertThat(result.has("body")).isFalse();
            assertThat(result.get("attempt").asInt()).isEqualTo(3);
        }

        @Test
        @DisplayName("응답 body가 null이면 body 필드를 제외한다")
        void omitsBodyWhenNull() {
            ResponseEntity<Void> response = ResponseEntity.status(HttpStatus.NO_CONTENT).build();

            JsonNode result = generator.buildResponsePayloadJson(response, 1);

            assertThat(result.get("status").asInt()).isEqualTo(204);
            assertThat(result.has("body")).isFalse();
        }
    }

    @Nested
    @DisplayName("buildErrorPayloadJson")
    class BuildErrorPayloadJson {

        @Test
        @DisplayName("error/message/attempt를 포함한 JSON을 생성한다")
        void buildsErrorPayload() {
            JsonNode result = generator.buildErrorPayloadJson("TIMEOUT", "요청 시간 초과", 2);

            assertThat(result.get("error").asText()).isEqualTo("TIMEOUT");
            assertThat(result.get("message").asText()).isEqualTo("요청 시간 초과");
            assertThat(result.get("attempt").asInt()).isEqualTo(2);
        }

        @Test
        @DisplayName("error가 null이면 error 필드를 제외한다")
        void omitsErrorWhenNull() {
            JsonNode result = generator.buildErrorPayloadJson(null, "메시지", 1);

            assertThat(result.has("error")).isFalse();
            assertThat(result.get("message").asText()).isEqualTo("메시지");
        }

        @Test
        @DisplayName("message가 null이면 message 필드를 제외한다")
        void omitsMessageWhenNull() {
            JsonNode result = generator.buildErrorPayloadJson("ERROR", null, 1);

            assertThat(result.get("error").asText()).isEqualTo("ERROR");
            assertThat(result.has("message")).isFalse();
        }
    }
}
