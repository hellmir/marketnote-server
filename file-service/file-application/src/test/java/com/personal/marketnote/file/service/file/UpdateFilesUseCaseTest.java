package com.personal.marketnote.file.service.file;

import com.personal.marketnote.common.domain.file.FileSort;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

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
                .build();
    }
}
