package com.personal.marketnote.file.service.file;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.FileDomainSnapshotState;
import com.personal.marketnote.file.domain.file.ResizedFile;
import com.personal.marketnote.file.domain.file.ResizedFileSnapshotState;
import com.personal.marketnote.file.port.in.usecase.file.GetFileUseCase;
import com.personal.marketnote.file.port.out.event.ImageEventCommand;
import com.personal.marketnote.file.port.out.event.PublishImageEventPort;
import com.personal.marketnote.file.port.out.file.UpdateFilePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
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

    @Test
    @DisplayName("파일 삭제 API 호출 시 발행되는 삭제 이벤트 페이로드에 리사이즈 URL이 포함된다")
    @SuppressWarnings("unchecked")
    void delete_publishesDeletedEventWithResizedUrls() {
        // given
        Long fileId = 2L;
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
        ResizedFile resized600 = ResizedFile.from(ResizedFileSnapshotState.builder()
                .id(10L)
                .fileId(fileId)
                .size("600")
                .storageUrl("https://cdn.example.com/test_600.png")
                .createdAt(LocalDateTime.of(2026, 3, 27, 10, 0))
                .status(EntityStatus.ACTIVE)
                .build());
        ResizedFile resized800 = ResizedFile.from(ResizedFileSnapshotState.builder()
                .id(11L)
                .fileId(fileId)
                .size("800")
                .storageUrl("https://cdn.example.com/test_800.png")
                .createdAt(LocalDateTime.of(2026, 3, 27, 10, 0))
                .status(EntityStatus.ACTIVE)
                .build());
        when(getFileUseCase.getFile(fileId)).thenReturn(file);
        when(getFileUseCase.getResizedFiles(anyList())).thenReturn(List.of(resized600, resized800));

        // when
        deleteFileService.delete(fileId);

        // then
        ArgumentCaptor<List<ImageEventCommand>> captor = ArgumentCaptor.forClass(List.class);
        verify(publishImageEventPort).publishImageDeletedEvents(captor.capture());
        List<ImageEventCommand> published = captor.getValue();
        assertThat(published).hasSize(1);
        assertThat(published.getFirst().imageId()).isEqualTo(fileId);
        assertThat(published.getFirst().resizedImages()).hasSize(2);
        assertThat(published.getFirst().resizedImages())
                .extracting(ImageEventCommand.ResizedImageCommand::size)
                .containsExactly("600", "800");
        assertThat(published.getFirst().resizedImages())
                .extracting(ImageEventCommand.ResizedImageCommand::storageUrl)
                .containsExactly(
                        "https://cdn.example.com/test_600.png",
                        "https://cdn.example.com/test_800.png"
                );
    }
}
