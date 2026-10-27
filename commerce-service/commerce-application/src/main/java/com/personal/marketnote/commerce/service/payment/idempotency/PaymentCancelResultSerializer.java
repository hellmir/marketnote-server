package com.personal.marketnote.commerce.service.payment.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.service.payment.PaymentCancelContext;
import com.personal.marketnote.common.utility.FormatValidator;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
 *
 * <p><b>직렬화 실패 정책:</b>
 * 직렬화 실패 시 {@code "{}"} fallback 을 반환한다 — 비즈니스 TX 는 이미 커밋되었고, 여기서 예외를 재던지면
 * 멱등 레코드가 IN_PROGRESS 로 남아 재요청이 409 로 실패한다. 따라서 멱등성 완료 전환을 보장하기 위해
 * 예외를 삼키고 빈 객체를 반환한다.
 *
 * <p>실패는 {@code payment.cancel.idempotency.serializer.failure} 카운터로 관찰되어 재발 감지가 가능하다.
 * 구체적인 실패 컨텍스트(orderKey 등)는 로그로 남긴다.
 *
 * <p><b>카운터 태그 정책:</b> orderKey 등 고카디널리티 식별자를 태그로 부착하지 않는다 — Prometheus
 * 시계열 폭발을 방지하기 위함. 개별 실패 식별은 로그로만 수행한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCancelResultSerializer {

    private static final String SERIALIZER_FAILURE_METRIC = "payment.cancel.idempotency.serializer.failure";

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

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
            incrementSerializerFailureCounter();
            return "{}";
        }
    }

    private void incrementSerializerFailureCounter() {
        if (FormatValidator.hasNoValue(meterRegistry)) {
            return;
        }
        Counter.builder(SERIALIZER_FAILURE_METRIC)
                .description("결제 취소 result 직렬화 실패 건수")
                .register(meterRegistry)
                .increment();
    }
}
