package com.personal.marketnote.file.domain.file;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FileDomainTest {

    @Nested
    @DisplayName("SnapshotState로 복원")
    class FromSnapshotStateTest {

        @Test
        @DisplayName("ACTIVE 상태의 FileDomain을 SnapshotState로 복원하면 isActive가 true를 반환한다")
        void snapshotState_withActiveStatus_isActiveReturnsTrue() {
            FileDomainSnapshotState state = FileDomainSnapshotState.builder()
                    .id(1L)
                    .ownerType(OwnerType.PRODUCT)
                    .ownerId(10L)
                    .sort(FileSort.PRODUCT_CATALOG_IMAGE)
                    .extension("jpg")
                    .name("test-image.jpg")
                    .storageUrl("https://cdn.example.com/test-image.jpg")
                    .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0, 0))
                    .status(EntityStatus.ACTIVE)
                    .orderNum(1L)
                    .build();

            FileDomain fileDomain = FileDomain.from(state);

            assertThat(fileDomain.isActive()).isTrue();
            assertThat(fileDomain.isInactive()).isFalse();
        }
    }

    @Nested
    @DisplayName("delete() 상태 전이")
    class DeleteTest {

        @Test
        @DisplayName("delete()를 호출하면 status가 INACTIVE로 전이되고 isInactive가 true를 반환한다")
        void delete_changesStatusToInactive() {
            FileDomainSnapshotState state = FileDomainSnapshotState.builder()
                    .id(1L)
                    .ownerType(OwnerType.PRODUCT)
                    .ownerId(10L)
                    .sort(FileSort.PRODUCT_CATALOG_IMAGE)
                    .extension("jpg")
                    .name("test-image.jpg")
                    .storageUrl("https://cdn.example.com/test-image.jpg")
                    .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0, 0))
                    .status(EntityStatus.ACTIVE)
                    .orderNum(1L)
                    .build();
            FileDomain fileDomain = FileDomain.from(state);

            fileDomain.delete();

            assertThat(fileDomain.isInactive()).isTrue();
            assertThat(fileDomain.isActive()).isFalse();
        }
    }
}
