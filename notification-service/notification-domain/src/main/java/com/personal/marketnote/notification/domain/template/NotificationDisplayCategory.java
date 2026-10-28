package com.personal.marketnote.notification.domain.template;

import java.util.Set;

public enum NotificationDisplayCategory {

    ORDER_SHIPPING(
            "주문/배송",
            Set.of(
                    NotificationType.ORDER_PAYMENT_COMPLETED,
                    NotificationType.SHIPPING_STARTED,
                    NotificationType.SHIPPING_COMPLETED,
                    NotificationType.SHIPPING_DELAYED,
                    NotificationType.ORDER_CANCELLED,
                    NotificationType.ORDER_CANCEL_FAILED,
                    NotificationType.RETURN_REQUESTED,
                    NotificationType.RETURN_COMPLETED,
                    NotificationType.RETURN_REJECTED,
                    NotificationType.PURCHASE_CONFIRMATION_REQUEST,
                    NotificationType.PURCHASE_CONFIRMATION_COMPLETED
            )
    ),
    NOTICE_EVENT(
            "공지/이벤트",
            Set.of(
                    NotificationType.NOTICE,
                    NotificationType.EVENT
            )
    ),
    CUSTOMER_SERVICE(
            "고객센터",
            Set.of(
                    NotificationType.ONE_ON_ONE_INQUIRY_REPLY,
                    NotificationType.PRODUCT_INQUIRY_REPLY,
                    NotificationType.REVIEW_WRITE_REQUEST,
                    NotificationType.REVIEW_REPLY
            )
    );

    private final String description;
    private final Set<NotificationType> types;

    NotificationDisplayCategory(String description, Set<NotificationType> types) {
        this.description = description;
        this.types = types;
    }

    public boolean contains(NotificationType type) {
        return types.contains(type);
    }

    public Set<NotificationType> types() {
        return types;
    }

    public String getDescription() {
        return description;
    }
}
