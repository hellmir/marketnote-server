package com.personal.marketnote.commerce.service.payment.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.service.payment.PaymentCancelContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 결제 취소 완료 결과를 멱등성 레코드에 저장할 JSON 으로 직렬화한다.
 *
 * <p>result 컬럼은 JSONB 이며, 현재는 최소 필드(orderKey, cancelAmount, isFullCancel, completedAt)만 저장한다.
 * 추후 운영 요구가 생기면 필드를 확장한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCancelResultSerializer {

    private final ObjectMapper objectMapper;

    public String serialize(PaymentCancelContext context) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderKey", context.originalCommand().orderKey());
        payload.put("cancelAmount", context.adjustedCancelAmount());
        payload.put("originalCancelAmount", context.cancelAmount());
        payload.put("isFullCancel", context.isFullCancel());
        payload.put("shippingFeeDeduction", context.shippingFeeDeduction());
        payload.put("cancelId", context.cancelId());
        payload.put("completedAt", LocalDateTime.now().toString());

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("결제 취소 멱등성 result 직렬화 실패 - orderKey: {}",
                    context.originalCommand().orderKey(), e);
            return "{}";
        }
    }
}
