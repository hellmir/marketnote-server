package com.personal.marketnote.file.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ServiceCommunicationPayloadGenerator {
    private final ObjectMapper objectMapper;

    public JsonNode buildPayloadJson(Object payload) {
        if (FormatValidator.hasNoValue(payload)) {
            return objectMapper.createObjectNode();
        }

        if (payload instanceof JsonNode jsonNode) {
            return jsonNode;
        }

        return objectMapper.valueToTree(payload);
    }

    public JsonNode buildRequestPayloadJson(HttpMethod method, URI uri, Object body, int attempt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", method.name());
        payload.put("url", uri.toString());
        if (FormatValidator.hasValue(body)) {
            payload.put("body", body);
        }
        payload.put("attempt", attempt);
        return buildPayloadJson(payload);
    }

    public JsonNode buildResponsePayloadJson(ResponseEntity<?> response, int attempt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (FormatValidator.hasValue(response)) {
            payload.put("status", response.getStatusCode().value());
            Object body = response.getBody();
            if (FormatValidator.hasValue(body)) {
                payload.put("body", body);
            }
        }
        payload.put("attempt", attempt);
        return buildPayloadJson(payload);
    }

    public JsonNode buildErrorPayloadJson(String error, String message, int attempt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (FormatValidator.hasValue(error)) {
            payload.put("error", error);
        }
        if (FormatValidator.hasValue(message)) {
            payload.put("message", message);
        }
        payload.put("attempt", attempt);
        return buildPayloadJson(payload);
    }
}
