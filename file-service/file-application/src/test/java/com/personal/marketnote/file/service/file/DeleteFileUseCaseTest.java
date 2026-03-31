package com.personal.marketnote.file.service.file;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.FileDomainSnapshotState;
import com.personal.marketnote.file.port.in.usecase.file.GetFileUseCase;
import com.personal.marketnote.file.port.out.event.PublishImageEventPort;
import com.personal.marketnote.file.port.out.file.UpdateFilePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteFileUseCaseTest {
    @InjectMocks
    private DeleteFileService deleteFileService;

    @Mock
    private GetFileUseCase getFileUseCase;

    @Mock
    private UpdateFilePort updateFilePort;

    @Mock
    private PublishImageEventPort publishImageEventPort;

    @Test
    @DisplayName("파일 삭제 시 DELETED 이벤트를 발행한다")
    void delete_publishesDeletedEvent() {
        // given
        Long fileId = 1L;
        FileDomain file = FileDomain.from(FileDomainSnapshotState.builder()
                .id(fileId)
                .ownerType(OwnerType.PRODUCT)
                .ownerId(100L)
                .sort(FileSort.PRODUCT_REPRESENTATIVE_IMAGE)
                .extension("png")
                .name("test.png")
                .storageUrl("https://cdn.example.com/test.png")
                .createdAt(LocalDateTime.of(2026, 3, 27, 10, 0))
                .status(EntityStatus.ACTIVE)
                .orderNum(1L)
                .build());
        when(getFileUseCase.getFile(fileId)).thenReturn(file);

        // when
        deleteFileService.delete(fileId);

        // then
        verify(updateFilePort).update(file);
        verify(publishImageEventPort).publishImageDeletedEvents(argThat(events ->
                events.size() == 1
                        && events.getFirst().imageId().equals(fileId)
                        && events.getFirst().targetId().equals(100L)
                        && events.getFirst().targetType().equals("PRODUCT")
                        && events.getFirst().imageUrl().equals("https://cdn.example.com/test.png")
        ));
    }
}
