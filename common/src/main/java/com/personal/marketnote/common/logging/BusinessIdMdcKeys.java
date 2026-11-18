package com.personal.marketnote.common.logging;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 비즈니스 ID MDC enrich 화이트리스트 상수 모음.
 *
 * <p>{@link BusinessIdMdcInterceptor}가 MDC 에 주입할 수 있는 키를 제한하여
 * 무분별한 path variable 키 노출을 차단한다.
 */
public final class BusinessIdMdcKeys {

    public static final String USER_ID = "userId";
    public static final String ORDER_ID = "orderId";
    public static final String PAYMENT_KEY = "paymentKey";
    public static final String SELLER_ID = "sellerId";
    public static final String PRODUCT_ID = "productId";
    public static final String EVENT_ID = "eventId";

    public static final Set<String> WHITELIST = Set.of(
            USER_ID, ORDER_ID, PAYMENT_KEY, SELLER_ID, PRODUCT_ID, EVENT_ID
    );

    private static final Pattern CRLF_PATTERN = Pattern.compile("[\\r\\n\\t\\u0000-\\u001F\\u007F]");

    private BusinessIdMdcKeys() {
    }

    /**
     * MDC 에 저장될 비즈니스 ID 값에서 CRLF/제어 문자를 {@code _} 로 치환한다.
     * 로그 인젝션(header splitting, log forging)을 방어한다.
     */
    public static String sanitize(String value) {
        return CRLF_PATTERN.matcher(value).replaceAll("_");
    }
}
