package com.personal.marketnote.user.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ServiceCommunicationPayloadGeneratorTest {

    @InjectMocks
    private ServiceCommunicationPayloadGenerator generator;

    @Spy
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("buildPayloadJson - 페이로드 JSON 변환")
    class BuildPayloadJsonTest {

        @Test
        @DisplayName("null 입력 시 빈 ObjectNode를 반환한다")
        void shouldReturnEmptyObjectNodeForNull() {
            JsonNode result = generator.buildPayloadJson(null);
            assertThat(result.isObject()).isTrue();
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("JsonNode 입력 시 그대로 반환한다")
        void shouldReturnJsonNodeAsIs() {
            ObjectNode input = objectMapper.createObjectNode();
            input.put("key", "value");

            JsonNode result = generator.buildPayloadJson(input);

            assertThat(result).isSameAs(input);
        }

        @Test
        @DisplayName("일반 객체를 JsonNode로 변환한다")
        void shouldConvertObjectToJsonNode() {
            record TestPayload(String name, int value) {}
            TestPayload payload = new TestPayload("test", 42);

            JsonNode result = generator.buildPayloadJson(payload);

            assertThat(result.get("name").asText()).isEqualTo("test");
            assertThat(result.get("value").asInt()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("buildRequestPayloadJson - 요청 페이로드 생성")
    class BuildRequestPayloadJsonTest {

        @Test
        @DisplayName("HTTP 메서드, URL, 바디, 시도 횟수를 포함한 JSON을 생성한다")
        void shouldBuildRequestPayloadWithAllFields() {
            JsonNode result = generator.buildRequestPayloadJson(
                    HttpMethod.POST, URI.create("https://api.example.com/users"), "request-body", 1
            );

            assertThat(result.get("method").asText()).isEqualTo("POST");
            assertThat(result.get("url").asText()).isEqualTo("https://api.example.com/users");
            assertThat(result.get("body").asText()).isEqualTo("request-body");
            assertThat(result.get("attempt").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("바디가 null이면 body 필드를 포함하지 않는다")
        void shouldExcludeBodyWhenNull() {
            JsonNode result = generator.buildRequestPayloadJson(HttpMethod.GET, URI.create("https://api.example.com"), null, 1);

            assertThat(result.has("body")).isFalse();
            assertThat(result.get("method").asText()).isEqualTo("GET");
        }
    }

    @Nested
    @DisplayName("buildResponsePayloadJson - 응답 페이로드 생성")
    class BuildResponsePayloadJsonTest {

        @Test
        @DisplayName("응답 상태 코드와 바디를 포함한 JSON을 생성한다")
        void shouldBuildResponsePayloadWithStatusAndBody() {
            ResponseEntity<String> response = ResponseEntity.ok("success");

            JsonNode result = generator.buildResponsePayloadJson(response, 1);

            assertThat(result.get("status").asInt()).isEqualTo(200);
            assertThat(result.get("body").asText()).isEqualTo("success");
            assertThat(result.get("attempt").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("응답이 null이면 attempt만 포함한다")
        void shouldIncludeOnlyAttemptWhenResponseIsNull() {
            JsonNode result = generator.buildResponsePayloadJson(null, 2);

            assertThat(result.has("status")).isFalse();
            assertThat(result.get("attempt").asInt()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("buildErrorPayloadJson - 에러 페이로드 생성")
    class BuildErrorPayloadJsonTest {

        @Test
        @DisplayName("에러 타입과 메시지를 포함한 JSON을 생성한다")
        void shouldBuildErrorPayloadWithErrorAndMessage() {
            JsonNode result = generator.buildErrorPayloadJson("ConnectionError", "Connection refused", 3);

            assertThat(result.get("error").asText()).isEqualTo("ConnectionError");
            assertThat(result.get("message").asText()).isEqualTo("Connection refused");
            assertThat(result.get("attempt").asInt()).isEqualTo(3);
        }

        @Test
        @DisplayName("에러와 메시지가 null이면 attempt만 포함한다")
        void shouldIncludeOnlyAttemptWhenErrorAndMessageAreNull() {
            JsonNode result = generator.buildErrorPayloadJson(null, null, 1);

            assertThat(result.has("error")).isFalse();
            assertThat(result.has("message")).isFalse();
            assertThat(result.get("attempt").asInt()).isEqualTo(1);
        }
    }
}
