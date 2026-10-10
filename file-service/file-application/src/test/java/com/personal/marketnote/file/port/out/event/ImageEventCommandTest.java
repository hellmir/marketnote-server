package com.personal.marketnote.file.port.out.event;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.FileDomainSnapshotState;
import com.personal.marketnote.file.domain.file.ResizedFile;
import com.personal.marketnote.file.domain.file.ResizedFileSnapshotState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DisplayName("ImageEventCommand 테스트")
class ImageEventCommandTest {

    @Test
    @DisplayName("ImageEventCommand.from()이 파일 하나에 매핑된 여러 ResizedFile을 올바르게 집계한다")
    void from_aggregatesMultipleResizedFilesForSingleFile() {
        // given
        FileDomain file = createFile(10L);
        ResizedFile resized600 = createResizedFile(1L, 10L, "600", "https://cdn.example.com/10_600.png");
        ResizedFile resized800 = createResizedFile(2L, 10L, "800", "https://cdn.example.com/10_800.png");
        ResizedFile unrelated = createResizedFile(3L, 99L, "500", "https://cdn.example.com/99_500.png");

        // when
        ImageEventCommand command = ImageEventCommand.from(file, List.of(resized600, resized800, unrelated));

        // then
        assertThat(command.imageId()).isEqualTo(10L);
        assertThat(command.resizedImages())
                .hasSize(2)
                .extracting(ImageEventCommand.ResizedImageCommand::size, ImageEventCommand.ResizedImageCommand::storageUrl)
                .containsExactly(
                        tuple("600", "https://cdn.example.com/10_600.png"),
                        tuple("800", "https://cdn.example.com/10_800.png")
                );
    }

    @Test
    @DisplayName("ImageEventCommand.from(FileDomain)은 리사이즈 URL 필드를 빈 배열로 반환한다")
    void from_withoutResizedFiles_returnsEmptyResizedImages() {
        FileDomain file = createFile(10L);

        ImageEventCommand command = ImageEventCommand.from(file);

        assertThat(command.resizedImages()).isEmpty();
    }

    private static FileDomain createFile(Long id) {
        return FileDomain.from(FileDomainSnapshotState.builder()
                .id(id)
                .ownerType(OwnerType.PRODUCT)
                .ownerId(100L)
                .sort(FileSort.PRODUCT_REPRESENTATIVE_IMAGE)
                .extension("png")
                .name("test.png")
                .storageUrl("https://cdn.example.com/test.png")
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0, 0))
                .status(EntityStatus.ACTIVE)
                .orderNum(1L)
                .userId(10L)
                .ownerKey("test-owner-key")
                .build());
    }

    private static ResizedFile createResizedFile(Long id, Long fileId, String size, String storageUrl) {
        return ResizedFile.from(ResizedFileSnapshotState.builder()
                .id(id)
                .fileId(fileId)
                .size(size)
                .storageUrl(storageUrl)
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0, 0))
                .status(EntityStatus.ACTIVE)
                .build());
    }
}
