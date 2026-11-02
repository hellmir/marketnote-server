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

    // ==================================================================================
    // 귀책 사유 구분
    // ==================================================================================

    @Nested
    @DisplayName("고객 귀책 사유")
    class BuyerFaultTest {

        @Test
        @DisplayName("CANCEL_ORDER는 고객 귀책이다")
        void cancelOrder_isBuyerFault() {
            assertThat(OrderStatusReasonCategory.CANCEL_ORDER.isBuyerFault()).isTrue();
            assertThat(OrderStatusReasonCategory.CANCEL_ORDER.isSellerFault()).isFalse();
        }

        @Test
        @DisplayName("CHANGE_OPTION은 고객 귀책이다")
        void changeOption_isBuyerFault() {
            assertThat(OrderStatusReasonCategory.CHANGE_OPTION.isBuyerFault()).isTrue();
            assertThat(OrderStatusReasonCategory.CHANGE_OPTION.isSellerFault()).isFalse();
        }

        @Test
        @DisplayName("MISTAKE는 고객 귀책이다")
        void mistake_isBuyerFault() {
            assertThat(OrderStatusReasonCategory.MISTAKE.isBuyerFault()).isTrue();
            assertThat(OrderStatusReasonCategory.MISTAKE.isSellerFault()).isFalse();
        }

        @Test
        @DisplayName("ETC는 고객 귀책이다")
        void etc_isBuyerFault() {
            assertThat(OrderStatusReasonCategory.ETC.isBuyerFault()).isTrue();
            assertThat(OrderStatusReasonCategory.ETC.isSellerFault()).isFalse();
        }

        @Test
        @DisplayName("SIMPLE_CHANGE_OF_MIND는 고객 귀책이다")
        void simpleChangeOfMind_isBuyerFault() {
            assertThat(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND.isBuyerFault()).isTrue();
            assertThat(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND.isSellerFault()).isFalse();
        }
    }

    @Nested
    @DisplayName("판매자 귀책 사유")
    class SellerFaultTest {

        @Test
        @DisplayName("PRODUCT_DAMAGE는 판매자 귀책이다")
        void productDamage_isSellerFault() {
            assertThat(OrderStatusReasonCategory.PRODUCT_DAMAGE.isSellerFault()).isTrue();
            assertThat(OrderStatusReasonCategory.PRODUCT_DAMAGE.isBuyerFault()).isFalse();
        }

        @Test
        @DisplayName("PRODUCT_MISMATCH는 판매자 귀책이다")
        void productMismatch_isSellerFault() {
            assertThat(OrderStatusReasonCategory.PRODUCT_MISMATCH.isSellerFault()).isTrue();
            assertThat(OrderStatusReasonCategory.PRODUCT_MISMATCH.isBuyerFault()).isFalse();
        }

        @Test
        @DisplayName("WRONG_DELIVERY는 판매자 귀책이다")
        void wrongDelivery_isSellerFault() {
            assertThat(OrderStatusReasonCategory.WRONG_DELIVERY.isSellerFault()).isTrue();
            assertThat(OrderStatusReasonCategory.WRONG_DELIVERY.isBuyerFault()).isFalse();
        }

        @Test
        @DisplayName("MISSING_COMPONENTS는 판매자 귀책이다")
        void missingComponents_isSellerFault() {
            assertThat(OrderStatusReasonCategory.MISSING_COMPONENTS.isSellerFault()).isTrue();
            assertThat(OrderStatusReasonCategory.MISSING_COMPONENTS.isBuyerFault()).isFalse();
        }
    }
}
