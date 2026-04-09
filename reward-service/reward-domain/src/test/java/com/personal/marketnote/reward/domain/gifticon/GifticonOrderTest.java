package com.personal.marketnote.reward.domain.gifticon;

import com.personal.marketnote.reward.domain.exception.InvalidGifticonOrderStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GifticonOrderTest {

    private GifticonOrder createPendingOrder() {
        return GifticonOrder.from(GifticonOrderSnapshotState.builder()
                .id(1L)
                .userId(100L)
                .goodsCode("G001")
                .goodsName("스타벅스 아메리카노")
                .brandName("스타벅스")
                .productImageUrl("https://example.com/image.jpg")
                .trId("NTCASH_100_20260403120000")
                .orderNo("ORD001")
                .cashPrice(4500L)
                .orderStatus(GifticonOrderStatus.PENDING)
                .couponImageUrl(null)
                .pinNo(null)
                .validEndDate(LocalDate.of(2026, 7, 3))
                .createdAt(LocalDateTime.of(2026, 4, 3, 12, 0, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 3, 12, 0, 0))
                .build());
    }

    private GifticonOrder createIssuedOrder() {
        return GifticonOrder.from(GifticonOrderSnapshotState.builder()
                .id(1L)
                .userId(100L)
                .goodsCode("G001")
                .goodsName("스타벅스 아메리카노")
                .brandName("스타벅스")
                .productImageUrl("https://example.com/image.jpg")
                .trId("NTCASH_100_20260403120000")
                .orderNo("ORD001")
                .cashPrice(4500L)
                .orderStatus(GifticonOrderStatus.ISSUED)
                .couponImageUrl("https://example.com/coupon.jpg")
                .pinNo("encrypted_pin")
                .validEndDate(LocalDate.of(2026, 7, 3))
                .createdAt(LocalDateTime.of(2026, 4, 3, 12, 0, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 3, 12, 0, 0))
                .build());
    }

    @Nested
    @DisplayName("SnapshotState로 복원")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState로 GifticonOrder를 복원한다")
        void restoreFromSnapshotState() {
            GifticonOrder order = createPendingOrder();

            assertThat(order.getId()).isEqualTo(1L);
            assertThat(order.getUserId()).isEqualTo(100L);
            assertThat(order.getGoodsCode()).isEqualTo("G001");
            assertThat(order.getGoodsName()).isEqualTo("스타벅스 아메리카노");
            assertThat(order.getBrandName()).isEqualTo("스타벅스");
            assertThat(order.getTrId()).isEqualTo("NTCASH_100_20260403120000");
            assertThat(order.getCashPrice()).isEqualTo(4500L);
            assertThat(order.getOrderStatus()).isEqualTo(GifticonOrderStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("상태 전이")
    class StatusTransition {

        @Test
        @DisplayName("PENDING → ISSUED 전이에 성공한다")
        void pendingToIssued() {
            GifticonOrder order = createPendingOrder();

            order.issue("https://example.com/coupon.jpg", "encrypted_pin", "ORD001", LocalDate.of(2026, 7, 3));

            assertThat(order.getOrderStatus()).isEqualTo(GifticonOrderStatus.ISSUED);
            assertThat(order.getCouponImageUrl()).isEqualTo("https://example.com/coupon.jpg");
            assertThat(order.getPinNo()).isEqualTo("encrypted_pin");
            assertThat(order.getOrderNo()).isEqualTo("ORD001");
            assertThat(order.getValidEndDate()).isEqualTo(LocalDate.of(2026, 7, 3));
        }

        @Test
        @DisplayName("ISSUED 상태에서 issue 호출 시 예외가 발생한다")
        void issuedToIssuedThrowsException() {
            GifticonOrder order = createIssuedOrder();

            assertThatThrownBy(() -> order.issue("url", "pin", "ORD002", LocalDate.of(2026, 7, 3)))
                    .isInstanceOf(InvalidGifticonOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("PENDING → SEND_FAILED 전이에 성공한다")
        void pendingToSendFailed() {
            GifticonOrder order = createPendingOrder();

            order.markSendFailed();

            assertThat(order.getOrderStatus()).isEqualTo(GifticonOrderStatus.SEND_FAILED);
        }

        @Test
        @DisplayName("ISSUED 상태에서 markSendFailed 호출 시 예외가 발생한다")
        void issuedToSendFailedThrowsException() {
            GifticonOrder order = createIssuedOrder();

            assertThatThrownBy(() -> order.markSendFailed())
                    .isInstanceOf(InvalidGifticonOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("PENDING → CANCELLED 전이에 성공한다")
        void pendingToCancelled() {
            GifticonOrder order = createPendingOrder();

            order.cancel();

            assertThat(order.getOrderStatus()).isEqualTo(GifticonOrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("ISSUED → CANCELLED 전이에 성공한다")
        void issuedToCancelled() {
            GifticonOrder order = createIssuedOrder();

            order.cancel();

            assertThat(order.getOrderStatus()).isEqualTo(GifticonOrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("SEND_FAILED → CANCELLED 전이에 성공한다")
        void sendFailedToCancelled() {
            GifticonOrder order = createPendingOrder();
            order.markSendFailed();

            order.cancel();

            assertThat(order.getOrderStatus()).isEqualTo(GifticonOrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("USED 상태에서 cancel 호출 시 예외가 발생한다")
        void usedToCancelledThrowsException() {
            GifticonOrder order = createIssuedOrder();
            order.syncStatus(GifticonOrderStatus.USED);

            assertThatThrownBy(() -> order.cancel())
                    .isInstanceOf(InvalidGifticonOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("syncStatus로 외부 동기화 상태를 반영한다")
        void syncStatusUpdates() {
            GifticonOrder order = createIssuedOrder();

            order.syncStatus(GifticonOrderStatus.USED);

            assertThat(order.getOrderStatus()).isEqualTo(GifticonOrderStatus.USED);
        }

        @Test
        @DisplayName("syncStatus로 동일 상태 반영 시 상태가 유지된다")
        void syncStatusSameStatusIsIdempotent() {
            GifticonOrder order = createIssuedOrder();

            order.syncStatus(GifticonOrderStatus.ISSUED);

            assertThat(order.getOrderStatus()).isEqualTo(GifticonOrderStatus.ISSUED);
        }
    }

    @Nested
    @DisplayName("술어 메서드")
    class PredicateMethods {

        @Test
        @DisplayName("PENDING 상태 주문은 isPending이 true이다")
        void pendingOrderIsPending() {
            GifticonOrder order = createPendingOrder();
            assertThat(order.isPending()).isTrue();
            assertThat(order.isIssued()).isFalse();
        }

        @Test
        @DisplayName("ISSUED 상태 주문은 isIssued이 true이다")
        void issuedOrderIsIssued() {
            GifticonOrder order = createIssuedOrder();
            assertThat(order.isIssued()).isTrue();
            assertThat(order.isPending()).isFalse();
        }
    }
}
