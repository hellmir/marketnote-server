package com.personal.marketnote.common.kafka;

import java.util.List;
import java.util.Set;

public final class DltTopicRegistry {

    private static final Set<String> ALLOWED_TOPICS = Set.of(
            KafkaTopicConstants.ORDER_PAYMENT_COMPLETED,
            KafkaTopicConstants.PAYMENT_APPROVED,
            KafkaTopicConstants.PAYMENT_FAILED,
            KafkaTopicConstants.PAYMENT_CANCELLED,
            KafkaTopicConstants.SETTLEMENT_EXECUTED,
            KafkaTopicConstants.ORDER_PURCHASE_CONFIRMED,
            KafkaTopicConstants.PRODUCT_REGISTERED,
            KafkaTopicConstants.PRICE_POLICY_CREATED,
            KafkaTopicConstants.PRODUCT_UPDATED,
            KafkaTopicConstants.USER_SIGNUP_COMPLETED,
            KafkaTopicConstants.USER_REFERRAL_COMPLETED,
            KafkaTopicConstants.REVIEW_REGISTERED,
            KafkaTopicConstants.ORDER_RETURNED
    );

    private DltTopicRegistry() {
    }

    public static boolean isAllowed(String topic) {
        return ALLOWED_TOPICS.contains(topic);
    }

    public static String toDltTopic(String originalTopic) {
        return originalTopic + KafkaTopicConstants.DLT_SUFFIX;
    }

    public static List<String> getAllOriginalTopics() {
        return List.copyOf(ALLOWED_TOPICS);
    }

    public static List<String> getAllDltTopics() {
        return ALLOWED_TOPICS.stream()
                .map(DltTopicRegistry::toDltTopic)
                .toList();
    }
}
