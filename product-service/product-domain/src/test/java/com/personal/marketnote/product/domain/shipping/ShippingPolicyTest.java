package com.personal.marketnote.product.domain.shipping;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShippingPolicyTest {

    @Nested
    @DisplayName("from(CreateState) 팩토리 메서드")
    class FromCreateState {

        @Test
        @DisplayName("유효한 값으로 배송비 정책을 생성한다")
        void shouldCreateShippingPolicyWithValidValues() {
            // given
            ShippingPolicyCreateState state = ShippingPolicyCreateState.builder()
                    .sellerId(1L)
                    .deliveryCompany("한진택배")
                    .shippingFee(3000L)
                    .freeShippingThreshold(20000L)
                    .build();

            // when
            ShippingPolicy policy = ShippingPolicy.from(state);

            // then
            assertThat(policy.getSellerId()).isEqualTo(1L);
            assertThat(policy.getDeliveryCompany()).isEqualTo("한진택배");
            assertThat(policy.getShippingFee()).isEqualTo(3000L);
            assertThat(policy.getFreeShippingThreshold()).isEqualTo(20000L);
            assertThat(policy.isActive()).isTrue();
        }

        @Test
        @DisplayName("배송비가 음수이면 예외가 발생한다")
        void shouldThrowExceptionWhenShippingFeeIsNegative() {
            // given
            ShippingPolicyCreateState state = ShippingPolicyCreateState.builder()
                    .sellerId(1L)
                    .deliveryCompany("한진택배")
                    .shippingFee(-1000L)
                    .freeShippingThreshold(20000L)
                    .build();

            // when & then
            assertThatThrownBy(() -> ShippingPolicy.from(state))
                    .isInstanceOf(InvalidShippingFeeException.class);
        }

        @Test
        @DisplayName("무료배송 기준금액이 음수이면 예외가 발생한다")
        void shouldThrowExceptionWhenFreeShippingThresholdIsNegative() {
            // given
            ShippingPolicyCreateState state = ShippingPolicyCreateState.builder()
                    .sellerId(1L)
                    .deliveryCompany("한진택배")
                    .shippingFee(3000L)
                    .freeShippingThreshold(-1L)
                    .build();

            // when & then
            assertThatThrownBy(() -> ShippingPolicy.from(state))
                    .isInstanceOf(InvalidFreeShippingThresholdException.class);
        }

        @Test
        @DisplayName("배송비가 0원이면 정상 생성된다")
        void shouldCreateShippingPolicyWithZeroShippingFee() {
            // given
            ShippingPolicyCreateState state = ShippingPolicyCreateState.builder()
                    .sellerId(1L)
                    .deliveryCompany("한진택배")
                    .shippingFee(0L)
                    .freeShippingThreshold(0L)
                    .build();

            // when
            ShippingPolicy policy = ShippingPolicy.from(state);

            // then
            assertThat(policy.getShippingFee()).isZero();
            assertThat(policy.getFreeShippingThreshold()).isZero();
        }
    }

    @Nested
    @DisplayName("from(SnapshotState) 팩토리 메서드")
    class FromSnapshotState {

        @Test
        @DisplayName("DB 스냅샷으로부터 배송비 정책을 복원한다")
        void shouldRestoreShippingPolicyFromSnapshot() {
            // given
            LocalDateTime now = LocalDateTime.of(2026, 3, 10, 12, 0);
            ShippingPolicySnapshotState state = ShippingPolicySnapshotState.builder()
                    .id(10L)
                    .sellerId(1L)
                    .deliveryCompany("CJ대한통운")
                    .shippingFee(2500L)
                    .freeShippingThreshold(30000L)
                    .status(EntityStatus.INACTIVE)
                    .createdAt(now)
                    .modifiedAt(now)
                    .build();

            // when
            ShippingPolicy policy = ShippingPolicy.from(state);

            // then
            assertThat(policy.getId()).isEqualTo(10L);
            assertThat(policy.getSellerId()).isEqualTo(1L);
            assertThat(policy.getDeliveryCompany()).isEqualTo("CJ대한통운");
            assertThat(policy.getShippingFee()).isEqualTo(2500L);
            assertThat(policy.getFreeShippingThreshold()).isEqualTo(30000L);
            assertThat(policy.isInactive()).isTrue();
            assertThat(policy.getCreatedAt()).isEqualTo(now);
            assertThat(policy.getModifiedAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("update 메서드")
    class Update {

        @Test
        @DisplayName("배송비 정책 정보를 수정한다")
        void shouldUpdateShippingPolicyInfo() {
            // given
            ShippingPolicy policy = createDefaultPolicy();

            // when
            policy.update("CJ대한통운", 2500L, 30000L);

            // then
            assertThat(policy.getDeliveryCompany()).isEqualTo("CJ대한통운");
            assertThat(policy.getShippingFee()).isEqualTo(2500L);
            assertThat(policy.getFreeShippingThreshold()).isEqualTo(30000L);
        }

        @Test
        @DisplayName("수정 시 배송비가 음수이면 예외가 발생한다")
        void shouldThrowExceptionWhenUpdateWithNegativeShippingFee() {
            // given
            ShippingPolicy policy = createDefaultPolicy();

            // when & then
            assertThatThrownBy(() -> policy.update("CJ대한통운", -1L, 30000L))
                    .isInstanceOf(InvalidShippingFeeException.class);
        }

        @Test
        @DisplayName("수정 시 무료배송 기준금액이 음수이면 예외가 발생한다")
        void shouldThrowExceptionWhenUpdateWithNegativeFreeShippingThreshold() {
            // given
            ShippingPolicy policy = createDefaultPolicy();

            // when & then
            assertThatThrownBy(() -> policy.update("CJ대한통운", 3000L, -1L))
                    .isInstanceOf(InvalidFreeShippingThresholdException.class);
        }
    }

    @Nested
    @DisplayName("배송비 계산 로직")
    class ShippingFeeCalculation {

        @Test
        @DisplayName("주문금액이 무료배송 기준금액 이상이면 무료배송이다")
        void shouldReturnFreeShippingWhenOrderAmountExceedsThreshold() {
            // given
            ShippingPolicy policy = createDefaultPolicy(); // shippingFee=3000, threshold=20000

            // when & then
            assertThat(policy.isFreeShipping(20000L)).isTrue();
            assertThat(policy.isFreeShipping(50000L)).isTrue();
            assertThat(policy.calculateShippingFee(20000L)).isZero();
            assertThat(policy.calculateShippingFee(50000L)).isZero();
        }

        @Test
        @DisplayName("주문금액이 무료배송 기준금액 미만이면 배송비가 부과된다")
        void shouldReturnShippingFeeWhenOrderAmountBelowThreshold() {
            // given
            ShippingPolicy policy = createDefaultPolicy(); // shippingFee=3000, threshold=20000

            // when & then
            assertThat(policy.isFreeShipping(19999L)).isFalse();
            assertThat(policy.isFreeShipping(0L)).isFalse();
            assertThat(policy.calculateShippingFee(19999L)).isEqualTo(3000L);
            assertThat(policy.calculateShippingFee(0L)).isEqualTo(3000L);
        }

        @Test
        @DisplayName("무료배송 기준금액이 0이면 항상 무료배송이다")
        void shouldAlwaysBeFreeWhenThresholdIsZero() {
            // given
            ShippingPolicyCreateState state = ShippingPolicyCreateState.builder()
                    .sellerId(1L)
                    .deliveryCompany("한진택배")
                    .shippingFee(3000L)
                    .freeShippingThreshold(0L)
                    .build();
            ShippingPolicy policy = ShippingPolicy.from(state);

            // when & then
            assertThat(policy.isFreeShipping(0L)).isTrue();
            assertThat(policy.calculateShippingFee(0L)).isZero();
        }
    }

    @Nested
    @DisplayName("상태 전이 메서드")
    class StatusTransition {

        @Test
        @DisplayName("배송비 정책을 비활성화한다")
        void shouldDeactivateShippingPolicy() {
            // given
            ShippingPolicy policy = createDefaultPolicy();
            assertThat(policy.isActive()).isTrue();

            // when
            policy.deactivate();

            // then
            assertThat(policy.isInactive()).isTrue();
        }
    }

    private ShippingPolicy createDefaultPolicy() {
        ShippingPolicyCreateState state = ShippingPolicyCreateState.builder()
                .sellerId(1L)
                .deliveryCompany("한진택배")
                .shippingFee(3000L)
                .freeShippingThreshold(20000L)
                .build();
        return ShippingPolicy.from(state);
    }
}
