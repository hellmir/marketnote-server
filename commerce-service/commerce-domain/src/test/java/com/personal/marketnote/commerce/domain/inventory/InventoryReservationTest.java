package com.personal.marketnote.commerce.domain.inventory;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidQuantityException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.IdNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("InventoryReservation 도메인 테스트")
class InventoryReservationTest {

    @Nested
    @DisplayName("from CreateState")
    class FromCreateState {

        @Test
        @DisplayName("CreateState로부터 InventoryReservation을 생성한다")
        void shouldCreateFromCreateState() {
            // given
            LocalDateTime now = LocalDateTime.of(2026, 4, 2, 10, 0, 0);
            InventoryReservationCreateState state = InventoryReservationCreateState.builder()
                    .orderId(1L)
                    .pricePolicyId(100L)
                    .quantity(3)
                    .reservedAt(now)
                    .build();

            // when
            InventoryReservation reservation = InventoryReservation.from(state);

            // then
            assertThat(reservation.getOrderId()).isEqualTo(1L);
            assertThat(reservation.getPricePolicyId()).isEqualTo(100L);
            assertThat(reservation.getQuantity()).isEqualTo(3);
            assertThat(reservation.getReservedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("orderId가 null이면 IdNoValueException을 던진다")
        void shouldThrowWhenOrderIdIsNull() {
            // given
            InventoryReservationCreateState state = InventoryReservationCreateState.builder()
                    .orderId(null)
                    .pricePolicyId(100L)
                    .quantity(3)
                    .reservedAt(LocalDateTime.now())
                    .build();

            // when & then
            assertThatThrownBy(() -> InventoryReservation.from(state))
                    .isInstanceOf(IdNoValueException.class);
        }

        @Test
        @DisplayName("pricePolicyId가 null이면 IdNoValueException을 던진다")
        void shouldThrowWhenPricePolicyIdIsNull() {
            // given
            InventoryReservationCreateState state = InventoryReservationCreateState.builder()
                    .orderId(1L)
                    .pricePolicyId(null)
                    .quantity(3)
                    .reservedAt(LocalDateTime.now())
                    .build();

            // when & then
            assertThatThrownBy(() -> InventoryReservation.from(state))
                    .isInstanceOf(IdNoValueException.class);
        }

        @Test
        @DisplayName("수량이 0 이하이면 InvalidQuantityException을 던진다")
        void shouldThrowWhenQuantityIsZeroOrNegative() {
            // given
            InventoryReservationCreateState state = InventoryReservationCreateState.builder()
                    .orderId(1L)
                    .pricePolicyId(100L)
                    .quantity(0)
                    .reservedAt(LocalDateTime.now())
                    .build();

            // when & then
            assertThatThrownBy(() -> InventoryReservation.from(state))
                    .isInstanceOf(InvalidQuantityException.class);
        }
    }

    @Nested
    @DisplayName("from SnapshotState")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState로부터 InventoryReservation을 복원한다")
        void shouldRestoreFromSnapshotState() {
            // given
            LocalDateTime now = LocalDateTime.of(2026, 4, 2, 10, 0, 0);
            InventoryReservationSnapshotState state = InventoryReservationSnapshotState.builder()
                    .id(1L)
                    .orderId(2L)
                    .pricePolicyId(200L)
                    .quantity(5)
                    .reservedAt(now)
                    .build();

            // when
            InventoryReservation reservation = InventoryReservation.from(state);

            // then
            assertThat(reservation.getId()).isEqualTo(1L);
            assertThat(reservation.getOrderId()).isEqualTo(2L);
            assertThat(reservation.getPricePolicyId()).isEqualTo(200L);
            assertThat(reservation.getQuantity()).isEqualTo(5);
            assertThat(reservation.getReservedAt()).isEqualTo(now);
        }
    }
}
