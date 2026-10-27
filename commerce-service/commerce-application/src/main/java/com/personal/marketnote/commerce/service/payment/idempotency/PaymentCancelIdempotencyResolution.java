package com.personal.marketnote.commerce.service.payment.idempotency;

/**
 * 멱등성 해석 결과.
 *
 * <ul>
 *   <li>{@link Signal#NEW}: 최초 요청 — 정상 플로우 진행</li>
 *   <li>{@link Signal#RETRY}: 실패 레코드 retry — 정상 플로우 진행</li>
 *   <li>{@link Signal#CACHED}: 완료 레코드 — {@link #cachedResult()} 로 응답</li>
 * </ul>
 *
 * @param signal        해석된 흐름 시그널
 * @param cachedResult  완료 캐시 결과 JSON (CACHED 만 유효)
 */
public record PaymentCancelIdempotencyResolution(Signal signal, String cachedResult) {
    public enum Signal {
        NEW, RETRY, CACHED;

        public boolean isCached() {
            return this == CACHED;
        }
    }

    public static PaymentCancelIdempotencyResolution newlyCreated() {
        return new PaymentCancelIdempotencyResolution(Signal.NEW, null);
    }

    public static PaymentCancelIdempotencyResolution retried() {
        return new PaymentCancelIdempotencyResolution(Signal.RETRY, null);
    }

    public static PaymentCancelIdempotencyResolution cached(String resultJson) {
        return new PaymentCancelIdempotencyResolution(Signal.CACHED, resultJson);
    }
}
