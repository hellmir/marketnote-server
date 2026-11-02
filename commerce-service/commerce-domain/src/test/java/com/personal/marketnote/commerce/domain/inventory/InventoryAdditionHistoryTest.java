package com.personal.marketnote.commerce.domain.inventory;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidQuantityException;
import com.personal.marketnote.common.domain.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("InventoryAdditionHistory 도메인 VO 테스트")
class InventoryAdditionHistoryTest {

    @Nested
    @DisplayName("CreateState 팩토리")
    class FromCreateState {

        @Test
        @DisplayName("CreateState의 모든 필드를 도메인에 매핑한다 - ID는 null 유지")
        void shouldMapAllFieldsFromCreateState() {
            InventoryAdditionHistoryCreateState state = InventoryAdditionHistoryCreateState.builder()
                    .productId(10L)
                    .pricePolicyId(20L)
                    .stock(30)
                    .reason("입고")
                    .unitPrice(5000L)
                    .supplier("공급사A")
                    .build();

            InventoryAdditionHistory history = InventoryAdditionHistory.from(state);

            assertThat(history.getId()).isNull();
            assertThat(history.getProductId()).isEqualTo(10L);
            assertThat(history.getPricePolicyId()).isEqualTo(20L);
            assertThat(history.getStock().getValue()).isEqualTo(30);
            assertThat(history.getReason()).isEqualTo("입고");
            assertThat(history.getUnitPrice()).isEqualTo(Money.of(5000L));
            assertThat(history.getSupplier()).isEqualTo("공급사A");
        }

        @Test
        @DisplayName("stock 0 경계값에서도 Stock VO를 생성한다")
        void shouldAcceptZeroStock() {
            InventoryAdditionHistoryCreateState state = InventoryAdditionHistoryCreateState.builder()
                    .productId(1L)
                    .pricePolicyId(1L)
                    .stock(0)
                    .reason("초기")
                    .unitPrice(0L)
                    .supplier("s")
                    .build();

            InventoryAdditionHistory history = InventoryAdditionHistory.from(state);

            assertThat(history.getStock().getValue()).isEqualTo(0);
            assertThat(history.getUnitPrice().isZero()).isTrue();
        }

        @Test
        @DisplayName("stock이 음수이면 InvalidQuantityException이 발생한다")
        void shouldThrowWhenStockIsNegative() {
            InventoryAdditionHistoryCreateState state = InventoryAdditionHistoryCreateState.builder()
                    .productId(1L)
                    .pricePolicyId(1L)
                    .stock(-1)
                    .reason("입고")
                    .unitPrice(100L)
                    .supplier("s")
                    .build();

            assertThatThrownBy(() -> InventoryAdditionHistory.from(state))
                    .isInstanceOf(InvalidQuantityException.class);
        }
    }

