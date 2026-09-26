package com.personal.marketnote.file.domain.file;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.domain.file.exception.InvalidFileOwnerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Nested
    @DisplayName("validateOwner() 소유권 검증")
    class ValidateOwnerTest {

        @Test
        @DisplayName("userId와 ownerKey가 모두 일치하면 예외를 던지지 않는다")
        void validateOwner_matched_doesNotThrow() {
            FileDomain fileDomain = snapshotFileDomain(100L, "key-abc");

            assertThatCode(() -> fileDomain.validateOwner(100L, "key-abc"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("userId가 불일치하면 InvalidFileOwnerException을 던진다")
        void validateOwner_userIdMismatch_throws() {
            FileDomain fileDomain = snapshotFileDomain(100L, "key-abc");

            assertThatThrownBy(() -> fileDomain.validateOwner(999L, "key-abc"))
                    .isInstanceOf(InvalidFileOwnerException.class);
        }

        @Test
        @DisplayName("ownerKey가 불일치하면 InvalidFileOwnerException을 던진다")
        void validateOwner_ownerKeyMismatch_throws() {
            FileDomain fileDomain = snapshotFileDomain(100L, "key-abc");

            assertThatThrownBy(() -> fileDomain.validateOwner(100L, "key-xyz"))
                    .isInstanceOf(InvalidFileOwnerException.class);
        }

        @Test
        @DisplayName("기존 userId가 null이면 InvalidFileOwnerException을 던진다")
        void validateOwner_existingUserIdNull_throws() {
            FileDomain fileDomain = snapshotFileDomain(null, "key-abc");

            assertThatThrownBy(() -> fileDomain.validateOwner(100L, "key-abc"))
                    .isInstanceOf(InvalidFileOwnerException.class);
        }

        @Test
        @DisplayName("기존 ownerKey가 null이면 InvalidFileOwnerException을 던진다")
        void validateOwner_existingOwnerKeyNull_throws() {
            FileDomain fileDomain = snapshotFileDomain(100L, null);

            assertThatThrownBy(() -> fileDomain.validateOwner(100L, "key-abc"))
                    .isInstanceOf(InvalidFileOwnerException.class);
        }
    }

    @Nested
    @DisplayName("CreateState로 생성")
    class FromCreateStateTest {

        @Test
        @DisplayName("userId와 ownerKey가 모두 있으면 정상 생성된다")
        void fromCreateState_withUserIdAndOwnerKey_createsSuccessfully() {
            FileDomainCreateState state = baseCreateStateBuilder()
                    .userId(100L)
                    .ownerKey("key-abc")
                    .build();

            FileDomain fileDomain = FileDomain.from(state);

            assertThat(fileDomain.getUserId()).isEqualTo(100L);
            assertThat(fileDomain.getOwnerKey()).isEqualTo("key-abc");
        }

        @Test
        @DisplayName("userId가 null이면 InvalidFileOwnerException을 던진다")
        void fromCreateState_userIdNull_throws() {
            FileDomainCreateState state = baseCreateStateBuilder()
                    .userId(null)
                    .ownerKey("key-abc")
                    .build();

            assertThatThrownBy(() -> FileDomain.from(state))
                    .isInstanceOf(InvalidFileOwnerException.class);
        }

        @Test
        @DisplayName("ownerKey가 null이면 InvalidFileOwnerException을 던진다")
        void fromCreateState_ownerKeyNull_throws() {
            FileDomainCreateState state = baseCreateStateBuilder()
                    .userId(100L)
                    .ownerKey(null)
                    .build();

            assertThatThrownBy(() -> FileDomain.from(state))
                    .isInstanceOf(InvalidFileOwnerException.class);
        }

        private FileDomainCreateState.FileDomainCreateStateBuilder baseCreateStateBuilder() {
            return FileDomainCreateState.builder()
                    .ownerType(OwnerType.PRODUCT)
                    .ownerId(10L)
                    .sort(FileSort.PRODUCT_CATALOG_IMAGE)
                    .extension("jpg")
                    .name("test-image.jpg");
        }
    }

    private static FileDomain snapshotFileDomain(Long userId, String ownerKey) {
        return FileDomain.from(FileDomainSnapshotState.builder()
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
                .userId(userId)
                .ownerKey(ownerKey)
                .build());
    }
}
