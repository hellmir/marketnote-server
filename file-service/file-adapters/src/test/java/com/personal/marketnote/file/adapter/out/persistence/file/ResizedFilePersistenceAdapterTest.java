package com.personal.marketnote.file.adapter.out.persistence.file;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.file.adapter.out.persistence.file.entity.FileJpaEntity;
import com.personal.marketnote.file.adapter.out.persistence.file.entity.ResizedFileJpaEntity;
import com.personal.marketnote.file.adapter.out.persistence.file.repository.FileJpaRepository;
import com.personal.marketnote.file.adapter.out.persistence.file.repository.ResizedFileJpaRepository;
import com.personal.marketnote.file.domain.file.ResizedFile;
import com.personal.marketnote.file.domain.file.ResizedFileCreateState;
import com.personal.marketnote.file.domain.file.ResizedFileSnapshotState;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResizedFilePersistenceAdapterTest {
    @InjectMocks
    private ResizedFilePersistenceAdapter resizedFilePersistenceAdapter;

    @Mock
    private FileJpaRepository fileJpaRepository;

    @Mock
    private ResizedFileJpaRepository resizedFileJpaRepository;

    @Nested
    @DisplayName("saveAll")
    class SaveAll {
        @Test
        @DisplayName("리사이즈 파일 목록이 null이면 저장하지 않는다")
        void doesNotSaveWhenListIsNull() {
            // when
            resizedFilePersistenceAdapter.saveAll(null);

            // then
            verifyNoInteractions(fileJpaRepository, resizedFileJpaRepository);
        }

        @Test
        @DisplayName("리사이즈 파일 목록이 비어있으면 저장하지 않는다")
        void doesNotSaveWhenListIsEmpty() {
            // when
            resizedFilePersistenceAdapter.saveAll(List.of());

            // then
            verifyNoInteractions(fileJpaRepository, resizedFileJpaRepository);
        }

        @Test
        @DisplayName("리사이즈 파일 목록이 존재하면 파일 참조를 조회하여 저장한다")
        void savesResizedFilesWithFileReference() {
            // given
            ResizedFile resizedFile = ResizedFile.from(ResizedFileCreateState.builder()
                    .fileId(1L)
                    .size("THUMBNAIL")
                    .storageUrl("https://bucket.s3.amazonaws.com/resized/thumb.jpg")
                    .build());

            FileJpaEntity fileRef = mock(FileJpaEntity.class);
            when(fileJpaRepository.getReferenceById(1L)).thenReturn(fileRef);

            // when
            resizedFilePersistenceAdapter.saveAll(List.of(resizedFile));

            // then
            verify(fileJpaRepository).getReferenceById(1L);
            verify(resizedFileJpaRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("여러 리사이즈 파일 저장 시 각 파일 참조를 개별 조회한다")
        void savesMultipleResizedFilesWithIndividualReferences() {
            // given
            ResizedFile resized1 = ResizedFile.from(ResizedFileCreateState.builder()
                    .fileId(1L)
                    .size("THUMBNAIL")
                    .storageUrl("https://bucket.s3.amazonaws.com/resized/thumb1.jpg")
                    .build());
            ResizedFile resized2 = ResizedFile.from(ResizedFileCreateState.builder()
                    .fileId(2L)
                    .size("MEDIUM")
                    .storageUrl("https://bucket.s3.amazonaws.com/resized/medium2.jpg")
                    .build());

            FileJpaEntity fileRef1 = mock(FileJpaEntity.class);
            FileJpaEntity fileRef2 = mock(FileJpaEntity.class);
            when(fileJpaRepository.getReferenceById(1L)).thenReturn(fileRef1);
            when(fileJpaRepository.getReferenceById(2L)).thenReturn(fileRef2);

            // when
            resizedFilePersistenceAdapter.saveAll(List.of(resized1, resized2));

            // then
            verify(fileJpaRepository).getReferenceById(1L);
            verify(fileJpaRepository).getReferenceById(2L);
            verify(resizedFileJpaRepository).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("findByFileIds")
    class FindByFileIds {
        @Test
        @DisplayName("파일 ID 목록이 null이면 빈 리스트를 반환한다")
        void returnsEmptyListWhenFileIdsIsNull() {
            // when
            List<ResizedFile> result = resizedFilePersistenceAdapter.findByFileIds(null);

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(resizedFileJpaRepository);
        }

        @Test
        @DisplayName("파일 ID 목록이 비어있으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenFileIdsIsEmpty() {
            // when
            List<ResizedFile> result = resizedFilePersistenceAdapter.findByFileIds(List.of());

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(resizedFileJpaRepository);
        }

        @Test
        @DisplayName("파일 ID로 리사이즈 파일을 조회하여 도메인 객체로 변환한다")
        void returnsResizedFileDomainsForGivenFileIds() {
            // given
            FileJpaEntity fileEntity = mock(FileJpaEntity.class);
            when(fileEntity.getId()).thenReturn(1L);

            ResizedFileJpaEntity entity = mock(ResizedFileJpaEntity.class);
            when(entity.getId()).thenReturn(10L);
            when(entity.getFile()).thenReturn(fileEntity);
            when(entity.getSize()).thenReturn("THUMBNAIL");
            when(entity.getStorageUrl()).thenReturn("https://bucket.s3.amazonaws.com/resized/thumb.jpg");
            when(entity.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 1, 1, 0, 0));
            when(entity.getStatus()).thenReturn(EntityStatus.ACTIVE);

            when(resizedFileJpaRepository.findAllByFile_IdIn(List.of(1L))).thenReturn(List.of(entity));

            // when
            List<ResizedFile> result = resizedFilePersistenceAdapter.findByFileIds(List.of(1L));

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(10L);
            assertThat(result.get(0).getFileId()).isEqualTo(1L);
            assertThat(result.get(0).getSize()).isEqualTo("THUMBNAIL");
            assertThat(result.get(0).getStorageUrl()).isEqualTo("https://bucket.s3.amazonaws.com/resized/thumb.jpg");
        }
    }
}
