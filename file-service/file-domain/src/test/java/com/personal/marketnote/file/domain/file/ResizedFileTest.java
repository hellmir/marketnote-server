package com.personal.marketnote.file.domain.file;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ResizedFileTest {

    @Nested
    @DisplayName("SnapshotState로 복원")
    class FromSnapshotStateTest {

        @Test
        @DisplayName("ACTIVE 상태의 ResizedFile을 SnapshotState로 복원하면 isActive가 true를 반환한다")
        void snapshotState_withActiveStatus_isActiveReturnsTrue() {
            ResizedFileSnapshotState state = ResizedFileSnapshotState.builder()
                    .id(1L)
                    .fileId(10L)
                    .size("800x600")
                    .storageUrl("https://cdn.example.com/resized-800x600.jpg")
                    .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0, 0))
                    .status(EntityStatus.ACTIVE)
                    .build();

            ResizedFile resizedFile = ResizedFile.from(state);

            assertThat(resizedFile.isActive()).isTrue();
            assertThat(resizedFile.isInactive()).isFalse();
        }
    }

    @Nested
    @DisplayName("delete() 상태 전이")
    class DeleteTest {

        @Test
        @DisplayName("delete()를 호출하면 status가 INACTIVE로 전이되고 isInactive가 true를 반환한다")
        void delete_changesStatusToInactive() {
            ResizedFileSnapshotState state = ResizedFileSnapshotState.builder()
                    .id(1L)
                    .fileId(10L)
                    .size("800x600")
                    .storageUrl("https://cdn.example.com/resized-800x600.jpg")
                    .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0, 0))
                    .status(EntityStatus.ACTIVE)
                    .build();
            ResizedFile resizedFile = ResizedFile.from(state);

            resizedFile.delete();

            assertThat(resizedFile.isInactive()).isTrue();
            assertThat(resizedFile.isActive()).isFalse();
        }
    }
}
