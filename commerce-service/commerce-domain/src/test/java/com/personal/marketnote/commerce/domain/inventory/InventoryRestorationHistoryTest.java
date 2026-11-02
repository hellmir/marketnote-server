package com.personal.marketnote.commerce.domain.inventory;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidQuantityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("InventoryRestorationHistory 도메인 VO 테스트")
class InventoryRestorationHistoryTest {

    @Nested
    @DisplayName("CreateState 팩토리")
    class FromCreateState {

        @Test
        @DisplayName("CreateState의 모든 필드를 도메인에 매핑한다 - ID는 null 유지")
        void shouldMapAllFieldsFromCreateState() {
            InventoryRestorationHistoryCreateState state = InventoryRestorationHistoryCreateState.builder()
                    .productId(10L)
                    .pricePolicyId(20L)
                    .orderId(30L)
                    .stock(40)
                    .reason("복구")
                    .build();

            InventoryRestorationHistory history = InventoryRestorationHistory.from(state);

            assertThat(history.getId()).isNull();
            assertThat(history.getProductId()).isEqualTo(10L);
            assertThat(history.getPricePolicyId()).isEqualTo(20L);
            assertThat(history.getOrderId()).isEqualTo(30L);
            assertThat(history.getStock().getValue()).isEqualTo(40);
            assertThat(history.getReason()).isEqualTo("복구");
        }

        @Test
        @DisplayName("stock 0 경계값에서도 생성 가능하다")
        void shouldAcceptZeroStock() {
            InventoryRestorationHistoryCreateState state = InventoryRestorationHistoryCreateState.builder()
                    .productId(1L)
                    .pricePolicyId(1L)
                    .orderId(1L)
                    .stock(0)
                    .reason("0 복구")
                    .build();

            InventoryRestorationHistory history = InventoryRestorationHistory.from(state);

            assertThat(history.getStockValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("stock이 음수이면 Stock VO 생성 단계에서 InvalidQuantityException이 발생한다")
        void shouldThrowWhenStockIsNegative() {
            InventoryRestorationHistoryCreateState state = InventoryRestorationHistoryCreateState.builder()
                    .productId(1L)
                    .pricePolicyId(1L)
                    .orderId(1L)
                    .stock(-3)
                    .reason("음수 복구")
                    .build();

            assertThatThrownBy(() -> InventoryRestorationHistory.from(state))
                    .isInstanceOf(InvalidQuantityException.class);
        }
    }

    @Nested
    @DisplayName("SnapshotState 팩토리")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState로부터 ID를 포함한 모든 필드를 복원한다")
        void shouldRestoreAllFieldsIncludingId() {
            InventoryRestorationHistorySnapshotState state = InventoryRestorationHistorySnapshotState.builder()
                    .id(202L)
                    .productId(11L)
                    .pricePolicyId(22L)
                    .orderId(33L)
                    .stock(55)
                    .reason("주문 복구")
                    .build();

            InventoryRestorationHistory history = InventoryRestorationHistory.from(state);

            assertThat(history.getId()).isEqualTo(202L);
            assertThat(history.getProductId()).isEqualTo(11L);
            assertThat(history.getPricePolicyId()).isEqualTo(22L);
            assertThat(history.getOrderId()).isEqualTo(33L);
            assertThat(history.getStock().getValue()).isEqualTo(55);
            assertThat(history.getReason()).isEqualTo("주문 복구");
        }
    }

    @Nested
    @DisplayName("getStockValue 반환 값")
    class GetStockValue {

        @Test
        @DisplayName("CreateState 기반 stock 양수 값을 그대로 반환한다")
        void shouldReturnStockValueFromCreate() {
            InventoryRestorationHistoryCreateState state = InventoryRestorationHistoryCreateState.builder()
                    .productId(1L)
                    .pricePolicyId(1L)
                    .orderId(1L)
                    .stock(123)
                    .reason("복구")
                    .build();

            InventoryRestorationHistory history = InventoryRestorationHistory.from(state);

            assertThat(history.getStockValue()).isEqualTo(123);
        }

        @Test
        @DisplayName("SnapshotState 기반 stock 양수 값을 그대로 반환한다")
        void shouldReturnStockValueFromSnapshot() {
            InventoryRestorationHistorySnapshotState state = InventoryRestorationHistorySnapshotState.builder()
                    .id(1L)
                    .productId(1L)
                    .pricePolicyId(1L)
                    .orderId(1L)
                    .stock(456)
                    .reason("복원")
                    .build();

            InventoryRestorationHistory history = InventoryRestorationHistory.from(state);

            assertThat(history.getStockValue()).isEqualTo(456);
        }
    }
}
