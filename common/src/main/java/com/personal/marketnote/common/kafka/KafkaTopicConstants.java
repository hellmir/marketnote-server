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
    public static final String SHIPPING_ADDRESS_CHANGED = "user.shipping-address.changed";

    // Community 이벤트
    public static final String REVIEW_REGISTERED = "community.review.registered";

    // File 이벤트
    public static final String FILE_IMAGE_CHANGED = "file.image.changed";

    // SAGA 응답
    public static final String SAGA_RESPONSE = "saga.response";

    // OrderPayment SAGA 스텝
    public static final String SAGA_ORDER_PAYMENT_INVENTORY = "saga.order-payment.inventory";
    public static final String SAGA_ORDER_PAYMENT_LEDGER = "saga.order-payment.ledger";
    public static final String SAGA_ORDER_PAYMENT_COMPLETED = "saga.order-payment.completed";

    // Inventory 이벤트
    public static final String INVENTORY_CHANGED = "commerce.inventory.changed";

    // Shipping Policy 이벤트
    public static final String SHIPPING_POLICY_CHANGED = "product.shipping-policy.changed";

    // Dead Letter Topic 접미사
    public static final String DLT_SUFFIX = ".dlt";
}
