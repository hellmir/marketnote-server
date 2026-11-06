package com.personal.marketnote.product.adapter.out.persistence.inventory.entity;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryReadModelJpaEntityTest {

    private InventoryReadModelJpaEntity newEntity() {
        return InventoryReadModelJpaEntity.of(100L, 200L, 50);
    }

    @Nested
    @DisplayName("updateFrom status 보존")
    class StatusPreservation {

        @Test
        @DisplayName("ACTIVE 상태에서 updateFrom 호출 시 status가 ACTIVE로 유지된다")
        void shouldPreserveActiveStatus() {
            InventoryReadModelJpaEntity entity = newEntity();
            assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);

            entity.updateFrom(77);

            assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);
            assertThat(entity.getStockQuantity()).isEqualTo(77);
        }

        @Test
        @DisplayName("INACTIVE 상태에서 updateFrom 호출 시 status가 INACTIVE로 유지된다")
        void shouldNotReactivateInactive() {
            InventoryReadModelJpaEntity entity = newEntity();
            entity.markInactive();
            assertThat(entity.getStatus()).isEqualTo(EntityStatus.INACTIVE);

            entity.updateFrom(77);

            assertThat(entity.getStatus()).isEqualTo(EntityStatus.INACTIVE);
            assertThat(entity.getStockQuantity()).isEqualTo(77);
        }

        @Test
        @DisplayName("UNEXPOSED 상태에서 updateFrom 호출 시 status가 UNEXPOSED로 유지된다")
        void shouldNotReactivateUnexposed() {
            InventoryReadModelJpaEntity entity = newEntity();
            ReflectionTestUtils.setField(entity, "status", EntityStatus.UNEXPOSED);
            assertThat(entity.getStatus()).isEqualTo(EntityStatus.UNEXPOSED);

            entity.updateFrom(77);

            assertThat(entity.getStatus()).isEqualTo(EntityStatus.UNEXPOSED);
            assertThat(entity.getStockQuantity()).isEqualTo(77);
        }
    }
}
