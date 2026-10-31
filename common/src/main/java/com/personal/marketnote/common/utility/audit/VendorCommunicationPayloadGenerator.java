package com.personal.marketnote.common.utility.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 벤더 통신 페이로드를 {@link JsonNode}로 빌드하는 공통 유틸리티.
 *
 * <p>에러 페이로드(error/message)를 공통 포맷으로 조립한다. 벤더별 고유 페이로드(예: 광고 네트워크,
 * 배송 벤더 등)는 각 서비스 어댑터에서 별도 Generator로 처리한다.</p>
 */
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
