package com.personal.marketnote.file.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceCommunicationPayloadGeneratorTest {
    @InjectMocks
    private ServiceCommunicationPayloadGenerator generator;

    @Mock
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("buildPayloadJson")
    class BuildPayloadJson {
        @Test
        @DisplayName("payload가 null이면 빈 ObjectNode를 반환한다")
        void returnsEmptyObjectNodeWhenPayloadIsNull() {
            // given
            ObjectMapper realMapper = new ObjectMapper();
            when(objectMapper.createObjectNode()).thenReturn(realMapper.createObjectNode());

            // when
            JsonNode result = generator.buildPayloadJson(null);

            // then
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("payload가 JsonNode이면 그대로 반환한다")
        void returnsJsonNodeAsIs() {
            // given
            ObjectMapper realMapper = new ObjectMapper();
            JsonNode input = realMapper.createObjectNode().put("key", "value");

            // when
            JsonNode result = generator.buildPayloadJson(input);

            // then
            assertThat(result).isEqualTo(input);
        }

        @Test
        @DisplayName("payload가 일반 객체이면 valueToTree로 변환한다")
        void convertsObjectToJsonNode() {
            // given
            ObjectMapper realMapper = new ObjectMapper();
            Map<String, String> payload = Map.of("key", "value");
            JsonNode expected = realMapper.valueToTree(payload);
            when(objectMapper.valueToTree(payload)).thenReturn(expected);

            // when
            JsonNode result = generator.buildPayloadJson(payload);

            // then
            assertThat(result.get("key").asText()).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("buildRequestPayloadJson")
    class BuildRequestPayloadJson {
        @Test
        @DisplayName("HTTP 요청 정보를 포함한 JsonNode를 생성한다")
        void buildsRequestPayloadWithMethodUrlAndAttempt() {
            // given
            ObjectMapper realMapper = new ObjectMapper();
            when(objectMapper.valueToTree(any())).thenAnswer(invocation -> realMapper.valueToTree(invocation.getArgument(0)));

            // when
            JsonNode result = generator.buildRequestPayloadJson(
                    HttpMethod.POST, URI.create("https://api.example.com/test"), null, 1
            );

            // then
            assertThat(result.get("method").asText()).isEqualTo("POST");
            assertThat(result.get("url").asText()).isEqualTo("https://api.example.com/test");
            assertThat(result.get("attempt").asInt()).isEqualTo(1);
            assertThat(result.has("body")).isFalse();
        }

        @Test
        @DisplayName("body가 존재하면 요청 페이로드에 포함한다")
        void includesBodyWhenPresent() {
            // given
            ObjectMapper realMapper = new ObjectMapper();
            when(objectMapper.valueToTree(any())).thenAnswer(invocation -> realMapper.valueToTree(invocation.getArgument(0)));

            // when
            JsonNode result = generator.buildRequestPayloadJson(
                    HttpMethod.POST, URI.create("https://api.example.com/test"), "request-body", 2
            );

            // then
            assertThat(result.get("body").asText()).isEqualTo("request-body");
            assertThat(result.get("attempt").asInt()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("buildResponsePayloadJson")
    class BuildResponsePayloadJson {
        @Test
        @DisplayName("HTTP 응답 정보를 포함한 JsonNode를 생성한다")
        void buildsResponsePayloadWithStatusAndBody() {
            // given
            ObjectMapper realMapper = new ObjectMapper();
            when(objectMapper.valueToTree(any())).thenAnswer(invocation -> realMapper.valueToTree(invocation.getArgument(0)));
            ResponseEntity<String> response = new ResponseEntity<>("response-body", HttpStatusCode.valueOf(200));

            // when
            JsonNode result = generator.buildResponsePayloadJson(response, 1);

            // then
            assertThat(result.get("status").asInt()).isEqualTo(200);
            assertThat(result.get("body").asText()).isEqualTo("response-body");
            assertThat(result.get("attempt").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("응답이 null이면 attempt만 포함한다")
        void includesOnlyAttemptWhenResponseIsNull() {
            // given
            ObjectMapper realMapper = new ObjectMapper();
            when(objectMapper.valueToTree(any())).thenAnswer(invocation -> realMapper.valueToTree(invocation.getArgument(0)));

            // when
            JsonNode result = generator.buildResponsePayloadJson(null, 3);

            // then
            assertThat(result.get("attempt").asInt()).isEqualTo(3);
            assertThat(result.has("status")).isFalse();
        }
    }

    @Nested
    @DisplayName("buildErrorPayloadJson")
    class BuildErrorPayloadJson {
        @Test
        @DisplayName("에러 정보를 포함한 JsonNode를 생성한다")
        void buildsErrorPayloadWithErrorAndMessage() {
            // given
            ObjectMapper realMapper = new ObjectMapper();
            when(objectMapper.valueToTree(any())).thenAnswer(invocation -> realMapper.valueToTree(invocation.getArgument(0)));

            // when
            JsonNode result = generator.buildErrorPayloadJson("IOException", "Connection refused", 1);

            // then
            assertThat(result.get("error").asText()).isEqualTo("IOException");
            assertThat(result.get("message").asText()).isEqualTo("Connection refused");
            assertThat(result.get("attempt").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("error와 message가 null이면 attempt만 포함한다")
        void includesOnlyAttemptWhenErrorAndMessageAreNull() {
            // given
            ObjectMapper realMapper = new ObjectMapper();
            when(objectMapper.valueToTree(any())).thenAnswer(invocation -> realMapper.valueToTree(invocation.getArgument(0)));

            // when
            JsonNode result = generator.buildErrorPayloadJson(null, null, 2);

            // then
            assertThat(result.get("attempt").asInt()).isEqualTo(2);
            assertThat(result.has("error")).isFalse();
            assertThat(result.has("message")).isFalse();
        }
    }
}
