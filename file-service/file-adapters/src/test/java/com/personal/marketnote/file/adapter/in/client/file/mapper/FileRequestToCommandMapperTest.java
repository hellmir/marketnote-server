package com.personal.marketnote.file.adapter.in.client.file.mapper;

import com.personal.marketnote.file.adapter.in.client.file.request.UpdateFilesRequest;
import com.personal.marketnote.file.port.in.command.UpdateFileCommand;
import com.personal.marketnote.file.port.in.command.UpdateFilesCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FileRequestToCommandMapperTest {

    private UpdateFilesRequest createRequest(
            List<MultipartFile> files, List<String> sorts, List<String> extensions, List<String> names
    ) {
        UpdateFilesRequest request = new UpdateFilesRequest();
        request.setFile(files);
        request.setSort(sorts);
        request.setExtension(extensions);
        request.setName(names);
        request.setOwnerType("PRODUCT");
        request.setOwnerId(1L);
        request.setOwnerKey("owner-key-1");
        return request;
    }

    @Nested
    @DisplayName("mapToCommand")
    class MapToCommand {
        @Test
        @DisplayName("파일 목록이 null이면 빈 fileInfo를 가진 Command를 반환한다")
        void returnsEmptyFileInfoWhenFilesIsNull() {
            // given
            UpdateFilesRequest request = createRequest(null, null, null, null);

            // when
            UpdateFilesCommand command = FileRequestToCommandMapper.mapToCommand(request, 100L, "ROLE_BUYER");

            // then
            assertThat(command.fileInfo()).isEmpty();
            assertThat(command.ownerType()).isEqualTo("PRODUCT");
            assertThat(command.ownerId()).isEqualTo(1L);
            assertThat(command.ownerKey()).isEqualTo("owner-key-1");
            assertThat(command.requesterId()).isEqualTo(100L);
            assertThat(command.requesterRole()).isEqualTo("ROLE_BUYER");
        }

        @Test
        @DisplayName("파일 목록이 비어있으면 빈 fileInfo를 가진 Command를 반환한다")
        void returnsEmptyFileInfoWhenFilesIsEmpty() {
            // given
            UpdateFilesRequest request = createRequest(new ArrayList<>(), null, null, null);

            // when
            UpdateFilesCommand command = FileRequestToCommandMapper.mapToCommand(request, 100L, "ROLE_BUYER");

            // then
            assertThat(command.fileInfo()).isEmpty();
        }

        @Test
        @DisplayName("파일이 존재하면 파일 수만큼 UpdateFileCommand를 생성한다")
        void createsFileCommandsMatchingFileCount() {
            // given
            MockMultipartFile file1 = new MockMultipartFile("file", "image1.jpg", "image/jpeg", "data1".getBytes());
            MockMultipartFile file2 = new MockMultipartFile("file", "image2.jpg", "image/jpeg", "data2".getBytes());

            UpdateFilesRequest request = createRequest(
                    List.of(file1, file2),
                    List.of("PRODUCT_CATALOG_IMAGE", "PRODUCT_REPRESENTATIVE_IMAGE"),
                    List.of("jpg", "png"),
                    List.of("상품이미지1", "상품이미지2")
            );

            // when
            UpdateFilesCommand command = FileRequestToCommandMapper.mapToCommand(request, 100L, "ROLE_SELLER");

            // then
            assertThat(command.fileInfo()).hasSize(2);

            UpdateFileCommand first = command.fileInfo().get(0);
            assertThat(first.file()).isEqualTo(file1);
            assertThat(first.sort()).isEqualTo("PRODUCT_CATALOG_IMAGE");
            assertThat(first.extension()).isEqualTo("jpg");
            assertThat(first.name()).isEqualTo("상품이미지1");

            UpdateFileCommand second = command.fileInfo().get(1);
            assertThat(second.file()).isEqualTo(file2);
            assertThat(second.sort()).isEqualTo("PRODUCT_REPRESENTATIVE_IMAGE");
            assertThat(second.extension()).isEqualTo("png");
            assertThat(second.name()).isEqualTo("상품이미지2");
        }

        @Test
        @DisplayName("sort/extension/name 배열이 파일 수보다 짧으면 초과 인덱스는 null로 매핑한다")
        void mapsNullForExcessIndicesWhenArraysShorter() {
            // given
            MockMultipartFile file1 = new MockMultipartFile("file", "image1.jpg", "image/jpeg", "data1".getBytes());
            MockMultipartFile file2 = new MockMultipartFile("file", "image2.jpg", "image/jpeg", "data2".getBytes());

            UpdateFilesRequest request = createRequest(
                    List.of(file1, file2),
                    List.of("PRODUCT_CATALOG_IMAGE"),
                    List.of("jpg"),
                    List.of("상품이미지1")
            );

            // when
            UpdateFilesCommand command = FileRequestToCommandMapper.mapToCommand(request, 100L, "ROLE_SELLER");

            // then
            assertThat(command.fileInfo()).hasSize(2);

            UpdateFileCommand first = command.fileInfo().get(0);
            assertThat(first.sort()).isEqualTo("PRODUCT_CATALOG_IMAGE");
            assertThat(first.extension()).isEqualTo("jpg");
            assertThat(first.name()).isEqualTo("상품이미지1");

            UpdateFileCommand second = command.fileInfo().get(1);
            assertThat(second.sort()).isNull();
            assertThat(second.extension()).isNull();
            assertThat(second.name()).isNull();
        }

        @Test
        @DisplayName("sort/extension/name 배열이 null이면 모든 인덱스가 null로 매핑된다")
        void mapsNullForAllIndicesWhenArraysAreNull() {
            // given
            MockMultipartFile file1 = new MockMultipartFile("file", "image1.jpg", "image/jpeg", "data1".getBytes());

            UpdateFilesRequest request = createRequest(List.of(file1), null, null, null);

            // when
            UpdateFilesCommand command = FileRequestToCommandMapper.mapToCommand(request, 100L, "ROLE_ADMIN");

            // then
            assertThat(command.fileInfo()).hasSize(1);

            UpdateFileCommand first = command.fileInfo().get(0);
            assertThat(first.file()).isEqualTo(file1);
            assertThat(first.sort()).isNull();
            assertThat(first.extension()).isNull();
            assertThat(first.name()).isNull();
        }
    }
}
