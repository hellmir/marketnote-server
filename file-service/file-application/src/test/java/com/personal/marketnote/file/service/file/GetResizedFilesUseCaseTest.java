package com.personal.marketnote.file.service.file;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.FileDomainSnapshotState;
import com.personal.marketnote.file.domain.file.ResizedFile;
import com.personal.marketnote.file.domain.file.ResizedFileSnapshotState;
import com.personal.marketnote.file.port.out.file.FindFilePort;
import com.personal.marketnote.file.port.out.resized.FindResizedFilesPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetFileUseCase.getResizedFiles 테스트")
class GetResizedFilesUseCaseTest {
    @InjectMocks
    private GetFileService getFileService;
    @Mock
    private FindFilePort findFilePort;
    @Mock
    private FindResizedFilesPort findResizedFilesPort;

    @Nested
    @DisplayName("리사이즈 파일 조회")
    class GetResizedFilesTest {
        @Test
        @DisplayName("파일 목록에 대한 리사이즈 파일이 존재하면 리사이즈 파일 목록을 반환한다")
        void shouldReturnResizedFilesForGivenFiles() {
            FileDomain file1 = buildFile(1L);
            FileDomain file2 = buildFile(2L);
            ResizedFile resized1 = ResizedFile.from(ResizedFileSnapshotState.builder()
                    .fileId(1L).size("SMALL").storageUrl("https://example.com/resized-1.jpg")
                    .status(EntityStatus.ACTIVE).build());
            ResizedFile resized2 = ResizedFile.from(ResizedFileSnapshotState.builder()
                    .fileId(2L).size("SMALL").storageUrl("https://example.com/resized-2.jpg")
                    .status(EntityStatus.ACTIVE).build());
            when(findResizedFilesPort.findByFileIds(List.of(1L, 2L))).thenReturn(List.of(resized1, resized2));
            List<ResizedFile> result = getFileService.getResizedFiles(List.of(file1, file2));
            assertThat(result).hasSize(2);
            verify(findResizedFilesPort).findByFileIds(List.of(1L, 2L));
        }

        @Test
        @DisplayName("리사이즈 파일이 없으면 빈 목록을 반환한다")
        void shouldReturnEmptyWhenNoResizedFiles() {
            FileDomain file = buildFile(1L);
            when(findResizedFilesPort.findByFileIds(List.of(1L))).thenReturn(List.of());
            List<ResizedFile> result = getFileService.getResizedFiles(List.of(file));
            assertThat(result).isEmpty();
            verify(findResizedFilesPort).findByFileIds(List.of(1L));
        }
    }

    private FileDomain buildFile(Long id) {
        return FileDomain.from(FileDomainSnapshotState.builder()
                .id(id).ownerType(OwnerType.PRODUCT).ownerId(100L).sort(FileSort.PRODUCT_CATALOG_IMAGE)
                .extension("jpg").name("test-file-" + id + ".jpg").storageUrl("https://example.com/test-file-" + id + ".jpg")
                .createdAt(LocalDateTime.now()).status(EntityStatus.ACTIVE).orderNum(id).build());
    }
}
