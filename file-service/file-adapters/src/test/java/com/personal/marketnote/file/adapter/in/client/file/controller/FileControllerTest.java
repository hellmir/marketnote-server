package com.personal.marketnote.file.adapter.in.client.file.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.adapter.in.client.file.request.UpdateFilesRequest;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.FileDomainSnapshotState;
import com.personal.marketnote.file.port.in.command.UpdateFilesCommand;
import com.personal.marketnote.file.port.in.result.GetFilesResult;
import com.personal.marketnote.file.port.in.usecase.file.DeleteFileUseCase;
import com.personal.marketnote.file.port.in.usecase.file.GetFileUseCase;
import com.personal.marketnote.file.port.in.usecase.file.UpdateFileUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.multipart.MultipartFile;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.exception.token.AuthenticationFailedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileController 파일 업로드/조회/삭제")
class FileControllerTest {

    @InjectMocks
    private FileController controller;

    @Mock
    private UpdateFileUseCase updateFileUseCase;

    @Mock
    private GetFileUseCase getFileUseCase;

    @Mock
    private DeleteFileUseCase deleteFileUseCase;

    private OAuth2AuthenticatedPrincipal buyerPrincipal(Long userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                String.valueOf(userId),
                Map.of("name", String.valueOf(userId)),
                List.of(new SimpleGrantedAuthority("ROLE_BUYER"))
        );
    }

    @Nested
    @DisplayName("POST /api/v1/files - 파일 목록 업로드/수정")
    class UpdateFiles {

        @Test
        @DisplayName("요청 파일 목록을 Command로 매핑하고 인증된 요청자 ID/역할을 담아 UseCase에 위임한다")
        void delegatesUpdateFilesWithRequesterContext() {
            // given
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", "image.jpg", "image/jpeg", "payload".getBytes()
            );
            UpdateFilesRequest request = new UpdateFilesRequest();
            request.setFile(List.of(multipartFile));
            request.setSort(List.of("PRODUCT_CATALOG_IMAGE"));
            request.setExtension(List.of("jpg"));
            request.setName(List.of("상품-이미지"));
            request.setOwnerType("PRODUCT");
            request.setOwnerId(1L);
            request.setOwnerKey("owner-key-1");

            // when
            ResponseEntity<BaseResponse<Void>> response = controller.updateFiles(request, buyerPrincipal(10L));

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();

            ArgumentCaptor<UpdateFilesCommand> captor = ArgumentCaptor.forClass(UpdateFilesCommand.class);
            verify(updateFileUseCase).updateFiles(captor.capture());
            UpdateFilesCommand captured = captor.getValue();
            assertThat(captured.ownerType()).isEqualTo("PRODUCT");
            assertThat(captured.ownerId()).isEqualTo(1L);
            assertThat(captured.ownerKey()).isEqualTo("owner-key-1");
            assertThat(captured.requesterId()).isEqualTo(10L);
            assertThat(captured.requesterRole()).isEqualTo("BUYER");
            assertThat(captured.fileInfo()).hasSize(1);
            assertThat(captured.fileInfo().get(0).sort()).isEqualTo("PRODUCT_CATALOG_IMAGE");
            assertThat(captured.fileInfo().get(0).extension()).isEqualTo("jpg");
            assertThat(captured.fileInfo().get(0).name()).isEqualTo("상품-이미지");
            MultipartFile capturedFile = captured.fileInfo().get(0).file();
            assertThat(capturedFile).isSameAs(multipartFile);
        }

        @Test
        @DisplayName("업로드 파일이 null이어도 ownerType/ownerId/ownerKey 메타데이터만 담은 Command로 위임한다")
        void delegatesUpdateFilesWithEmptyFileList() {
            // given
            UpdateFilesRequest request = new UpdateFilesRequest();
            request.setOwnerType("REVIEW");
            request.setOwnerId(200L);
            request.setOwnerKey("owner-key-review");

            // when
            ResponseEntity<BaseResponse<Void>> response = controller.updateFiles(request, buyerPrincipal(55L));

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            ArgumentCaptor<UpdateFilesCommand> captor = ArgumentCaptor.forClass(UpdateFilesCommand.class);
            verify(updateFileUseCase).updateFiles(captor.capture());
            UpdateFilesCommand captured = captor.getValue();
            assertThat(captured.fileInfo()).isEmpty();
            assertThat(captured.ownerType()).isEqualTo("REVIEW");
            assertThat(captured.ownerId()).isEqualTo(200L);
            assertThat(captured.ownerKey()).isEqualTo("owner-key-review");
            assertThat(captured.requesterId()).isEqualTo(55L);
            assertThat(captured.requesterRole()).isEqualTo("BUYER");
        }

        @Test
        @DisplayName("principal 이름이 -1이면 AuthenticationFailedException이 발생한다")
        void throwsWhenPrincipalIsAnonymous() {
            // given
            UpdateFilesRequest request = new UpdateFilesRequest();
            request.setOwnerType("PRODUCT");
            request.setOwnerId(1L);
            request.setOwnerKey("owner-key");
            OAuth2AuthenticatedPrincipal anonymous = new DefaultOAuth2AuthenticatedPrincipal(
                    "-1", Map.of("name", "-1"), List.of()
            );

            // when & then
            assertThatThrownBy(() -> controller.updateFiles(request, anonymous))
                    .isInstanceOf(AuthenticationFailedException.class);
            verifyNoInteractions(updateFileUseCase);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/files - 파일 목록 조회")
    class GetFiles {

        @Test
        @DisplayName("소유자 타입/ID/정렬 기준으로 조회하여 OK 응답과 결과를 반환한다")
        void returnsFilesByOwnerAndSort() {
            // given
            GetFilesResult result = new GetFilesResult(
                    List.of(GetFilesResult.FileItem.from(
                            buildFileDomain(1L),
                            Map.of(1L, List.of("https://bucket/resized/1.jpg"))
                    ))
            );
            when(getFileUseCase.getFiles("PRODUCT", 1L, "PRODUCT_CATALOG_IMAGE")).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetFilesResult>> response = controller.getFiles(
                    "PRODUCT", 1L, "PRODUCT_CATALOG_IMAGE"
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().files()).hasSize(1);
            assertThat(response.getBody().getContent().files().get(0).id()).isEqualTo(1L);
            verify(getFileUseCase).getFiles("PRODUCT", 1L, "PRODUCT_CATALOG_IMAGE");
        }

        @Test
        @DisplayName("sort가 null이어도 UseCase에 그대로 전달한다")
        void passesNullSortToUseCase() {
            // given
            GetFilesResult emptyResult = new GetFilesResult(List.of());
            when(getFileUseCase.getFiles("POST", 5L, null)).thenReturn(emptyResult);

            // when
            ResponseEntity<BaseResponse<GetFilesResult>> response = controller.getFiles("POST", 5L, null);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent().files()).isEmpty();
            verify(getFileUseCase).getFiles("POST", 5L, null);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/files/{id} - 파일 삭제")
    class DeleteFile {

        @Test
        @DisplayName("경로 파라미터 ID로 UseCase delete를 호출하고 OK를 반환한다")
        void delegatesDeleteToUseCase() {
            // when
            ResponseEntity<BaseResponse<Void>> response = controller.deleteFile(99L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(deleteFileUseCase).delete(99L);
        }
    }

    private FileDomain buildFileDomain(Long id) {
        return FileDomain.from(FileDomainSnapshotState.builder()
                .id(id)
                .ownerType(OwnerType.PRODUCT)
                .ownerId(1L)
                .sort(FileSort.PRODUCT_CATALOG_IMAGE)
                .extension("jpg")
                .name("test.jpg")
                .storageUrl("https://bucket/product/1/test.jpg")
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .status(EntityStatus.ACTIVE)
                .orderNum(id)
                .userId(10L)
                .ownerKey("owner-key-1")
                .build());
    }
}
