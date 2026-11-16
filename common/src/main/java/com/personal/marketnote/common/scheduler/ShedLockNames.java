package com.personal.marketnote.common.scheduler;

/**
 * ShedLock 락 이름 상수 모음.
 *
 * <p>네이밍 규칙:
 * <ul>
 *   <li><b>common 모듈 잡</b> (OUTBOX_*, SAGA_*): 서비스 prefix를 포함하지 않음. 사용처에서
 *       SpEL {@code "${spring.application.name}-" + ShedLockNames.OUTBOX_PUBLISH} 형태로
 *       조합하여 서비스별 독립 락을 형성한다. (common이 9개 서비스에 임포트되므로
 *       서비스별로 독립 실행되어야 함 — outbox/saga는 서비스별 독립 도메인)</li>
 *   <li><b>서비스별 잡</b> (COMMERCE_*, NOTIFICATION_*, REWARD_*, FULFILLMENT_*, PRODUCT_*, USER_*):
 *       상수 자체에 서비스 prefix가 이미 포함되어 있어 SpEL 결합 불필요.
 *       해당 서비스 어댑터 모듈에서만 사용된다.</li>
 * </ul>
 */
public final class ShedLockNames {

    private ShedLockNames() {
    }

    public static final String OUTBOX_PUBLISH = "outbox-publish";
    public static final String OUTBOX_CLEANUP_PUBLISHED = "outbox-cleanup-published";
    public static final String OUTBOX_CLEANUP_DISCARDED = "outbox-cleanup-discarded";
    public static final String SAGA_TIMEOUT_CHECK = "saga-timeout-check";

    public static final String COMMERCE_SETTLEMENT = "commerce-settlement";
    public static final String COMMERCE_AUTO_CONFIRM_DELIVERED_ORDERS = "commerce-auto-confirm-delivered-orders";
    public static final String COMMERCE_PAYMENT_CANCEL_IDEMPOTENCY_CLEANUP = "commerce-payment-cancel-idempotency-cleanup";
    public static final String COMMERCE_RETURN_INSPECTION_POLLING = "commerce-return-inspection-polling";
    public static final String COMMERCE_INVENTORY_RESERVATION_TIMEOUT = "commerce-inventory-reservation-timeout";

    public static final String NOTIFICATION_EXPIRED_CLEANUP = "notification-expired-cleanup";
    public static final String NOTIFICATION_SCHEDULED_PUBLISH = "notification-scheduled-publish";
    public static final String NOTIFICATION_ALIGO_POLLING = "notification-aligo-polling";
    public static final String NOTIFICATION_STALE_DEVICE_TOKEN_CLEANUP = "notification-stale-device-token-cleanup";
    public static final String NOTIFICATION_CONSENT_REMINDER = "notification-consent-reminder";

    public static final String REWARD_GIFTICON_SYNC = "reward-gifticon-sync";
    public static final String REWARD_GIFTICON_COUPON_SYNC = "reward-gifticon-coupon-sync";

    public static final String FULFILLMENT_STOCK_SYNC = "fulfillment-stock-sync";
    public static final String FULFILLMENT_DELIVERY_STATUS_POLLING = "fulfillment-delivery-status-polling";

    public static final String PRODUCT_POPULARITY = "product-popularity";

    public static final String USER_ACTIVATE_EXPIRED_DEACTIVATION = "user-activate-expired-deactivation";
}
