package com.personal.marketnote.file.adapter.out.persistence.file;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.adapter.out.persistence.file.entity.FileJpaEntity;
import com.personal.marketnote.file.adapter.out.persistence.file.entity.ResizedFileJpaEntity;
import com.personal.marketnote.file.adapter.out.persistence.file.repository.FileJpaRepository;
import com.personal.marketnote.file.adapter.out.persistence.file.repository.ResizedFileJpaRepository;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.FileDomainSnapshotState;
import com.personal.marketnote.file.domain.file.ResizedFile;
import com.personal.marketnote.file.domain.file.ResizedFileSnapshotState;
import com.personal.marketnote.file.exception.FileNotFoundException;
import com.personal.marketnote.file.exception.InvalidFileStorageUrlsSizeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilePersistenceAdapterTest {
    @InjectMocks
    private FilePersistenceAdapter filePersistenceAdapter;

    @Mock
    private FileJpaRepository fileJpaRepository;

    @Mock
    private ResizedFileJpaRepository resizedFileJpaRepository;

    private FileDomain createFileDomain(Long id, OwnerType ownerType, Long ownerId, FileSort sort) {
        return FileDomain.from(FileDomainSnapshotState.builder()
                .id(id)
                .ownerType(ownerType)
                .ownerId(ownerId)
                .sort(sort)
                .extension("jpg")
                .name("test-image.jpg")
                .storageUrl("https://bucket.s3.amazonaws.com/product/1/test.jpg")
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .status(EntityStatus.ACTIVE)
                .orderNum(id)
                .userId(100L)
                .ownerKey("owner-key-1")
                .build());
    }

    private FileJpaEntity createFileJpaEntity(Long id) {
        FileJpaEntity entity = mock(FileJpaEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getOwnerType()).thenReturn(OwnerType.PRODUCT);
        when(entity.getOwnerId()).thenReturn(1L);
        when(entity.getSort()).thenReturn("PRODUCT_CATALOG_IMAGE");
        when(entity.getExtension()).thenReturn("jpg");
        when(entity.getName()).thenReturn("test-image.jpg");
        when(entity.getStorageUrl()).thenReturn("https://bucket.s3.amazonaws.com/product/1/test.jpg");
        when(entity.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 1, 1, 0, 0));
        when(entity.getStatus()).thenReturn(EntityStatus.ACTIVE);
        when(entity.getOrderNum()).thenReturn(id);
        when(entity.getUserId()).thenReturn(100L);
        when(entity.getOwnerKey()).thenReturn("owner-key-1");
        return entity;
    }

    @Nested
    @DisplayName("saveAll")
    class SaveAll {
        @Test
        @DisplayName("파일 목록이 null이면 빈 리스트를 반환한다")
        void returnsEmptyListWhenFilesIsNull() {
            // when
            List<FileDomain> result = filePersistenceAdapter.saveAll(null, List.of("url1"));

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(fileJpaRepository);
        }

        @Test
        @DisplayName("파일 목록이 비어있으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenFilesIsEmpty() {
            // when
            List<FileDomain> result = filePersistenceAdapter.saveAll(List.of(), List.of("url1"));

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(fileJpaRepository);
        }

        @Test
        @DisplayName("storageUrls가 null이면 InvalidFileStorageUrlsSizeException이 발생한다")
        void throwsExceptionWhenStorageUrlsIsNull() {
            // given
            FileDomain file = createFileDomain(1L, OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE);

            // when & then
            assertThatThrownBy(() -> filePersistenceAdapter.saveAll(List.of(file), null))
                    .isInstanceOf(InvalidFileStorageUrlsSizeException.class);
        }

        @Test
        @DisplayName("storageUrls 크기가 파일 수와 다르면 InvalidFileStorageUrlsSizeException이 발생한다")
        void throwsExceptionWhenStorageUrlsSizeMismatch() {
            // given
            FileDomain file1 = createFileDomain(1L, OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE);
            FileDomain file2 = createFileDomain(2L, OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE);

            // when & then
            assertThatThrownBy(() -> filePersistenceAdapter.saveAll(List.of(file1, file2), List.of("url1")))
                    .isInstanceOf(InvalidFileStorageUrlsSizeException.class);
        }

        @Test
        @DisplayName("파일과 storageUrls 크기가 일치하면 저장 후 도메인 객체 리스트를 반환한다")
        void savesAndReturnsDomainListWhenSizesMatch() {
            // given
            FileDomain file = createFileDomain(1L, OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE);
            List<String> storageUrls = List.of("https://bucket.s3.amazonaws.com/product/1/new.jpg");

            FileJpaEntity savedEntity = createFileJpaEntity(1L);
            when(fileJpaRepository.saveAll(anyList())).thenReturn(List.of(savedEntity));

            // when
            List<FileDomain> result = filePersistenceAdapter.saveAll(List.of(file), storageUrls);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            verify(fileJpaRepository).saveAll(anyList());
            verify(savedEntity).setIdToOrderNum();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {
        @Test
        @DisplayName("파일이 존재하면 도메인 객체를 반환한다")
        void returnsDomainWhenFileExists() {
            // given
            FileJpaEntity entity = createFileJpaEntity(1L);
            when(fileJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            // when
            Optional<FileDomain> result = filePersistenceAdapter.findById(1L);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("파일이 존재하지 않으면 빈 Optional을 반환한다")
        void returnsEmptyWhenFileNotFound() {
            // given
            when(fileJpaRepository.findById(999L)).thenReturn(Optional.empty());

            // when
            Optional<FileDomain> result = filePersistenceAdapter.findById(999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOwner")
    class FindByOwner {
        @Test
        @DisplayName("소유자 타입과 ID로 파일 목록을 조회한다")
        void returnsFilesByOwner() {
            // given
            FileJpaEntity entity = createFileJpaEntity(1L);
            when(fileJpaRepository.findAllByOwnerTypeAndOwnerIdOrderByIdAsc(OwnerType.PRODUCT, 1L))
                    .thenReturn(List.of(entity));

            // when
            List<FileDomain> result = filePersistenceAdapter.findByOwner(OwnerType.PRODUCT, 1L);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getOwnerType()).isEqualTo(OwnerType.PRODUCT);
        }

        @Test
        @DisplayName("해당 소유자의 파일이 없으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNoFilesForOwner() {
            // given
            when(fileJpaRepository.findAllByOwnerTypeAndOwnerIdOrderByIdAsc(OwnerType.PRODUCT, 999L))
                    .thenReturn(List.of());

            // when
            List<FileDomain> result = filePersistenceAdapter.findByOwner(OwnerType.PRODUCT, 999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOwnerAndSort")
    class FindByOwnerAndSort {
        @Test
        @DisplayName("소유자 타입, ID, 정렬 기준으로 파일 목록을 조회한다")
        void returnsFilesByOwnerAndSort() {
            // given
            FileJpaEntity entity = createFileJpaEntity(1L);
            when(fileJpaRepository.findByOwnerTypeAndOwnerIdAndSort(
                    OwnerType.PRODUCT, 1L, "PRODUCT_REPRESENTATIVE_IMAGE",
                    PageRequest.of(0, 8)
            )).thenReturn(List.of(entity));

            // when
            List<FileDomain> result = filePersistenceAdapter.findByOwnerAndSort(
                    OwnerType.PRODUCT, 1L, FileSort.PRODUCT_REPRESENTATIVE_IMAGE
            );

            // then
            assertThat(result).hasSize(1);
            verify(fileJpaRepository).findByOwnerTypeAndOwnerIdAndSort(
                    OwnerType.PRODUCT, 1L, "PRODUCT_REPRESENTATIVE_IMAGE",
                    PageRequest.of(0, 8)
            );
        }
    }

    @Nested
    @DisplayName("update (단일)")
    class UpdateSingle {
        @Test
        @DisplayName("파일이 존재하면 엔티티를 업데이트한다")
        void updatesEntityWhenFileExists() {
            // given
            FileDomain file = createFileDomain(1L, OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE);
            FileJpaEntity entity = mock(FileJpaEntity.class);
            when(fileJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            // when
            filePersistenceAdapter.update(file);

            // then
            verify(entity).updateFrom(file);
        }

        @Test
        @DisplayName("파일이 존재하지 않으면 FileNotFoundException이 발생한다")
        void throwsExceptionWhenFileNotFound() {
            // given
            FileDomain file = createFileDomain(999L, OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE);
            when(fileJpaRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> filePersistenceAdapter.update(file))
                    .isInstanceOf(FileNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update (배치)")
    class UpdateBatch {
        @Test
        @DisplayName("파일과 리사이즈 파일을 함께 배치 업데이트한다")
        void updatesBothFilesAndResizedFiles() {
            // given
            FileDomain file = createFileDomain(1L, OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE);
            ResizedFile resizedFile = ResizedFile.from(ResizedFileSnapshotState.builder()
                    .id(10L)
                    .fileId(1L)
                    .size("THUMBNAIL")
                    .storageUrl("https://bucket.s3.amazonaws.com/resized/thumb.jpg")
                    .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                    .status(EntityStatus.ACTIVE)
                    .build());

            FileJpaEntity fileEntity = mock(FileJpaEntity.class);
            ResizedFileJpaEntity resizedEntity = mock(ResizedFileJpaEntity.class);

            when(fileJpaRepository.findByIds(List.of(1L))).thenReturn(List.of(fileEntity));
            when(resizedFileJpaRepository.findAllByFile_IdIn(List.of(1L))).thenReturn(List.of(resizedEntity));

            // when
            filePersistenceAdapter.update(List.of(file), List.of(resizedFile));

            // then
            verify(fileEntity).updateFrom(file);
            verify(resizedEntity).updateFrom(resizedFile);
        }
    }
}
