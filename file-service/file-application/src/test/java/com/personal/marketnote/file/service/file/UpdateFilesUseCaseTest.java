package com.personal.marketnote.file.service.file;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.FileDomainSnapshotState;
import com.personal.marketnote.file.domain.file.exception.InvalidFileOwnerException;
import com.personal.marketnote.file.exception.InvalidFileCountLimitException;
import com.personal.marketnote.file.port.in.command.UpdateFileCommand;
import com.personal.marketnote.file.port.in.command.UpdateFilesCommand;
import com.personal.marketnote.file.port.in.usecase.file.GetFileUseCase;
import com.personal.marketnote.file.port.out.event.PublishImageEventPort;
import com.personal.marketnote.file.port.out.file.SaveFilesPort;
import com.personal.marketnote.file.port.out.file.UpdateFilesPort;
import com.personal.marketnote.file.port.out.resized.SaveResizedFilesPort;
import com.personal.marketnote.file.port.out.storage.UploadFilesPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateFilesUseCaseTest {
    @Mock
    private GetFileUseCase getFileUseCase;
    @Mock
    private UploadFilesPort uploadFilesPort;
    @Mock
    private SaveFilesPort saveFilesPort;
    @Mock
    private SaveResizedFilesPort saveResizedFilesPort;
    @Mock
    private UpdateFilesPort updateFilesPort;
    @Mock
    private PublishImageEventPort publishImageEventPort;

    @InjectMocks
    private UpdateFilesService updateFilesService;

    @Test
    @DisplayName("대표 이미지 9개 업로드 시 InvalidFileCountLimitException 예외를 던진다")
    void updateFiles_representativeImage_exceedsMaxCount_throws() {
        UpdateFilesCommand command = buildCommand(
                FileSort.PRODUCT_REPRESENTATIVE_IMAGE.name(), 9
        );

        assertThatThrownBy(() -> updateFilesService.updateFiles(command))
                .isInstanceOf(InvalidFileCountLimitException.class)
                .hasMessageContaining("8");

        verifyNoInteractions(uploadFilesPort, saveFilesPort, saveResizedFilesPort);
    }

    @Test
    @DisplayName("카탈로그 이미지 2개 업로드 시 InvalidFileCountLimitException 예외를 던진다")
    void updateFiles_catalogImage_exceedsMaxCount_throws() {
        UpdateFilesCommand command = buildCommand(
                FileSort.PRODUCT_CATALOG_IMAGE.name(), 2
        );

        assertThatThrownBy(() -> updateFilesService.updateFiles(command))
                .isInstanceOf(InvalidFileCountLimitException.class)
                .hasMessageContaining("1");

        verifyNoInteractions(uploadFilesPort, saveFilesPort, saveResizedFilesPort);
    }

    @Test
    @DisplayName("본문 이미지 6개 업로드 시 InvalidFileCountLimitException 예외를 던진다")
    void updateFiles_contentImage_exceedsMaxCount_throws() {
        UpdateFilesCommand command = buildCommand(
                FileSort.PRODUCT_CONTENT_IMAGE.name(), 6
        );

        assertThatThrownBy(() -> updateFilesService.updateFiles(command))
                .isInstanceOf(InvalidFileCountLimitException.class)
                .hasMessageContaining("5");

        verifyNoInteractions(uploadFilesPort, saveFilesPort, saveResizedFilesPort);
    }

    @Test
    @DisplayName("재업로드 시 기존 파일의 userId와 ownerKey가 일치하면 정상 처리된다")
    void updateFiles_reupload_ownerMatched_processesSuccessfully() {
        UpdateFilesCommand command = buildCommand(FileSort.PRODUCT_CONTENT_IMAGE.name(), 1);
        FileDomain existing = snapshotFileDomain(100L, "test-owner-key");
        when(getFileUseCase.getFiles(OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CONTENT_IMAGE.name()))
                .thenReturn(new ArrayList<>(List.of(existing)));
        when(getFileUseCase.getResizedFiles(anyList())).thenReturn(new ArrayList<>());
        when(uploadFilesPort.uploadFiles(anyList(), eq(OwnerType.PRODUCT), eq(1L)))
                .thenReturn(List.of("https://cdn.example.com/new_0.png"));
        when(saveFilesPort.saveAll(anyList(), anyList())).thenReturn(new ArrayList<>());

        assertThatCode(() -> updateFilesService.updateFiles(command)).doesNotThrowAnyException();

        verify(updateFilesPort).update(anyList(), anyList());
        verify(publishImageEventPort).publishImageDeletedEvents(anyList());
        verify(uploadFilesPort).uploadFiles(anyList(), eq(OwnerType.PRODUCT), eq(1L));
        verify(saveFilesPort).saveAll(anyList(), anyList());
        verify(publishImageEventPort).publishImageCreatedEvents(anyList());
    }

    @Test
    @DisplayName("재업로드 시 기존 파일의 userId가 불일치하면 InvalidFileOwnerException을 던지고 부수효과를 실행하지 않는다")
    void updateFiles_reupload_userIdMismatch_throwsAndSkipsSideEffects() {
        UpdateFilesCommand command = buildCommand(FileSort.PRODUCT_CONTENT_IMAGE.name(), 1);
        FileDomain existing = snapshotFileDomain(999L, "test-owner-key");
        when(getFileUseCase.getFiles(OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CONTENT_IMAGE.name()))
                .thenReturn(new ArrayList<>(List.of(existing)));

        assertThatThrownBy(() -> updateFilesService.updateFiles(command))
                .isInstanceOf(InvalidFileOwnerException.class);

        verifyNoInteractions(uploadFilesPort, saveFilesPort, saveResizedFilesPort, updateFilesPort, publishImageEventPort);
    }

    @Test
    @DisplayName("재업로드 시 기존 파일의 ownerKey가 불일치하면 InvalidFileOwnerException을 던지고 부수효과를 실행하지 않는다")
    void updateFiles_reupload_ownerKeyMismatch_throwsAndSkipsSideEffects() {
        UpdateFilesCommand command = buildCommand(FileSort.PRODUCT_CONTENT_IMAGE.name(), 1);
        FileDomain existing = snapshotFileDomain(100L, "other-owner-key");
        when(getFileUseCase.getFiles(OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CONTENT_IMAGE.name()))
                .thenReturn(new ArrayList<>(List.of(existing)));

        assertThatThrownBy(() -> updateFilesService.updateFiles(command))
                .isInstanceOf(InvalidFileOwnerException.class);

        verifyNoInteractions(uploadFilesPort, saveFilesPort, saveResizedFilesPort, updateFilesPort, publishImageEventPort);
    }

    @Test
    @DisplayName("재업로드 시 기존 파일의 userId가 null이면 InvalidFileOwnerException을 던진다")
    void updateFiles_reupload_existingUserIdNull_throws() {
        UpdateFilesCommand command = buildCommand(FileSort.PRODUCT_CONTENT_IMAGE.name(), 1);
        FileDomain existing = snapshotFileDomain(null, "test-owner-key");
        when(getFileUseCase.getFiles(OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CONTENT_IMAGE.name()))
                .thenReturn(new ArrayList<>(List.of(existing)));

        assertThatThrownBy(() -> updateFilesService.updateFiles(command))
                .isInstanceOf(InvalidFileOwnerException.class);

        verifyNoInteractions(uploadFilesPort, saveFilesPort, saveResizedFilesPort, updateFilesPort, publishImageEventPort);
    }

    @Test
    @DisplayName("최초 업로드 시 기존 파일이 없으면 소유권 검증 없이 정상 저장된다")
    void updateFiles_firstUpload_noExistingFiles_savesSuccessfully() {
        UpdateFilesCommand command = buildCommand(FileSort.PRODUCT_CONTENT_IMAGE.name(), 1);
        when(getFileUseCase.getFiles(OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CONTENT_IMAGE.name()))
                .thenReturn(new ArrayList<>());
        when(uploadFilesPort.uploadFiles(anyList(), eq(OwnerType.PRODUCT), eq(1L)))
                .thenReturn(List.of("https://cdn.example.com/new_0.png"));
        when(saveFilesPort.saveAll(anyList(), anyList())).thenReturn(new ArrayList<>());

        assertThatCode(() -> updateFilesService.updateFiles(command)).doesNotThrowAnyException();

        verify(updateFilesPort, never()).update(anyList(), anyList());
        verify(publishImageEventPort, never()).publishImageDeletedEvents(anyList());
        verify(uploadFilesPort).uploadFiles(anyList(), eq(OwnerType.PRODUCT), eq(1L));
        verify(saveFilesPort).saveAll(anyList(), anyList());
        verify(publishImageEventPort).publishImageCreatedEvents(anyList());
    }

    private static FileDomain snapshotFileDomain(Long userId, String ownerKey) {
        return FileDomain.from(FileDomainSnapshotState.builder()
                .id(1L)
                .ownerType(OwnerType.PRODUCT)
                .ownerId(1L)
                .sort(FileSort.PRODUCT_CONTENT_IMAGE)
                .extension("png")
                .name("existing.png")
                .storageUrl("https://cdn.example.com/existing.png")
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0, 0))
                .status(EntityStatus.ACTIVE)
                .orderNum(1L)
                .userId(userId)
                .ownerKey(ownerKey)
                .build());
    }

    private UpdateFilesCommand buildCommand(String sort, int fileCount) {
        List<UpdateFileCommand> fileCommands = new ArrayList<>();
        for (int i = 0; i < fileCount; i++) {
            MockMultipartFile mockFile = new MockMultipartFile(
                    "file", "test_" + i + ".png", "image/png", new byte[]{1}
            );
            fileCommands.add(UpdateFileCommand.builder()
                    .file(mockFile)
                    .sort(sort)
                    .extension("png")
                    .name("test_" + i + ".png")
                    .build());
        }

        return UpdateFilesCommand.builder()
                .fileInfo(fileCommands)
                .ownerType("PRODUCT")
                .ownerId(1L)
                .ownerKey("test-owner-key")
                .requesterId(100L)
                .requesterRole("SELLER")
                .build();
    }
}
