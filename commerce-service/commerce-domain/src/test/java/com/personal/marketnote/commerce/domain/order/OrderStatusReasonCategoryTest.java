package com.personal.marketnote.commerce.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusReasonCategoryTest {

    // ==================================================================================
    // 취소 전용 사유
    // ==================================================================================

    @Nested
    @DisplayName("취소 전용 사유")
    class CancelOnlyReasonTest {

        @Test
        @DisplayName("CANCEL_ORDER는 취소 사유이다")
        void cancelOrder_isCancelReason() {
            assertThat(OrderStatusReasonCategory.CANCEL_ORDER.isCancelReason()).isTrue();
        }

        @Test
        @DisplayName("CANCEL_ORDER는 반품 사유가 아니다")
        void cancelOrder_isNotReturnReason() {
            assertThat(OrderStatusReasonCategory.CANCEL_ORDER.isReturnReason()).isFalse();
        }

        @Test
        @DisplayName("CHANGE_OPTION은 취소 사유이다")
        void changeOption_isCancelReason() {
            assertThat(OrderStatusReasonCategory.CHANGE_OPTION.isCancelReason()).isTrue();
        }

        @Test
        @DisplayName("CHANGE_OPTION은 반품 사유가 아니다")
        void changeOption_isNotReturnReason() {
            assertThat(OrderStatusReasonCategory.CHANGE_OPTION.isReturnReason()).isFalse();
        }
    }

    // ==================================================================================
    // 반품 전용 사유
    // ==================================================================================

    @Nested
    @DisplayName("반품 전용 사유")
    class ReturnOnlyReasonTest {

        @Test
        @DisplayName("SIMPLE_CHANGE_OF_MIND는 반품 사유이다")
        void simpleChangeOfMind_isReturnReason() {
            assertThat(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND.isReturnReason()).isTrue();
        }

        @Test
        @DisplayName("SIMPLE_CHANGE_OF_MIND는 취소 사유가 아니다")
        void simpleChangeOfMind_isNotCancelReason() {
            assertThat(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND.isCancelReason()).isFalse();
        }

        @Test
        @DisplayName("PRODUCT_DAMAGE는 반품 사유이다")
        void productDamage_isReturnReason() {
            assertThat(OrderStatusReasonCategory.PRODUCT_DAMAGE.isReturnReason()).isTrue();
        }

        @Test
        @DisplayName("PRODUCT_DAMAGE는 취소 사유가 아니다")
        void productDamage_isNotCancelReason() {
            assertThat(OrderStatusReasonCategory.PRODUCT_DAMAGE.isCancelReason()).isFalse();
        }

        @Test
        @DisplayName("PRODUCT_MISMATCH는 반품 사유이다")
        void productMismatch_isReturnReason() {
            assertThat(OrderStatusReasonCategory.PRODUCT_MISMATCH.isReturnReason()).isTrue();
        }

        @Test
        @DisplayName("WRONG_DELIVERY는 반품 사유이다")
        void wrongDelivery_isReturnReason() {
            assertThat(OrderStatusReasonCategory.WRONG_DELIVERY.isReturnReason()).isTrue();
        }

        @Test
        @DisplayName("MISSING_COMPONENTS는 반품 사유이다")
        void missingComponents_isReturnReason() {
            assertThat(OrderStatusReasonCategory.MISSING_COMPONENTS.isReturnReason()).isTrue();
        }
    }

    // ==================================================================================
    // 취소+반품 공용 사유
    // ==================================================================================

    @Nested
    @DisplayName("취소+반품 공용 사유")
    class BothReasonTest {

        @Test
        @DisplayName("MISTAKE는 취소 사유이다")
        void mistake_isCancelReason() {
            assertThat(OrderStatusReasonCategory.MISTAKE.isCancelReason()).isTrue();
        }

        @Test
        @DisplayName("MISTAKE는 반품 사유이다")
        void mistake_isReturnReason() {
            assertThat(OrderStatusReasonCategory.MISTAKE.isReturnReason()).isTrue();
        }

        @Test
        @DisplayName("ETC는 취소 사유이다")
        void etc_isCancelReason() {
            assertThat(OrderStatusReasonCategory.ETC.isCancelReason()).isTrue();
        }

        @Test
        @DisplayName("ETC는 반품 사유이다")
        void etc_isReturnReason() {
            assertThat(OrderStatusReasonCategory.ETC.isReturnReason()).isTrue();
        }
    }
}
