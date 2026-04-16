package com.personal.marketnote.file.service.file;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.FileDomainSnapshotState;
import com.personal.marketnote.file.exception.FileNotFoundException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetFileUseCase.getFile 테스트")
class GetFileUseCaseTest {
    @InjectMocks
    private GetFileService getFileService;
    @Mock
    private FindFilePort findFilePort;
    @Mock
    private FindResizedFilesPort findResizedFilesPort;

    @Nested
    @DisplayName("파일 단건 조회")
    class GetFileTest {
        @Test
        @DisplayName("파일이 존재하면 파일 도메인을 반환한다")
        void shouldReturnFileWhenExists() {
            Long fileId = 1L;
            FileDomain file = buildFile(fileId);
            when(findFilePort.findById(fileId)).thenReturn(Optional.of(file));
            FileDomain result = getFileService.getFile(fileId);
            assertThat(result.getId()).isEqualTo(fileId);
            assertThat(result.getName()).isEqualTo("test-file.jpg");
            verify(findFilePort).findById(fileId);
        }

        @Test
        @DisplayName("파일이 존재하지 않으면 FileNotFoundException이 발생한다")
        void shouldThrowWhenFileNotFound() {
            Long fileId = 999L;
            when(findFilePort.findById(fileId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> getFileService.getFile(fileId)).isInstanceOf(FileNotFoundException.class);
            verify(findFilePort).findById(fileId);
        }
    }

    private FileDomain buildFile(Long id) {
        return FileDomain.from(FileDomainSnapshotState.builder()
                .id(id).ownerType(OwnerType.PRODUCT).ownerId(100L).sort(FileSort.PRODUCT_CATALOG_IMAGE)
                .extension("jpg").name("test-file.jpg").storageUrl("https://example.com/test-file.jpg")
                .createdAt(LocalDateTime.now()).status(EntityStatus.ACTIVE).orderNum(1L).build());
    }
}
