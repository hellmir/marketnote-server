package com.personal.marketnote.product.adapter.out.persistence.review.entity;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewAggregateReadModelJpaEntityTest {

    private ReviewAggregateReadModelJpaEntity newEntity() {
        return ReviewAggregateReadModelJpaEntity.of(100L, 10, new BigDecimal("4.50"));
    }

    @Nested
    @DisplayName("updateFrom status 보존")
    class StatusPreservation {

        @Test
        @DisplayName("ACTIVE 상태에서 updateFrom 호출 시 status가 ACTIVE로 유지된다")
        void shouldPreserveActiveStatus() {
            ReviewAggregateReadModelJpaEntity entity = newEntity();
            assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);

            entity.updateFrom(20, new BigDecimal("3.00"));

            assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);
            assertThat(entity.getTotalCount()).isEqualTo(20);
            assertThat(entity.getAverageRating()).isEqualByComparingTo(new BigDecimal("3.00"));
        }

        @Test
        @DisplayName("INACTIVE 상태에서 updateFrom 호출 시 status가 INACTIVE로 유지된다")
        void shouldNotReactivateInactive() {
            ReviewAggregateReadModelJpaEntity entity = newEntity();
            entity.markInactive();
            assertThat(entity.getStatus()).isEqualTo(EntityStatus.INACTIVE);

            entity.updateFrom(20, new BigDecimal("3.00"));

            assertThat(entity.getStatus()).isEqualTo(EntityStatus.INACTIVE);
            assertThat(entity.getTotalCount()).isEqualTo(20);
            assertThat(entity.getAverageRating()).isEqualByComparingTo(new BigDecimal("3.00"));
        }

        @Test
        @DisplayName("UNEXPOSED 상태에서 updateFrom 호출 시 status가 UNEXPOSED로 유지된다")
        void shouldNotReactivateUnexposed() {
            ReviewAggregateReadModelJpaEntity entity = newEntity();
            ReflectionTestUtils.setField(entity, "status", EntityStatus.UNEXPOSED);
            assertThat(entity.getStatus()).isEqualTo(EntityStatus.UNEXPOSED);

            entity.updateFrom(20, new BigDecimal("3.00"));

            assertThat(entity.getStatus()).isEqualTo(EntityStatus.UNEXPOSED);
            assertThat(entity.getTotalCount()).isEqualTo(20);
            assertThat(entity.getAverageRating()).isEqualByComparingTo(new BigDecimal("3.00"));
        }
    }
}
