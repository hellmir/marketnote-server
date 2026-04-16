package com.personal.marketnote.file.service.file;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.FileDomainSnapshotState;
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
@DisplayName("GetFileUseCase.getFiles(OwnerType, Long, String) 테스트")
class GetFilesOwnerTypeUseCaseTest {
    @InjectMocks private GetFileService getFileService;
    @Mock private FindFilePort findFilePort;
    @Mock private FindResizedFilesPort findResizedFilesPort;

    @Nested
    @DisplayName("파일 목록 조회 (OwnerType)")
    class GetFilesWithOwnerTypeTest {
        @Test
        @DisplayName("sort가 있으면 소유자와 sort로 파일을 조회한다")
        void shouldReturnFilesByOwnerAndSort() {
            FileDomain file = buildFile(1L);
            when(findFilePort.findByOwnerAndSort(OwnerType.PRODUCT, 100L, FileSort.PRODUCT_CATALOG_IMAGE)).thenReturn(List.of(file));
            List<FileDomain> result = getFileService.getFiles(OwnerType.PRODUCT, 100L, "PRODUCT_CATALOG_IMAGE");
            assertThat(result).hasSize(1);
            verify(findFilePort).findByOwnerAndSort(OwnerType.PRODUCT, 100L, FileSort.PRODUCT_CATALOG_IMAGE);
        }

        @Test
        @DisplayName("sort가 없으면 소유자로만 파일을 조회한다")
        void shouldReturnFilesByOwnerOnlyWhenSortIsNull() {
            FileDomain file = buildFile(1L);
            when(findFilePort.findByOwner(OwnerType.PRODUCT, 100L)).thenReturn(List.of(file));
            List<FileDomain> result = getFileService.getFiles(OwnerType.PRODUCT, 100L, null);
            assertThat(result).hasSize(1);
            verify(findFilePort).findByOwner(OwnerType.PRODUCT, 100L);
        }
    }

    private FileDomain buildFile(Long id) {
        return FileDomain.from(FileDomainSnapshotState.builder()
                .id(id).ownerType(OwnerType.PRODUCT).ownerId(100L).sort(FileSort.PRODUCT_CATALOG_IMAGE)
                .extension("jpg").name("test-file.jpg").storageUrl("https://example.com/test-file.jpg")
                .createdAt(LocalDateTime.now()).status(EntityStatus.ACTIVE).orderNum(1L).build());
    }
}