    @Nested
    @DisplayName("SnapshotState 팩토리")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState로부터 ID를 포함한 모든 필드를 복원한다")
        void shouldRestoreAllFieldsIncludingId() {
            InventoryAdditionHistorySnapshotState state = InventoryAdditionHistorySnapshotState.builder()
                    .id(99L)
                    .productId(11L)
                    .pricePolicyId(22L)
                    .stock(33)
                    .reason("보충")
                    .unitPrice(1500L)
                    .supplier("공급사B")
                    .build();

            InventoryAdditionHistory history = InventoryAdditionHistory.from(state);

            assertThat(history.getId()).isEqualTo(99L);
            assertThat(history.getProductId()).isEqualTo(11L);
            assertThat(history.getPricePolicyId()).isEqualTo(22L);
            assertThat(history.getStock().getValue()).isEqualTo(33);
            assertThat(history.getReason()).isEqualTo("보충");
            assertThat(history.getUnitPrice()).isEqualTo(Money.of(1500L));
            assertThat(history.getSupplier()).isEqualTo("공급사B");
        }
    }

    @Nested
    @DisplayName("unitPrice Money 변환")
    class UnitPriceConversion {

        @Test
        @DisplayName("CreateState의 unitPrice가 null이면 Money.zero로 변환한다")
        void shouldConvertNullUnitPriceToZeroOnCreate() {
            InventoryAdditionHistoryCreateState state = InventoryAdditionHistoryCreateState.builder()
                    .productId(1L)
                    .pricePolicyId(1L)
                    .stock(5)
                    .reason("입고")
                    .unitPrice(null)
                    .supplier("공급사")
                    .build();

            InventoryAdditionHistory history = InventoryAdditionHistory.from(state);

            assertThat(history.getUnitPrice()).isEqualTo(Money.zero());
            assertThat(history.getUnitPrice().isZero()).isTrue();
        }

        @Test
        @DisplayName("SnapshotState의 unitPrice가 null이면 Money.zero로 복원한다")
        void shouldConvertNullUnitPriceToZeroOnSnapshot() {
            InventoryAdditionHistorySnapshotState state = InventoryAdditionHistorySnapshotState.builder()
                    .id(1L)
                    .productId(1L)
                    .pricePolicyId(1L)
                    .stock(5)
                    .reason("보충")
                    .unitPrice(null)
                    .supplier("공급사")
                    .build();

            InventoryAdditionHistory history = InventoryAdditionHistory.from(state);

            assertThat(history.getUnitPrice()).isEqualTo(Money.zero());
            assertThat(history.getUnitPrice().isZero()).isTrue();
        }

        @Test
        @DisplayName("unitPrice가 0이면 Money.zero로 설정된다")
        void shouldMapZeroUnitPriceToMoneyZero() {
            InventoryAdditionHistoryCreateState state = InventoryAdditionHistoryCreateState.builder()
                    .productId(1L)
                    .pricePolicyId(1L)
                    .stock(5)
                    .reason("입고")
                    .unitPrice(0L)
                    .supplier("공급사")
                    .build();

            InventoryAdditionHistory history = InventoryAdditionHistory.from(state);

            assertThat(history.getUnitPrice()).isEqualTo(Money.zero());
        }

        @Test
        @DisplayName("unitPrice가 양수이면 Money.of 값으로 변환된다")
        void shouldMapPositiveUnitPriceToMoney() {
            InventoryAdditionHistoryCreateState state = InventoryAdditionHistoryCreateState.builder()
                    .productId(1L)
                    .pricePolicyId(1L)
                    .stock(5)
                    .reason("입고")
                    .unitPrice(12345L)
                    .supplier("공급사")
                    .build();

            InventoryAdditionHistory history = InventoryAdditionHistory.from(state);

            assertThat(history.getUnitPrice().getValue()).isEqualTo(12345L);
        }
    }

    @Nested
    @DisplayName("Stock VO 변환")
    class StockConversion {

        @Test
        @DisplayName("CreateState의 Integer stock이 Stock VO로 변환되고 내부 값은 동일하다")
        void shouldConvertIntegerStockToStockVoOnCreate() {
            InventoryAdditionHistoryCreateState state = InventoryAdditionHistoryCreateState.builder()
                    .productId(1L)
                    .pricePolicyId(1L)
                    .stock(77)
                    .reason("입고")
                    .unitPrice(1000L)
                    .supplier("공급사")
                    .build();

            InventoryAdditionHistory history = InventoryAdditionHistory.from(state);

            assertThat(history.getStock()).isEqualTo(Stock.of("77"));
            assertThat(history.getStock().getValue()).isEqualTo(77);
        }

        @Test
        @DisplayName("SnapshotState의 Integer stock이 Stock VO로 복원되고 내부 값은 동일하다")
        void shouldConvertIntegerStockToStockVoOnSnapshot() {
            InventoryAdditionHistorySnapshotState state = InventoryAdditionHistorySnapshotState.builder()
                    .id(1L)
                    .productId(1L)
                    .pricePolicyId(1L)
                    .stock(88)
                    .reason("보충")
                    .unitPrice(1000L)
                    .supplier("공급사")
                    .build();

            InventoryAdditionHistory history = InventoryAdditionHistory.from(state);

            assertThat(history.getStock()).isEqualTo(Stock.of("88"));
            assertThat(history.getStock().getValue()).isEqualTo(88);
        }
    }
}
