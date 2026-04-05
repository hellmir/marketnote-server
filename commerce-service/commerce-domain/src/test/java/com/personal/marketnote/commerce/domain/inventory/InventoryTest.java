package com.personal.marketnote.commerce.domain.inventory;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidQuantityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Inventory 도메인 테스트")
class InventoryTest {

    private Inventory createInventory(int stock, int reserved) {
        InventorySnapshotState state = InventorySnapshotState.builder()
                .productId(1L)
                .pricePolicyId(100L)
                .stock(stock)
                .version(0L)
                .reserved(reserved)
                .build();
        return Inventory.from(state);
    }

    @Nested
    @DisplayName("availableStock")
    class AvailableStock {

        @Test
        @DisplayName("가용재고는 stock - reserved이다")
        void shouldReturnStockMinusReserved() {
            // given
            Inventory inventory = createInventory(10, 3);

            // when
            int availableStock = inventory.availableStock();

            // then
            assertThat(availableStock).isEqualTo(7);
        }

        @Test
        @DisplayName("reserved가 0이면 가용재고는 stock과 같다")
        void shouldReturnStockWhenReservedIsZero() {
            // given
            Inventory inventory = createInventory(10, 0);

            // when
            int availableStock = inventory.availableStock();

            // then
            assertThat(availableStock).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("reserve")
    class Reserve {

        @Test
        @DisplayName("가용재고가 충분하면 reserved를 증가시킨다")
        void shouldIncreaseReservedWhenAvailableStockIsSufficient() {
            // given
            Inventory inventory = createInventory(10, 0);

            // when
            inventory.reserve(3);

            // then
            assertThat(inventory.getReserved()).isEqualTo(3);
            assertThat(inventory.availableStock()).isEqualTo(7);
            assertThat(inventory.getStockValue()).isEqualTo(10);
        }

        @Test
        @DisplayName("이미 예약이 있는 상태에서 추가 예약 시 reserved가 누적된다")
        void shouldAccumulateReserved() {
            // given
            Inventory inventory = createInventory(10, 2);

            // when
            inventory.reserve(3);

            // then
            assertThat(inventory.getReserved()).isEqualTo(5);
            assertThat(inventory.availableStock()).isEqualTo(5);
        }

        @Test
        @DisplayName("가용재고가 부족하면 InsufficientAvailableStockException을 던진다")
        void shouldThrowWhenAvailableStockIsInsufficient() {
            // given
            Inventory inventory = createInventory(10, 8);

            // when & then
            assertThatThrownBy(() -> inventory.reserve(5))
                    .isInstanceOf(InsufficientAvailableStockException.class);
        }

        @Test
        @DisplayName("가용재고와 정확히 같은 수량 예약은 성공한다")
        void shouldSucceedWhenReservingExactAvailableStock() {
            // given
            Inventory inventory = createInventory(10, 3);

            // when
            inventory.reserve(7);

            // then
            assertThat(inventory.getReserved()).isEqualTo(10);
            assertThat(inventory.availableStock()).isEqualTo(0);
        }

        @Test
        @DisplayName("0 이하 수량 예약 시 InvalidQuantityException을 던진다")
        void shouldThrowWhenQuantityIsZeroOrNegative() {
            // given
            Inventory inventory = createInventory(10, 0);

            // when & then
            assertThatThrownBy(() -> inventory.reserve(0))
                    .isInstanceOf(InvalidQuantityException.class);
            assertThatThrownBy(() -> inventory.reserve(-1))
                    .isInstanceOf(InvalidQuantityException.class);
        }
    }

    @Nested
    @DisplayName("confirmReservation")
    class ConfirmReservation {

        @Test
        @DisplayName("예약 확정 시 reserved 감소 + stock 감소한다")
        void shouldDecreaseReservedAndStock() {
            // given
            Inventory inventory = createInventory(10, 5);

            // when
            inventory.confirmReservation(3);

            // then
            assertThat(inventory.getReserved()).isEqualTo(2);
            assertThat(inventory.getStockValue()).isEqualTo(7);
            assertThat(inventory.availableStock()).isEqualTo(5);
        }

        @Test
        @DisplayName("전체 예약을 확정하면 reserved가 0이 된다")
        void shouldSetReservedToZeroWhenConfirmingAll() {
            // given
            Inventory inventory = createInventory(10, 5);

            // when
            inventory.confirmReservation(5);

            // then
            assertThat(inventory.getReserved()).isEqualTo(0);
            assertThat(inventory.getStockValue()).isEqualTo(5);
        }

        @Test
        @DisplayName("확정 수량이 현재 예약 수량을 초과하면 InvalidInventoryReservationQuantityException을 던진다")
        void shouldThrowWhenQuantityExceedsReserved() {
            // given
            Inventory inventory = createInventory(10, 3);

            // when & then
            assertThatThrownBy(() -> inventory.confirmReservation(5))
                    .isInstanceOf(InvalidInventoryReservationQuantityException.class);
        }

        @Test
        @DisplayName("0 이하 수량 확정 시 InvalidQuantityException을 던진다")
        void shouldThrowWhenQuantityIsZeroOrNegative() {
            // given
            Inventory inventory = createInventory(10, 5);

            // when & then
            assertThatThrownBy(() -> inventory.confirmReservation(0))
                    .isInstanceOf(InvalidQuantityException.class);
            assertThatThrownBy(() -> inventory.confirmReservation(-1))
                    .isInstanceOf(InvalidQuantityException.class);
        }
    }

    @Nested
    @DisplayName("releaseReservation")
    class ReleaseReservation {

        @Test
        @DisplayName("예약 해제 시 reserved가 감소한다")
        void shouldDecreaseReserved() {
            // given
            Inventory inventory = createInventory(10, 5);

            // when
            inventory.releaseReservation(3);

            // then
            assertThat(inventory.getReserved()).isEqualTo(2);
            assertThat(inventory.getStockValue()).isEqualTo(10);
            assertThat(inventory.availableStock()).isEqualTo(8);
        }

        @Test
        @DisplayName("예약 해제 결과가 0 이하이면 reserved를 0으로 설정한다")
        void shouldSetReservedToZeroWhenResultIsNegative() {
            // given
            Inventory inventory = createInventory(10, 2);

            // when
            inventory.releaseReservation(5);

            // then
            assertThat(inventory.getReserved()).isEqualTo(0);
        }

        @Test
        @DisplayName("전체 예약을 해제하면 가용재고가 stock과 같아진다")
        void shouldRestoreFullAvailableStock() {
            // given
            Inventory inventory = createInventory(10, 10);

            // when
            inventory.releaseReservation(10);

            // then
            assertThat(inventory.getReserved()).isEqualTo(0);
            assertThat(inventory.availableStock()).isEqualTo(10);
        }

        @Test
        @DisplayName("0 이하 수량 해제 시 InvalidQuantityException을 던진다")
        void shouldThrowWhenQuantityIsZeroOrNegative() {
            // given
            Inventory inventory = createInventory(10, 5);

            // when & then
            assertThatThrownBy(() -> inventory.releaseReservation(0))
                    .isInstanceOf(InvalidQuantityException.class);
            assertThatThrownBy(() -> inventory.releaseReservation(-1))
                    .isInstanceOf(InvalidQuantityException.class);
        }
    }

    @Nested
    @DisplayName("from SnapshotState")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState로부터 Inventory를 복원한다")
        void shouldRestoreFromSnapshotState() {
            // given
            InventorySnapshotState state = InventorySnapshotState.builder()
                    .productId(1L)
                    .pricePolicyId(100L)
                    .stock(10)
                    .version(5L)
                    .reserved(3)
                    .build();

            // when
            Inventory inventory = Inventory.from(state);

            // then
            assertThat(inventory.getProductId()).isEqualTo(1L);
            assertThat(inventory.getPricePolicyId()).isEqualTo(100L);
            assertThat(inventory.getStockValue()).isEqualTo(10);
            assertThat(inventory.getVersion()).isEqualTo(5L);
            assertThat(inventory.getReserved()).isEqualTo(3);
            assertThat(inventory.availableStock()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("of 팩토리 메서드 하위 호환성")
    class FactoryMethodBackwardCompatibility {

        @Test
        @DisplayName("reserved 없는 of() 메서드는 reserved를 0으로 초기화한다")
        void shouldInitializeReservedToZero() {
            // given & when
            Inventory inventory = Inventory.of(1L, 100L, 10, 0L);

            // then
            assertThat(inventory.getReserved()).isEqualTo(0);
            assertThat(inventory.availableStock()).isEqualTo(10);
        }
    }
}
