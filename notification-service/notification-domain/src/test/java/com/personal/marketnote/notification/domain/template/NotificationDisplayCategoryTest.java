package com.personal.marketnote.notification.domain.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationDisplayCategoryTest {

    @Nested
    @DisplayName("ORDER_SHIPPING")
    class OrderShipping {

        @Test
        @DisplayName("주문/배송 관련 NotificationType을 포함한다")
        void containsOrderShippingTypes() {
            NotificationDisplayCategory category = NotificationDisplayCategory.ORDER_SHIPPING;

            assertThat(category.contains(NotificationType.ORDER_PAYMENT_COMPLETED)).isTrue();
            assertThat(category.contains(NotificationType.SHIPPING_STARTED)).isTrue();
            assertThat(category.contains(NotificationType.SHIPPING_COMPLETED)).isTrue();
            assertThat(category.contains(NotificationType.SHIPPING_DELAYED)).isTrue();
            assertThat(category.contains(NotificationType.ORDER_CANCELLED)).isTrue();
            assertThat(category.contains(NotificationType.ORDER_CANCEL_FAILED)).isTrue();
            assertThat(category.contains(NotificationType.RETURN_REQUESTED)).isTrue();
            assertThat(category.contains(NotificationType.RETURN_COMPLETED)).isTrue();
            assertThat(category.contains(NotificationType.RETURN_REJECTED)).isTrue();
            assertThat(category.contains(NotificationType.PURCHASE_CONFIRMATION_REQUEST)).isTrue();
            assertThat(category.contains(NotificationType.PURCHASE_CONFIRMATION_COMPLETED)).isTrue();
        }

        @Test
        @DisplayName("공지/고객센터 타입은 포함하지 않는다")
        void excludesOtherCategoryTypes() {
            NotificationDisplayCategory category = NotificationDisplayCategory.ORDER_SHIPPING;

            assertThat(category.contains(NotificationType.NOTICE)).isFalse();
            assertThat(category.contains(NotificationType.EVENT)).isFalse();
            assertThat(category.contains(NotificationType.REVIEW_WRITE_REQUEST)).isFalse();
            assertThat(category.contains(NotificationType.REVIEW_REPLY)).isFalse();
            assertThat(category.contains(NotificationType.ONE_ON_ONE_INQUIRY_REPLY)).isFalse();
            assertThat(category.contains(NotificationType.PRODUCT_INQUIRY_REPLY)).isFalse();
        }
    }

    @Nested
    @DisplayName("NOTICE_EVENT")
    class NoticeEvent {

        @Test
        @DisplayName("공지/이벤트 NotificationType을 포함한다")
        void containsNoticeEventTypes() {
            NotificationDisplayCategory category = NotificationDisplayCategory.NOTICE_EVENT;

            assertThat(category.contains(NotificationType.NOTICE)).isTrue();
            assertThat(category.contains(NotificationType.EVENT)).isTrue();
        }

        @Test
        @DisplayName("주문/고객센터 타입은 포함하지 않는다")
        void excludesOtherCategoryTypes() {
            NotificationDisplayCategory category = NotificationDisplayCategory.NOTICE_EVENT;

            assertThat(category.contains(NotificationType.ORDER_PAYMENT_COMPLETED)).isFalse();
            assertThat(category.contains(NotificationType.SHIPPING_STARTED)).isFalse();
            assertThat(category.contains(NotificationType.ONE_ON_ONE_INQUIRY_REPLY)).isFalse();
        }
    }

    @Nested
    @DisplayName("CUSTOMER_SERVICE")
    class CustomerService {

        @Test
        @DisplayName("고객센터 관련 NotificationType을 포함한다")
        void containsCustomerServiceTypes() {
            NotificationDisplayCategory category = NotificationDisplayCategory.CUSTOMER_SERVICE;

            assertThat(category.contains(NotificationType.ONE_ON_ONE_INQUIRY_REPLY)).isTrue();
            assertThat(category.contains(NotificationType.PRODUCT_INQUIRY_REPLY)).isTrue();
            assertThat(category.contains(NotificationType.REVIEW_WRITE_REQUEST)).isTrue();
            assertThat(category.contains(NotificationType.REVIEW_REPLY)).isTrue();
        }

        @Test
        @DisplayName("주문/공지 타입은 포함하지 않는다")
        void excludesOtherCategoryTypes() {
            NotificationDisplayCategory category = NotificationDisplayCategory.CUSTOMER_SERVICE;

            assertThat(category.contains(NotificationType.ORDER_PAYMENT_COMPLETED)).isFalse();
            assertThat(category.contains(NotificationType.NOTICE)).isFalse();
            assertThat(category.contains(NotificationType.EVENT)).isFalse();
        }
    }

    @Nested
    @DisplayName("types")
    class Types {

        @Test
        @DisplayName("ORDER_SHIPPING의 타입 집합은 11개 NotificationType을 포함한다")
        void orderShippingTypesSize() {
            Set<NotificationType> types = NotificationDisplayCategory.ORDER_SHIPPING.types();

            assertThat(types).hasSize(11);
        }

        @Test
        @DisplayName("NOTICE_EVENT의 타입 집합은 2개 NotificationType을 포함한다")
        void noticeEventTypesSize() {
            Set<NotificationType> types = NotificationDisplayCategory.NOTICE_EVENT.types();

            assertThat(types).hasSize(2);
        }

        @Test
        @DisplayName("CUSTOMER_SERVICE의 타입 집합은 4개 NotificationType을 포함한다")
        void customerServiceTypesSize() {
            Set<NotificationType> types = NotificationDisplayCategory.CUSTOMER_SERVICE.types();

            assertThat(types).hasSize(4);
        }

        @Test
        @DisplayName("반환된 Set은 불변이다")
        void typesReturnImmutableSet() {
            Set<NotificationType> types = NotificationDisplayCategory.ORDER_SHIPPING.types();

            assertThatThrownBy(() -> types.add(NotificationType.NOTICE))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
