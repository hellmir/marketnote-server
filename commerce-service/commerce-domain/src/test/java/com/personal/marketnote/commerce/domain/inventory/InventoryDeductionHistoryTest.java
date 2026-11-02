package com.personal.marketnote.commerce.domain.inventory;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidQuantityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("InventoryDeductionHistory 도메인 VO 테스트")
class InventoryDeductionHistoryTest {

    @Nested
    @DisplayName("CreateState 팩토리")
    class FromCreateState {

        @Test
        @DisplayName("CreateState의 모든 필드를 도메인에 매핑한다 - ID는 null 유지")
        void shouldMapAllFieldsFromCreateState() {
            InventoryDeductionHistoryCreateState state = InventoryDeductionHistoryCreateState.builder()
                    .productId(10L)
                    .pricePolicyId(20L)
                    .orderId(30L)
                    .stock(40)
                    .reason("차감")
                    .build();

            InventoryDeductionHistory history = InventoryDeductionHistory.from(state);

            assertThat(history.getId()).isNull();
            assertThat(history.getProductId()).isEqualTo(10L);
            assertThat(history.getPricePolicyId()).isEqualTo(20L);
            assertThat(history.getOrderId()).isEqualTo(30L);
            assertThat(history.getStock().getValue()).isEqualTo(40);
            assertThat(history.getReason()).isEqualTo("차감");
        }
    }

    @Nested
    @DisplayName("SnapshotState 팩토리")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState로부터 ID를 포함한 모든 필드를 복원한다")
        void shouldRestoreAllFieldsIncludingId() {
            InventoryDeductionHistorySnapshotState state = InventoryDeductionHistorySnapshotState.builder()
                    .id(101L)
                    .productId(11L)
                    .pricePolicyId(22L)
                    .orderId(33L)
                    .stock(44)
                    .reason("주문 차감")
                    .build();

            InventoryDeductionHistory history = InventoryDeductionHistory.from(state);

            assertThat(history.getId()).isEqualTo(101L);
            assertThat(history.getProductId()).isEqualTo(11L);
            assertThat(history.getPricePolicyId()).isEqualTo(22L);
            assertThat(history.getOrderId()).isEqualTo(33L);
            assertThat(history.getStock().getValue()).isEqualTo(44);
            assertThat(history.getReason()).isEqualTo("주문 차감");
        }
    }

    @Nested
    @DisplayName("getStockValue 경계 처리")
    class GetStockValue {

        @Test
        @DisplayName("stock이 0이면 getStockValue는 0을 반환한다")
        void shouldReturnZeroWhenStockIsZero() {
            InventoryDeductionHistoryCreateState state = InventoryDeductionHistoryCreateState.builder()
                    .productId(1L)
                    .pricePolicyId(1L)
                    .orderId(1L)
                    .stock(0)
                    .reason("0 차감")
                    .build();

            InventoryDeductionHistory history = InventoryDeductionHistory.from(state);

            assertThat(history.getStockValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("stock이 양수면 getStockValue는 동일한 값을 반환한다")
        void shouldReturnPositiveStockValue() {
            InventoryDeductionHistoryCreateState state = InventoryDeductionHistoryCreateState.builder()
                    .productId(1L)
                    .pricePolicyId(1L)
                    .orderId(1L)
                    .stock(150)
                    .reason("차감")
                    .build();

            InventoryDeductionHistory history = InventoryDeductionHistory.from(state);

            assertThat(history.getStockValue()).isEqualTo(150);
        }

        @Test
        @DisplayName("stock이 음수이면 Stock VO 생성 단계에서 InvalidQuantityException이 발생한다")
        void shouldThrowWhenStockIsNegative() {
            InventoryDeductionHistoryCreateState state = InventoryDeductionHistoryCreateState.builder()
                    .productId(1L)
                    .pricePolicyId(1L)
                    .orderId(1L)
                    .stock(-5)
                    .reason("음수 차감")
                    .build();

            assertThatThrownBy(() -> InventoryDeductionHistory.from(state))
                    .isInstanceOf(InvalidQuantityException.class);
        }

        @Test
        @DisplayName("SnapshotState 복원 시에도 getStockValue는 동일 값을 반환한다")
        void shouldReturnStockValueAfterSnapshotRestore() {
            InventoryDeductionHistorySnapshotState state = InventoryDeductionHistorySnapshotState.builder()
                    .id(1L)
                    .productId(1L)
                    .pricePolicyId(1L)
                    .orderId(1L)
                    .stock(77)
                    .reason("복원")
                    .build();

            InventoryDeductionHistory history = InventoryDeductionHistory.from(state);

            assertThat(history.getStockValue()).isEqualTo(77);
        }
    }
}
