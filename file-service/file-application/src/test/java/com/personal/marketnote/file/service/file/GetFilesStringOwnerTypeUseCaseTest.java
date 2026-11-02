package com.personal.marketnote.file.service.file;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.FileDomainSnapshotState;
import com.personal.marketnote.file.domain.file.ResizedFile;
import com.personal.marketnote.file.domain.file.ResizedFileSnapshotState;
import com.personal.marketnote.file.port.in.result.GetFilesResult;
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
@DisplayName("GetFileUseCase.getFiles(String, Long, String) 테스트")
class GetFilesStringOwnerTypeUseCaseTest {
    @InjectMocks
    private GetFileService getFileService;
    @Mock
    private FindFilePort findFilePort;
    @Mock
    private FindResizedFilesPort findResizedFilesPort;

    @Nested
    @DisplayName("파일 목록 조회 (문자열 소유자 타입)")
    class GetFilesWithStringOwnerTypeTest {
        @Test
        @DisplayName("파일이 존재하면 리사이즈 URL을 포함한 결과를 반환한다")
        void shouldReturnFilesWithResizedUrls() {
            FileDomain file = buildFile(1L);
            ResizedFile resizedFile = ResizedFile.from(ResizedFileSnapshotState.builder()
                    .fileId(1L).size("SMALL").storageUrl("https://example.com/resized.jpg")
                    .status(EntityStatus.ACTIVE).build());
            when(findFilePort.findByOwnerAndSort(OwnerType.PRODUCT, 100L, FileSort.PRODUCT_CATALOG_IMAGE)).thenReturn(List.of(file));
            when(findResizedFilesPort.findByFileIds(List.of(1L))).thenReturn(List.of(resizedFile));
            GetFilesResult result = getFileService.getFiles("PRODUCT", 100L, "PRODUCT_CATALOG_IMAGE");
            assertThat(result.files()).hasSize(1);
            assertThat(result.files().get(0).resizedStorageUrls()).contains("https://example.com/resized.jpg");
            verify(findFilePort).findByOwnerAndSort(OwnerType.PRODUCT, 100L, FileSort.PRODUCT_CATALOG_IMAGE);
        }

        @Test
        @DisplayName("파일이 없으면 빈 결과를 반환한다")
        void shouldReturnEmptyWhenNoFiles() {
            when(findFilePort.findByOwnerAndSort(OwnerType.PRODUCT, 100L, FileSort.PRODUCT_CATALOG_IMAGE)).thenReturn(List.of());
            when(findResizedFilesPort.findByFileIds(List.of())).thenReturn(List.of());
            GetFilesResult result = getFileService.getFiles("PRODUCT", 100L, "PRODUCT_CATALOG_IMAGE");
            assertThat(result.files()).isEmpty();
        }
    }

    private FileDomain buildFile(Long id) {
        return FileDomain.from(FileDomainSnapshotState.builder()
                .id(id).ownerType(OwnerType.PRODUCT).ownerId(100L).sort(FileSort.PRODUCT_CATALOG_IMAGE)
                .extension("jpg").name("test-file.jpg").storageUrl("https://example.com/test-file.jpg")
                .createdAt(LocalDateTime.now()).status(EntityStatus.ACTIVE).orderNum(1L).build());
    }
}
