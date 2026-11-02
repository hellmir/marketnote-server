package com.personal.marketnote.file.adapter.out.persistence.file.entity;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.FileDomainSnapshotState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FileJpaEntity")
class FileJpaEntityTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 20, 10, 0);

    private FileDomain buildDomain(EntityStatus status, String storageUrl) {
        return FileDomain.from(FileDomainSnapshotState.builder()
                .id(1L)
                .ownerType(OwnerType.PRODUCT)
                .ownerId(100L)
                .sort(FileSort.PRODUCT_CATALOG_IMAGE)
                .extension("png")
                .name("image.png")
                .storageUrl(storageUrl)
                .createdAt(NOW)
                .status(status)
                .orderNum(1L)
                .userId(200L)
                .ownerKey("owner-key")
                .build());
    }

    @Nested
    @DisplayName("from")
    class From {

        @Test
        @DisplayName("도메인 + storageUrl로 엔티티를 생성하면 모든 필드가 매핑된다")
        void shouldMapAllFieldsFromDomain() {
            FileDomain domain = buildDomain(EntityStatus.ACTIVE, "ignored-url");

            FileJpaEntity entity = FileJpaEntity.from(domain, "https://cdn.example.com/a.png");

            assertThat(entity.getOwnerType()).isEqualTo(OwnerType.PRODUCT);
            assertThat(entity.getOwnerId()).isEqualTo(100L);
            assertThat(entity.getSort()).isEqualTo(FileSort.PRODUCT_CATALOG_IMAGE.name());
            assertThat(entity.getExtension()).isEqualTo("png");
            assertThat(entity.getName()).isEqualTo("image.png");
            assertThat(entity.getStorageUrl()).isEqualTo("https://cdn.example.com/a.png");
            assertThat(entity.getUserId()).isEqualTo(200L);
            assertThat(entity.getOwnerKey()).isEqualTo("owner-key");
        }
    }

    @Nested
    @DisplayName("updateFrom")
    class UpdateFrom {

        @Test
        @DisplayName("도메인으로 업데이트하면 도메인의 storageUrl이 그대로 유지되어 엔티티에 반영된다")
        void shouldRetainDomainStorageUrl() {
            FileDomain original = buildDomain(EntityStatus.ACTIVE, "https://cdn.example.com/orig.png");
            FileJpaEntity entity = FileJpaEntity.from(original, "https://cdn.example.com/orig.png");
            FileDomain updated = FileDomain.from(FileDomainSnapshotState.builder()
                    .id(1L)
                    .ownerType(OwnerType.REVIEW)
                    .ownerId(300L)
                    .sort(FileSort.REVIEW_IMAGE)
                    .extension("jpg")
                    .name("review.jpg")
                    .storageUrl("https://cdn.example.com/updated.jpg")
                    .status(EntityStatus.ACTIVE)
                    .orderNum(2L)
                    .userId(500L)
                    .ownerKey("updated-owner-key")
                    .build());

            entity.updateFrom(updated);

            assertThat(entity.getStorageUrl()).isEqualTo("https://cdn.example.com/updated.jpg");
            assertThat(entity.getName()).isEqualTo("review.jpg");
            assertThat(entity.getExtension()).isEqualTo("jpg");
            assertThat(entity.getSort()).isEqualTo(FileSort.REVIEW_IMAGE.name());
            assertThat(entity.getOwnerType()).isEqualTo(OwnerType.REVIEW);
            assertThat(entity.getOwnerId()).isEqualTo(300L);
            assertThat(entity.getOrderNum()).isEqualTo(2L);
            assertThat(entity.getUserId()).isEqualTo(500L);
            assertThat(entity.getOwnerKey()).isEqualTo("updated-owner-key");
        }

        @Test
        @DisplayName("도메인 status가 INACTIVE면 엔티티도 비활성화된다")
        void shouldDeactivateWhenDomainInactive() {
            FileDomain active = buildDomain(EntityStatus.ACTIVE, "a");
            FileJpaEntity entity = FileJpaEntity.from(active, "a");
            FileDomain inactive = buildDomain(EntityStatus.INACTIVE, "a");

            entity.updateFrom(inactive);

            assertThat(entity.getStatus()).isEqualTo(EntityStatus.INACTIVE);
        }

        @Test
        @DisplayName("도메인 status가 UNEXPOSED면 엔티티는 hide된다")
        void shouldHideWhenDomainUnexposed() {
            FileDomain active = buildDomain(EntityStatus.ACTIVE, "a");
            FileJpaEntity entity = FileJpaEntity.from(active, "a");
            FileDomain hidden = buildDomain(EntityStatus.UNEXPOSED, "a");

            entity.updateFrom(hidden);

            assertThat(entity.getStatus()).isEqualTo(EntityStatus.UNEXPOSED);
        }
    }
}
