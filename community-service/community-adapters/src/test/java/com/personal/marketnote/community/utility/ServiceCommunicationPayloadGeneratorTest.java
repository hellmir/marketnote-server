package com.personal.marketnote.community.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ServiceCommunicationPayloadGenerator")
class ServiceCommunicationPayloadGeneratorTest {

    private ObjectMapper objectMapper;
    private ServiceCommunicationPayloadGenerator generator;

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
        void returnsEmptyNodeWhenNull() {
            JsonNode result = generator.buildPayloadJson(null);

            assertThat(result).isNotNull();
            assertThat(result.size()).isZero();
        }

        @Test
        @DisplayName("payload가 JsonNode이면 그대로 반환한다")
        void returnsSameInstanceWhenAlreadyJsonNode() {
            ObjectNode given = objectMapper.createObjectNode().put("key", "value");

            JsonNode result = generator.buildPayloadJson(given);

            assertThat(result).isSameAs(given);
        }

        @Test
        @DisplayName("일반 객체는 JsonNode 트리로 변환한다")
        void convertsRegularObjectToJsonNode() {
            Map<String, Object> payload = Map.of("foo", 1, "bar", "baz");

            JsonNode result = generator.buildPayloadJson(payload);

            assertThat(result.get("foo").asInt()).isEqualTo(1);
            assertThat(result.get("bar").asText()).isEqualTo("baz");
        }
    }

    @Nested
    @DisplayName("buildRequestPayloadJson")
    class BuildRequestPayloadJson {

        @Test
        @DisplayName("body가 있으면 method/url/body/attempt를 모두 포함한다")
        void includesAllFieldsWhenBodyPresent() {
            URI uri = URI.create("https://commerce.example.com/api/test");
            Map<String, Object> body = Map.of("key", "value");

            JsonNode result = generator.buildRequestPayloadJson(HttpMethod.POST, uri, body, 2);

            assertThat(result.get("method").asText()).isEqualTo("POST");
            assertThat(result.get("url").asText()).isEqualTo(uri.toString());
            assertThat(result.get("body")).isNotNull();
            assertThat(result.get("body").get("key").asText()).isEqualTo("value");
            assertThat(result.get("attempt").asInt()).isEqualTo(2);
        }

        @Test
        @DisplayName("body가 null이면 body 키를 생략한다")
        void omitsBodyWhenNull() {
            URI uri = URI.create("https://commerce.example.com/api/test");

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
        @DisplayName("response가 null이면 attempt만 포함한다")
        void includesAttemptOnlyWhenResponseNull() {
            JsonNode result = generator.buildResponsePayloadJson(null, 3);

            assertThat(result.has("status")).isFalse();
            assertThat(result.has("body")).isFalse();
            assertThat(result.get("attempt").asInt()).isEqualTo(3);
        }

        @Test
        @DisplayName("response body가 없으면 status와 attempt만 포함한다")
        void includesStatusOnlyWhenBodyAbsent() {
            ResponseEntity<Object> response = ResponseEntity.ok().build();

            JsonNode result = generator.buildResponsePayloadJson(response, 1);

            assertThat(result.get("status").asInt()).isEqualTo(200);
            assertThat(result.has("body")).isFalse();
            assertThat(result.get("attempt").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("response body가 있으면 status/body/attempt를 포함한다")
        void includesAllWhenBodyPresent() {
            Map<String, Object> body = Map.of("result", "ok");
            ResponseEntity<Map<String, Object>> response =
                    new ResponseEntity<>(body, HttpStatus.OK);

            JsonNode result = generator.buildResponsePayloadJson(response, 1);

            assertThat(result.get("status").asInt()).isEqualTo(200);
            assertThat(result.get("body")).isNotNull();
            assertThat(result.get("body").get("result").asText()).isEqualTo("ok");
            assertThat(result.get("attempt").asInt()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("buildErrorPayloadJson")
    class BuildErrorPayloadJson {

        @Test
        @DisplayName("error/message가 모두 있으면 둘 다 포함한다")
        void includesErrorAndMessage() {
            JsonNode result = generator.buildErrorPayloadJson("ConnectException", "Connection refused", 1);

            assertThat(result.get("error").asText()).isEqualTo("ConnectException");
            assertThat(result.get("message").asText()).isEqualTo("Connection refused");
            assertThat(result.get("attempt").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("error가 null이면 error 키를 생략한다")
        void omitsErrorWhenNull() {
            JsonNode result = generator.buildErrorPayloadJson(null, "msg", 2);

            assertThat(result.has("error")).isFalse();
            assertThat(result.get("message").asText()).isEqualTo("msg");
        }

        @Test
        @DisplayName("message가 null이면 message 키를 생략한다")
        void omitsMessageWhenNull() {
            JsonNode result = generator.buildErrorPayloadJson("Err", null, 5);

            assertThat(result.get("error").asText()).isEqualTo("Err");
            assertThat(result.has("message")).isFalse();
            assertThat(result.get("attempt").asInt()).isEqualTo(5);
        }

        @Test
        @DisplayName("error/message 모두 null이면 attempt만 포함한다")
        void includesAttemptOnlyWhenAllNull() {
            JsonNode result = generator.buildErrorPayloadJson(null, null, 7);

            assertThat(result.has("error")).isFalse();
            assertThat(result.has("message")).isFalse();
            assertThat(result.get("attempt").asInt()).isEqualTo(7);
        }
    }
}
