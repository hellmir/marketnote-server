package com.personal.marketnote.file.adapter.out.persistence.file.entity;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.FileDomainSnapshotState;
import com.personal.marketnote.file.domain.file.ResizedFile;
import com.personal.marketnote.file.domain.file.ResizedFileSnapshotState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ResizedFileJpaEntity")
class ResizedFileJpaEntityTest {

    private FileJpaEntity buildFileEntity() {
        FileDomain domain = FileDomain.from(FileDomainSnapshotState.builder()
                .id(1L)
                .ownerType(OwnerType.PRODUCT)
                .ownerId(100L)
                .sort(FileSort.PRODUCT_CATALOG_IMAGE)
                .extension("png")
                .name("name.png")
                .status(EntityStatus.ACTIVE)
                .orderNum(1L)
                .userId(200L)
                .ownerKey("owner-key")
                .build());
        return FileJpaEntity.from(domain, "https://cdn.example.com/a.png");
    }

    @Nested
    @DisplayName("of(file, size)")
    class OfFileSize {

        @Test
        @DisplayName("file과 size만으로 생성하면 storageUrl은 null이다")
        void shouldCreateWithoutStorageUrl() {
            FileJpaEntity file = buildFileEntity();

            ResizedFileJpaEntity entity = ResizedFileJpaEntity.of(file, "MEDIUM");

            assertThat(entity.getFile()).isSameAs(file);
            assertThat(entity.getSize()).isEqualTo("MEDIUM");
            assertThat(entity.getStorageUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("of(file, size, storageUrl)")
    class OfFileSizeUrl {

        @Test
        @DisplayName("3개 인자로 생성하면 모든 필드가 매핑된다")
        void shouldMapAllFields() {
            FileJpaEntity file = buildFileEntity();

            ResizedFileJpaEntity entity = ResizedFileJpaEntity.of(file, "LARGE", "https://cdn.example.com/resized-L.png");

            assertThat(entity.getFile()).isSameAs(file);
            assertThat(entity.getSize()).isEqualTo("LARGE");
            assertThat(entity.getStorageUrl()).isEqualTo("https://cdn.example.com/resized-L.png");
        }
    }

    @Nested
    @DisplayName("updateFrom")
    class UpdateFrom {

        @Test
        @DisplayName("도메인 상태가 ACTIVE면 엔티티가 활성 상태로 유지된다")
        void shouldActivate() {
            FileJpaEntity file = buildFileEntity();
            ResizedFileJpaEntity entity = ResizedFileJpaEntity.of(file, "MEDIUM", "url");
            ResizedFile active = ResizedFile.from(
                    ResizedFileSnapshotState.builder()
                            .id(1L)
                            .fileId(1L)
                            .size("MEDIUM")
                            .storageUrl("url")
                            .status(EntityStatus.ACTIVE)
                            .build()
            );

            entity.updateFrom(active);

            assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("도메인 상태가 INACTIVE면 엔티티가 비활성화된다")
        void shouldDeactivate() {
            FileJpaEntity file = buildFileEntity();
            ResizedFileJpaEntity entity = ResizedFileJpaEntity.of(file, "MEDIUM", "url");
            ResizedFile inactive = ResizedFile.from(
                    ResizedFileSnapshotState.builder()
                            .id(1L)
                            .fileId(1L)
                            .size("MEDIUM")
                            .storageUrl("url")
                            .status(EntityStatus.INACTIVE)
                            .build()
            );

            entity.updateFrom(inactive);

            assertThat(entity.getStatus()).isEqualTo(EntityStatus.INACTIVE);
        }

        @Test
        @DisplayName("도메인 상태가 UNEXPOSED면 엔티티는 hide된다")
        void shouldHide() {
            FileJpaEntity file = buildFileEntity();
            ResizedFileJpaEntity entity = ResizedFileJpaEntity.of(file, "MEDIUM", "url");
            ResizedFile hidden = ResizedFile.from(
                    ResizedFileSnapshotState.builder()
                            .id(1L)
                            .fileId(1L)
                            .size("MEDIUM")
                            .storageUrl("url")
                            .status(EntityStatus.UNEXPOSED)
                            .build()
            );

            entity.updateFrom(hidden);

            assertThat(entity.getStatus()).isEqualTo(EntityStatus.UNEXPOSED);
        }
    }
}
