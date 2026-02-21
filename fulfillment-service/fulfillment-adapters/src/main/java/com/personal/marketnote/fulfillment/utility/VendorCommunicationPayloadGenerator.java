package com.personal.marketnote.fulfillment.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VendorCommunicationPayloadGenerator {
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

    public JsonNode buildErrorPayloadJson(String error, String message) {
        ObjectNode node = objectMapper.createObjectNode();
        if (FormatValidator.hasValue(error)) {
            node.put("error", error);
        }
        if (FormatValidator.hasValue(message)) {
            node.put("message", message);
        }

        return node;
    }
}
