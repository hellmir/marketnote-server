package com.personal.marketnote.commerce.adapter.out.persistence.inventory;

import com.personal.marketnote.commerce.adapter.out.persistence.inventory.repository.InventoryDeductionHistoryJpaRepository;
import com.personal.marketnote.commerce.adapter.out.persistence.inventory.repository.InventoryJpaRepository;
import com.personal.marketnote.commerce.adapter.out.persistence.inventory.repository.InventoryRestorationHistoryJpaRepository;
import com.personal.marketnote.commerce.domain.inventory.*;
import com.personal.marketnote.commerce.exception.DuplicateInventoryDeductionException;
import com.personal.marketnote.commerce.exception.DuplicateInventoryRestorationException;
import com.personal.marketnote.commerce.exception.InventoryNotFoundException;
import com.personal.marketnote.common.configuration.AuditConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, InventoryPersistenceAdapter.class})
@DisplayName("InventoryPersistenceAdapter 통합 테스트")
class InventoryPersistenceAdapterTest {

    @Autowired
    private InventoryPersistenceAdapter adapter;

    @Autowired
    private InventoryJpaRepository inventoryJpaRepository;

    @Autowired
    private InventoryDeductionHistoryJpaRepository deductionHistoryJpaRepository;

    @Autowired
    private InventoryRestorationHistoryJpaRepository restorationHistoryJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        deductionHistoryJpaRepository.deleteAll();
        restorationHistoryJpaRepository.deleteAll();
        inventoryJpaRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private void persistInventory(Long productId, Long pricePolicyId, int stock) {
        entityManager.createNativeQuery("""
                INSERT INTO inventory (product_id, price_policy_id, stock, reserved, version, created_at, modified_at)
                VALUES (:productId, :pricePolicyId, :stock, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """)
                .setParameter("productId", productId)
                .setParameter("pricePolicyId", pricePolicyId)
                .setParameter("stock", stock)
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("save (단건)")
    class SaveSingle {

        @Test
        @DisplayName("재고를 저장한다")
        void shouldSaveInventory() {
            // given
            Inventory inventory = Inventory.of(1L, 100L);

            // when
            adapter.save(inventory);
            entityManager.flush();

            // then
            assertThat(inventoryJpaRepository.findByPricePolicyId(100L)).isPresent();
        }
    }

    @Nested
    @DisplayName("save (다건)")
    class SaveMultiple {

        @Test
        @DisplayName("여러 재고를 한번에 저장한다")
        void shouldSaveMultipleInventories() {
            // given
            Set<Inventory> inventories = Set.of(
                    Inventory.of(1L, 100L),
                    Inventory.of(1L, 200L)
            );

            // when
            adapter.save(inventories);
            entityManager.flush();

            // then
            assertThat(inventoryJpaRepository.findAll()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findByPricePolicyIds")
    class FindByPricePolicyIds {

        @Test
        @DisplayName("가격정책 ID 목록으로 재고를 조회한다")
        void shouldFindByPricePolicyIds() {
            // given
            persistInventory(1L, 100L, 50);
            persistInventory(1L, 200L, 30);
            persistInventory(2L, 300L, 20);

            // when
            Set<Inventory> found = adapter.findByPricePolicyIds(Set.of(100L, 200L));

            // then
            assertThat(found).hasSize(2);
            assertThat(found).allMatch(inv ->
                    inv.getPricePolicyId().equals(100L) || inv.getPricePolicyId().equals(200L)
            );
        }

        @Test
        @DisplayName("존재하지 않는 가격정책 ID로 조회 시 빈 Set을 반환한다")
        void shouldReturnEmptySetWhenNotFound() {
            // when
            Set<Inventory> found = adapter.findByPricePolicyIds(Set.of(999L));

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByProductIds")
    class FindByProductIds {

        @Test
        @DisplayName("상품 ID 목록으로 재고를 조회한다")
        void shouldFindByProductIds() {
            // given
            persistInventory(1L, 100L, 50);
            persistInventory(1L, 200L, 30);
            persistInventory(2L, 300L, 20);

            // when
            Set<Inventory> found = adapter.findByProductIds(Set.of(1L));

            // then
            assertThat(found).hasSize(2);
            assertThat(found).allMatch(inv -> inv.getProductId().equals(1L));
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 조회 시 빈 Set을 반환한다")
        void shouldReturnEmptySetWhenNotFound() {
            // when
            Set<Inventory> found = adapter.findByProductIds(Set.of(999L));

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByPricePolicyId")
    class ExistsByPricePolicyId {

        @Test
        @DisplayName("재고가 존재하면 true를 반환한다")
        void shouldReturnTrueWhenExists() {
            // given
            persistInventory(1L, 100L, 50);

            // when
            boolean exists = adapter.existsByPricePolicyId(100L);

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("재고가 존재하지 않으면 false를 반환한다")
        void shouldReturnFalseWhenNotExists() {
            // when
            boolean exists = adapter.existsByPricePolicyId(999L);

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("재고 수량을 업데이트한다")
        void shouldUpdateStock() {
            // given
            persistInventory(1L, 100L, 50);

            Inventory updated = Inventory.of(1L, 100L, 30, 0L);

            // when
            adapter.update(Set.of(updated));
            entityManager.flush();
            entityManager.clear();

            // then
            Set<Inventory> found = adapter.findByPricePolicyIds(Set.of(100L));
            assertThat(found).hasSize(1);
            assertThat(found.iterator().next().getStockValue()).isEqualTo(30);
        }

        @Test
        @DisplayName("존재하지 않는 재고를 업데이트하면 InventoryNotFoundException이 발생한다")
        void shouldThrowWhenInventoryNotFound() {
            // given
            Inventory nonExistent = Inventory.of(1L, 999L, 10, 0L);

            // when & then
            assertThatThrownBy(() -> adapter.update(Set.of(nonExistent)))
                    .isInstanceOf(InventoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("save (InventoryDeductionHistories)")
    class SaveDeductionHistories {

        @Test
        @DisplayName("재고 차감 이력을 저장한다")
        void shouldSaveDeductionHistories() {
            // given
            InventoryDeductionHistories histories = InventoryDeductionHistories.from(
                    Map.of(100L, 5, 200L, 3),
                    Map.of(100L, 1L, 200L, 1L),
                    1L,
                    "주문 결제 완료 차감"
            );

            // when
            adapter.save(histories);

            // then
            assertThat(deductionHistoryJpaRepository.findAll()).hasSize(2);
        }

        @Test
        @DisplayName("동일 주문의 중복 차감 이력 저장 시 DuplicateInventoryDeductionException이 발생한다")
        void shouldThrowOnDuplicateDeduction() {
            // given
            InventoryDeductionHistories histories = InventoryDeductionHistories.from(
                    Map.of(100L, 5),
                    Map.of(100L, 1L),
                    1L,
                    "주문 결제 완료 차감"
            );
            adapter.save(histories);

            InventoryDeductionHistories duplicate = InventoryDeductionHistories.from(
                    Map.of(100L, 5),
                    Map.of(100L, 1L),
                    1L,
                    "중복 차감 시도"
            );

            // when & then
            assertThatThrownBy(() -> adapter.save(duplicate))
                    .isInstanceOf(DuplicateInventoryDeductionException.class);
        }
    }

    @Nested
    @DisplayName("save (InventoryRestorationHistories)")
    class SaveRestorationHistories {

        @Test
        @DisplayName("재고 복원 이력을 저장한다")
        void shouldSaveRestorationHistories() {
            // given
            InventoryRestorationHistories histories = InventoryRestorationHistories.from(
                    Map.of(100L, 5, 200L, 3),
                    Map.of(100L, 1L, 200L, 1L),
                    1L,
                    "주문 취소 복원"
            );

            // when
            adapter.save(histories);

            // then
            assertThat(restorationHistoryJpaRepository.findAll()).hasSize(2);
        }

        @Test
        @DisplayName("동일 주문의 중복 복원 이력 저장 시 DuplicateInventoryRestorationException이 발생한다")
        void shouldThrowOnDuplicateRestoration() {
            // given
            InventoryRestorationHistories histories = InventoryRestorationHistories.from(
                    Map.of(100L, 5),
                    Map.of(100L, 1L),
                    1L,
                    "주문 취소 복원"
            );
            adapter.save(histories);

            InventoryRestorationHistories duplicate = InventoryRestorationHistories.from(
                    Map.of(100L, 5),
                    Map.of(100L, 1L),
                    1L,
                    "중복 복원 시도"
            );

            // when & then
            assertThatThrownBy(() -> adapter.save(duplicate))
                    .isInstanceOf(DuplicateInventoryRestorationException.class);
        }
    }
}
