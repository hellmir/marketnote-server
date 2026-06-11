package com.personal.marketnote.fulfillment.domain.shipping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ShippingStatus 테스트")
class ShippingStatusTest {

    @Nested
    @DisplayName("술어 메서드")
    class PredicateTests {

        @Test
        @DisplayName("PREPARING 상태는 isPreparing()이 true를 반환한다")
        void preparingIsPreparing() {
            assertThat(ShippingStatus.PREPARING.isPreparing()).isTrue();
        }

        @Test
        @DisplayName("SHIPPING 상태는 isShipping()이 true를 반환한다")
        void shippingIsShipping() {
            assertThat(ShippingStatus.SHIPPING.isShipping()).isTrue();
        }

        @Test
        @DisplayName("DELIVERED 상태는 isDelivered()가 true를 반환한다")
        void deliveredIsDelivered() {
            assertThat(ShippingStatus.DELIVERED.isDelivered()).isTrue();
        }

        @Test
        @DisplayName("CANCELLED 상태는 isCancelled()가 true를 반환한다")
        void cancelledIsCancelled() {
            assertThat(ShippingStatus.CANCELLED.isCancelled()).isTrue();
        }

        @Test
        @DisplayName("RETURN_SHIPPING 상태는 isReturnShipping()이 true를 반환한다")
        void returnShippingIsReturnShipping() {
            assertThat(ShippingStatus.RETURN_SHIPPING.isReturnShipping()).isTrue();
        }

        @Test
        @DisplayName("RETURN_DELIVERED 상태는 isReturnDelivered()가 true를 반환한다")
        void returnDeliveredIsReturnDelivered() {
            assertThat(ShippingStatus.RETURN_DELIVERED.isReturnDelivered()).isTrue();
        }
    }

    @Nested
    @DisplayName("폴링 대상 판단")
    class PollingTargetTests {

        @ParameterizedTest
        @EnumSource(value = ShippingStatus.class, names = {"PREPARING", "SHIPPING", "RETURN_SHIPPING"})
        @DisplayName("PREPARING/SHIPPING/RETURN_SHIPPING 상태는 폴링 대상이다")
        void pollingTargetStatuses(ShippingStatus status) {
            assertThat(status.isPollingTarget()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = ShippingStatus.class, names = {"DELIVERED", "CANCELLED", "RETURN_DELIVERED"})
        @DisplayName("DELIVERED/CANCELLED/RETURN_DELIVERED 상태는 폴링 대상이 아니다")
        void nonPollingTargetStatuses(ShippingStatus status) {
            assertThat(status.isPollingTarget()).isFalse();
        }
    }

    @Nested
    @DisplayName("종료 상태 판단")
    class TerminalTests {

        @ParameterizedTest
        @EnumSource(value = ShippingStatus.class, names = {"DELIVERED", "CANCELLED", "RETURN_DELIVERED"})
        @DisplayName("DELIVERED/CANCELLED/RETURN_DELIVERED 상태는 종료 상태이다")
        void terminalStatuses(ShippingStatus status) {
            assertThat(status.isTerminal()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = ShippingStatus.class, names = {"PREPARING", "SHIPPING", "RETURN_SHIPPING"})
        @DisplayName("PREPARING/SHIPPING/RETURN_SHIPPING 상태는 종료 상태가 아니다")
        void nonTerminalStatuses(ShippingStatus status) {
            assertThat(status.isTerminal()).isFalse();
        }
    }

    @Nested
    @DisplayName("상태 전이 규칙")
    class TransitionTests {

        @Test
        @DisplayName("PREPARING에서 SHIPPING으로 전이할 수 있다")
        void preparingToShipping() {
            assertThat(ShippingStatus.PREPARING.canTransitionTo(ShippingStatus.SHIPPING)).isTrue();
        }

        @Test
        @DisplayName("PREPARING에서 CANCELLED로 전이할 수 있다")
        void preparingToCancelled() {
            assertThat(ShippingStatus.PREPARING.canTransitionTo(ShippingStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("PREPARING에서 DELIVERED로 직접 전이할 수 없다")
        void preparingToDelivered() {
            assertThat(ShippingStatus.PREPARING.canTransitionTo(ShippingStatus.DELIVERED)).isFalse();
        }

        @Test
        @DisplayName("SHIPPING에서 DELIVERED로 전이할 수 있다")
        void shippingToDelivered() {
            assertThat(ShippingStatus.SHIPPING.canTransitionTo(ShippingStatus.DELIVERED)).isTrue();
        }

        @Test
        @DisplayName("SHIPPING에서 CANCELLED로 전이할 수 있다")
        void shippingToCancelled() {
            assertThat(ShippingStatus.SHIPPING.canTransitionTo(ShippingStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("SHIPPING에서 PREPARING으로 역전이할 수 없다")
        void shippingToPreparing() {
            assertThat(ShippingStatus.SHIPPING.canTransitionTo(ShippingStatus.PREPARING)).isFalse();
        }

        @Test
        @DisplayName("DELIVERED에서 RETURN_SHIPPING으로 전이할 수 있다")
        void deliveredToReturnShipping() {
            assertThat(ShippingStatus.DELIVERED.canTransitionTo(ShippingStatus.RETURN_SHIPPING)).isTrue();
        }

        @Test
        @DisplayName("DELIVERED에서 CANCELLED로 전이할 수 없다")
        void deliveredToCancelled() {
            assertThat(ShippingStatus.DELIVERED.canTransitionTo(ShippingStatus.CANCELLED)).isFalse();
        }

        @Test
        @DisplayName("RETURN_SHIPPING에서 RETURN_DELIVERED로 전이할 수 있다")
        void returnShippingToReturnDelivered() {
            assertThat(ShippingStatus.RETURN_SHIPPING.canTransitionTo(ShippingStatus.RETURN_DELIVERED)).isTrue();
        }

        @Test
        @DisplayName("CANCELLED에서는 어떤 상태로도 전이할 수 없다")
        void cancelledToAny() {
            for (ShippingStatus target : ShippingStatus.values()) {
                assertThat(ShippingStatus.CANCELLED.canTransitionTo(target)).isFalse();
            }
        }

        @Test
        @DisplayName("RETURN_DELIVERED에서는 어떤 상태로도 전이할 수 없다")
        void returnDeliveredToAny() {
            for (ShippingStatus target : ShippingStatus.values()) {
                assertThat(ShippingStatus.RETURN_DELIVERED.canTransitionTo(target)).isFalse();
            }
        }
    }
}
