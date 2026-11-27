package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.GiftishowOperationMappingException;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationTargetType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * GiftishowApiClient의 operation 문자열을 {@link RewardVendorCommunicationTargetType}으로 매핑한다.
 *
 * <p>operation은 컴파일타임 상수만 허용한다 (외부 응답 raw 주입 금지). 매핑 누락 시 fail-open으로 기본값 반환 +
 * ERROR 로그 — 쿠폰 발송/조회 등 사용자 체감 동작이 매핑 누락으로 차단되지 않도록 한다.
 *
 * <p>{@code @PostConstruct} 검증으로 알려진 operation 10종이 모두 매핑되어 있는지 빈 초기화 시점에 검사한다.
 */
@Component
@Slf4j
public class GiftishowOperationTargetTypeResolver {

    static final String OP_PRODUCT_LIST = "product.list";
    static final String OP_PRODUCT_DETAIL = "product.detail";
    static final String OP_BRAND_LIST = "brand.list";
    static final String OP_BRAND_DETAIL = "brand.detail";
    static final String OP_COUPON_SEND = "coupon.send";
    static final String OP_COUPON_DETAIL = "coupon.detail";
    static final String OP_COUPON_CANCEL = "coupon.cancel";
    static final String OP_COUPON_RESEND = "coupon.resend";
    static final String OP_COUPON_SEND_FAIL_CANCEL = "coupon.sendFailCancel";
    static final String OP_BIZMONEY_BALANCE = "bizmoney.balance";

    private static final Set<String> KNOWN_OPERATIONS = Set.of(
        OP_PRODUCT_LIST,
        OP_PRODUCT_DETAIL,
        OP_BRAND_LIST,
        OP_BRAND_DETAIL,
        OP_COUPON_SEND,
        OP_COUPON_DETAIL,
        OP_COUPON_CANCEL,
        OP_COUPON_RESEND,
        OP_COUPON_SEND_FAIL_CANCEL,
        OP_BIZMONEY_BALANCE
    );

    private static final Map<String, RewardVendorCommunicationTargetType> OPERATION_TARGET_TYPE_MAP = Map.of(
        OP_PRODUCT_LIST, RewardVendorCommunicationTargetType.COUPON,
        OP_PRODUCT_DETAIL, RewardVendorCommunicationTargetType.COUPON,
        OP_BRAND_LIST, RewardVendorCommunicationTargetType.COUPON,
        OP_BRAND_DETAIL, RewardVendorCommunicationTargetType.COUPON,
        OP_COUPON_SEND, RewardVendorCommunicationTargetType.COUPON,
        OP_COUPON_DETAIL, RewardVendorCommunicationTargetType.COUPON,
        OP_COUPON_CANCEL, RewardVendorCommunicationTargetType.COUPON,
        OP_COUPON_RESEND, RewardVendorCommunicationTargetType.COUPON,
        OP_COUPON_SEND_FAIL_CANCEL, RewardVendorCommunicationTargetType.COUPON,
        OP_BIZMONEY_BALANCE, RewardVendorCommunicationTargetType.BIZMONEY
    );

    private static final RewardVendorCommunicationTargetType DEFAULT_TARGET_TYPE =
        RewardVendorCommunicationTargetType.COUPON;

    /**
     * operation에 대응되는 targetType을 반환한다. 매핑 누락 시 ERROR 로그를 남기고 기본값을 반환한다 (fail-open).
     */
    public RewardVendorCommunicationTargetType resolve(String operation) {
        RewardVendorCommunicationTargetType resolved = OPERATION_TARGET_TYPE_MAP.get(operation);
        if (resolved == null) {
            log.error(
                "Giftishow operation 매핑 누락: operation={} — 기본값 {}으로 fail-open 처리",
                operation, DEFAULT_TARGET_TYPE
            );
            return DEFAULT_TARGET_TYPE;
        }
        return resolved;
    }

    /**
     * 빈 초기화 시점에 알려진 operation 10종이 모두 매핑되어 있는지 검증한다.
     */
    @PostConstruct
    void validateMappingCompleteness() {
        for (String operation : KNOWN_OPERATIONS) {
            if (!OPERATION_TARGET_TYPE_MAP.containsKey(operation)) {
                throw new GiftishowOperationMappingException(
                    "Giftishow operation 매핑 누락: operation=" + operation
                );
            }
        }
    }
}
