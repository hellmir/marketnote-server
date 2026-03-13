package com.personal.marketnote.common.kafka;

public final class KafkaTopicConstants {

    private KafkaTopicConstants() {
    }

    // Commerce 이벤트
    public static final String ORDER_PAYMENT_COMPLETED = "commerce.order.payment-completed";
    public static final String PAYMENT_APPROVED = "commerce.payment.approved";
    public static final String PAYMENT_FAILED = "commerce.payment.failed";
    public static final String PAYMENT_CANCELLED = "commerce.payment.cancelled";
    public static final String SETTLEMENT_EXECUTED = "commerce.settlement.executed";
    public static final String ORDER_PURCHASE_CONFIRMED = "commerce.order.purchase-confirmed";

    // Product 이벤트
    public static final String PRODUCT_REGISTERED = "product.product.registered";
    public static final String PRICE_POLICY_CREATED = "product.price-policy.created";
    public static final String PRODUCT_UPDATED = "product.product.updated";

    // User 이벤트
    public static final String USER_SIGNUP_COMPLETED = "user.user.signup-completed";
    public static final String USER_REFERRAL_COMPLETED = "user.user.referral-completed";

    // Community 이벤트
    public static final String REVIEW_REGISTERED = "community.review.registered";

    // Dead Letter Topic 접미사
    public static final String DLT_SUFFIX = ".dlt";
}
