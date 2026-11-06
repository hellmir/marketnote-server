package com.personal.marketnote.product.adapter.out.persistence.image.entity;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ImageReadModelJpaEntityTest {

    private ImageReadModelJpaEntity newEntity() {
        return ImageReadModelJpaEntity.of(
                100L, 200L, "PRODUCT",
                "MAIN", "https://img.example/a.png", 0
        );
    }

    @Nested
    @DisplayName("updateFrom status 보존")
    class StatusPreservation {

        @Test
        @DisplayName("ACTIVE 상태에서 updateFrom 호출 시 status가 ACTIVE로 유지된다")
        void shouldPreserveActiveStatus() {
            ImageReadModelJpaEntity entity = newEntity();
            assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);

            entity.updateFrom(300L, "REVIEW", "THUMBNAIL", "https://img.example/b.png", 1);

            assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);
            assertThat(entity.getTargetId()).isEqualTo(300L);
            assertThat(entity.getTargetType()).isEqualTo("REVIEW");
            assertThat(entity.getFileSort()).isEqualTo("THUMBNAIL");
            assertThat(entity.getImageUrl()).isEqualTo("https://img.example/b.png");
            assertThat(entity.getSortOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("INACTIVE 상태에서 updateFrom 호출 시 status가 INACTIVE로 유지된다")
        void shouldNotReactivateInactive() {
            ImageReadModelJpaEntity entity = newEntity();
            entity.markInactive();
            assertThat(entity.getStatus()).isEqualTo(EntityStatus.INACTIVE);

            entity.updateFrom(300L, "REVIEW", "THUMBNAIL", "https://img.example/b.png", 1);

            assertThat(entity.getStatus()).isEqualTo(EntityStatus.INACTIVE);
            assertThat(entity.getTargetId()).isEqualTo(300L);
            assertThat(entity.getTargetType()).isEqualTo("REVIEW");
            assertThat(entity.getFileSort()).isEqualTo("THUMBNAIL");
            assertThat(entity.getImageUrl()).isEqualTo("https://img.example/b.png");
            assertThat(entity.getSortOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("UNEXPOSED 상태에서 updateFrom 호출 시 status가 UNEXPOSED로 유지된다")
        void shouldNotReactivateUnexposed() {
            ImageReadModelJpaEntity entity = newEntity();
            ReflectionTestUtils.setField(entity, "status", EntityStatus.UNEXPOSED);
            assertThat(entity.getStatus()).isEqualTo(EntityStatus.UNEXPOSED);

            entity.updateFrom(300L, "REVIEW", "THUMBNAIL", "https://img.example/b.png", 1);

            assertThat(entity.getStatus()).isEqualTo(EntityStatus.UNEXPOSED);
            assertThat(entity.getTargetId()).isEqualTo(300L);
            assertThat(entity.getTargetType()).isEqualTo("REVIEW");
            assertThat(entity.getFileSort()).isEqualTo("THUMBNAIL");
            assertThat(entity.getImageUrl()).isEqualTo("https://img.example/b.png");
            assertThat(entity.getSortOrder()).isEqualTo(1);
        }
    }
}
