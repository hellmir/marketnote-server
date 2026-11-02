package com.personal.marketnote.file.adapter.out.persistence.file.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.adapter.out.persistence.file.entity.FileJpaEntity;
import com.personal.marketnote.file.adapter.out.persistence.file.entity.ResizedFileJpaEntity;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.FileDomainSnapshotState;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AuditConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ResizedFileJpaRepositoryTest {

    @Autowired
    private ResizedFileJpaRepository resizedFileJpaRepository;

    @Autowired
    private FileJpaRepository fileJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void clearContext() {
        entityManager.clear();
    }

    @Nested
    @DisplayName("findAllByFile_IdIn")
    class FindAllByFileIdIn {

        @Test
        @DisplayName("전달된 파일 ID 목록에 속한 ACTIVE 리사이즈 파일을 ID 내림차순으로 반환한다")
        void returnsActiveResizedFilesInIdDescOrder() {
            // given
            FileJpaEntity file = persistFile();
            ResizedFileJpaEntity first = persistResizedFile(file, "THUMBNAIL");
            ResizedFileJpaEntity second = persistResizedFile(file, "MEDIUM");
            ResizedFileJpaEntity third = persistResizedFile(file, "LARGE");
            entityManager.flush();
            entityManager.clear();

            // when
            List<ResizedFileJpaEntity> result = resizedFileJpaRepository.findAllByFile_IdIn(List.of(file.getId()));

            // then
            assertThat(result)
                    .extracting(ResizedFileJpaEntity::getId)
                    .containsExactly(third.getId(), second.getId(), first.getId());
        }

        @Test
        @DisplayName("여러 파일 ID로 조회하면 모든 파일에 속한 ACTIVE 리사이즈 파일을 반환한다")
        void returnsResizedFilesForMultipleFileIds() {
            // given
            FileJpaEntity fileA = persistFile();
            FileJpaEntity fileB = persistFile();
            ResizedFileJpaEntity resizedA = persistResizedFile(fileA, "THUMBNAIL");
            ResizedFileJpaEntity resizedB = persistResizedFile(fileB, "THUMBNAIL");
            entityManager.flush();
            entityManager.clear();

            // when
            List<ResizedFileJpaEntity> result = resizedFileJpaRepository
                    .findAllByFile_IdIn(List.of(fileA.getId(), fileB.getId()));

            // then
            assertThat(result)
                    .extracting(ResizedFileJpaEntity::getId)
                    .containsExactlyInAnyOrder(resizedA.getId(), resizedB.getId());
        }

        @Test
        @DisplayName("전달되지 않은 파일 ID의 리사이즈 파일은 제외된다")
        void excludesResizedFilesNotInFileIds() {
            // given
            FileJpaEntity fileA = persistFile();
            FileJpaEntity fileB = persistFile();
            ResizedFileJpaEntity resizedA = persistResizedFile(fileA, "THUMBNAIL");
            persistResizedFile(fileB, "THUMBNAIL");
            entityManager.flush();
            entityManager.clear();

            // when
            List<ResizedFileJpaEntity> result = resizedFileJpaRepository
                    .findAllByFile_IdIn(List.of(fileA.getId()));

            // then
            assertThat(result)
                    .extracting(ResizedFileJpaEntity::getId)
                    .containsExactly(resizedA.getId());
        }

        @Test
        @DisplayName("ACTIVE가 아닌 상태의 리사이즈 파일은 조회되지 않는다")
        void excludesNonActiveStatus() {
            // given
            FileJpaEntity file = persistFile();
            ResizedFileJpaEntity active = persistResizedFile(file, "THUMBNAIL");
            ResizedFileJpaEntity inactive = persistResizedFile(file, "MEDIUM");
            entityManager.flush();
            markResizedFileStatus(inactive.getId(), EntityStatus.INACTIVE);

            // when
            List<ResizedFileJpaEntity> result = resizedFileJpaRepository.findAllByFile_IdIn(List.of(file.getId()));

            // then
            assertThat(result)
                    .extracting(ResizedFileJpaEntity::getId)
                    .containsExactly(active.getId());
        }

        @Test
        @DisplayName("일치하는 리사이즈 파일이 없으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNoMatch() {
            // given
            entityManager.flush();
            entityManager.clear();

            // when
            List<ResizedFileJpaEntity> result = resizedFileJpaRepository.findAllByFile_IdIn(List.of(9999L));

            // then
            assertThat(result).isEmpty();
        }
    }

    private FileJpaEntity persistFile() {
        FileDomain domain = FileDomain.from(FileDomainSnapshotState.builder()
                .ownerType(OwnerType.PRODUCT)
                .ownerId(1L)
                .sort(FileSort.PRODUCT_CATALOG_IMAGE)
                .extension("jpg")
                .name("test-image.jpg")
                .storageUrl("https://bucket.s3.amazonaws.com/product/1/test.jpg")
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .status(EntityStatus.ACTIVE)
                .orderNum(null)
                .userId(100L)
                .ownerKey("owner-key")
                .build());
        FileJpaEntity entity = FileJpaEntity.from(domain, domain.getStorageUrl());
        return fileJpaRepository.saveAndFlush(entity);
    }

    private ResizedFileJpaEntity persistResizedFile(FileJpaEntity file, String size) {
        ResizedFileJpaEntity entity = ResizedFileJpaEntity.of(
                file,
                size,
                "https://bucket.s3.amazonaws.com/resized/" + file.getId() + "/" + size + ".jpg"
        );
        return resizedFileJpaRepository.saveAndFlush(entity);
    }

    private void markResizedFileStatus(Long id, EntityStatus status) {
        entityManager.createNativeQuery("UPDATE resized_file SET status = :status WHERE id = :id")
                .setParameter("status", status.name())
                .setParameter("id", id)
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }
}
