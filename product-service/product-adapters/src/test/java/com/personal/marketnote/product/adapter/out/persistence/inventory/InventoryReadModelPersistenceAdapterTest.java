package com.personal.marketnote.product.adapter.out.persistence.inventory;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.product.adapter.out.persistence.inventory.entity.InventoryReadModelJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.inventory.repository.InventoryReadModelJpaRepository;
import com.personal.marketnote.product.port.out.result.GetInventoryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, InventoryReadModelPersistenceAdapter.class})
class InventoryReadModelPersistenceAdapterTest {

    @Autowired
    private InventoryReadModelPersistenceAdapter adapter;

    @Autowired
    private InventoryReadModelJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("upsert")
    class Upsert {

        @Test
        @DisplayName("신규 재고를 저장한다")
        void insertsNewInventory() {
            // when
            adapter.upsert(1L, 100L, 50);

            // then
            Optional<InventoryReadModelJpaEntity> entity = repository.findByPricePolicyId(1L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getPricePolicyId()).isEqualTo(1L);
            assertThat(entity.get().getProductId()).isEqualTo(100L);
            assertThat(entity.get().getStockQuantity()).isEqualTo(50);
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("동일한 pricePolicyId로 upsert 시 기존 stockQuantity를 업데이트한다")
        void updatesExistingInventory() {
            // given
            adapter.upsert(1L, 100L, 50);

            // when
            adapter.upsert(1L, 100L, 30);

            // then
            Optional<InventoryReadModelJpaEntity> entity = repository.findByPricePolicyId(1L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getStockQuantity()).isEqualTo(30);
        }

        @Test
        @DisplayName("비활성화된 재고에 대해 upsert 시 다시 활성화된다")
        void reactivatesInactiveInventory() {
            // given
            adapter.upsert(1L, 100L, 50);
            InventoryReadModelJpaEntity savedEntity = repository.findByPricePolicyId(1L).get();
            savedEntity.markInactive();
            repository.saveAndFlush(savedEntity);

            // when
            adapter.upsert(1L, 100L, 40);

            // then
            Optional<InventoryReadModelJpaEntity> entity = repository.findByPricePolicyId(1L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
            assertThat(entity.get().getStockQuantity()).isEqualTo(40);
        }
    }

    @Nested
    @DisplayName("findByPricePolicyIds")
    class FindByPricePolicyIds {

        @Test
        @DisplayName("ACTIVE 상태의 재고를 조회한다")
        void returnsActiveInventories() {
            // given
            adapter.upsert(1L, 100L, 50);
            adapter.upsert(2L, 200L, 30);

            // when
            Set<GetInventoryResult> results = adapter.findByPricePolicyIds(List.of(1L, 2L));

            // then
            assertThat(results).hasSize(2);
            assertThat(results).anySatisfy(result -> {
                assertThat(result.pricePolicyId()).isEqualTo(1L);
                assertThat(result.stock()).isEqualTo(50);
            });
            assertThat(results).anySatisfy(result -> {
                assertThat(result.pricePolicyId()).isEqualTo(2L);
                assertThat(result.stock()).isEqualTo(30);
            });
        }

        @Test
        @DisplayName("Read Model에 없는 pricePolicyId는 stock이 null인 기본값을 반환한다")
        void returnsDefaultForMissingPricePolicyIds() {
            // given
            adapter.upsert(1L, 100L, 50);

            // when
            Set<GetInventoryResult> results = adapter.findByPricePolicyIds(List.of(1L, 999L));

            // then
            assertThat(results).hasSize(2);
            assertThat(results).anySatisfy(result -> {
                assertThat(result.pricePolicyId()).isEqualTo(1L);
                assertThat(result.stock()).isEqualTo(50);
            });
            assertThat(results).anySatisfy(result -> {
                assertThat(result.pricePolicyId()).isEqualTo(999L);
                assertThat(result.stock()).isNull();
            });
        }

        @Test
        @DisplayName("INACTIVE 상태의 재고는 조회하지 않고 기본값을 반환한다")
        void excludesInactiveInventories() {
            // given
            adapter.upsert(1L, 100L, 50);
            InventoryReadModelJpaEntity savedEntity = repository.findByPricePolicyId(1L).get();
            savedEntity.markInactive();
            repository.saveAndFlush(savedEntity);

            // when
            Set<GetInventoryResult> results = adapter.findByPricePolicyIds(List.of(1L));

            // then
            assertThat(results).hasSize(1);
            assertThat(results).anySatisfy(result -> {
                assertThat(result.pricePolicyId()).isEqualTo(1L);
                assertThat(result.stock()).isNull();
            });
        }

        @Test
        @DisplayName("모든 pricePolicyId가 Read Model에 없으면 모두 기본값을 반환한다")
        void returnsAllDefaultsWhenNoneExist() {
            // when
            Set<GetInventoryResult> results = adapter.findByPricePolicyIds(List.of(10L, 20L, 30L));

            // then
            assertThat(results).hasSize(3);
            results.forEach(result -> assertThat(result.stock()).isNull());
        }
    }
}
