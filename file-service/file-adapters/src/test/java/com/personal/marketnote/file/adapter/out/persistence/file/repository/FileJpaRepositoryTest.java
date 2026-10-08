package com.personal.marketnote.file.adapter.out.persistence.file.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.adapter.out.persistence.file.entity.FileJpaEntity;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AuditConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FileJpaRepositoryTest {

    @Autowired
    private FileJpaRepository fileJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void clearContext() {
        entityManager.clear();
    }

    @Nested
    @DisplayName("findAllByOwnerTypeAndOwnerIdOrderByIdAsc")
    class FindAllByOwnerTypeAndOwnerIdOrderByIdAsc {

        @Test
        @DisplayName("소유자 타입과 ID가 일치하는 ACTIVE 상태 파일만 ID 오름차순으로 조회한다")
        void returnsActiveFilesInIdAscOrder() {
            // given
            FileJpaEntity first = persistFile(OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE, 100L);
            FileJpaEntity second = persistFile(OwnerType.PRODUCT, 1L, FileSort.PRODUCT_REPRESENTATIVE_IMAGE, 50L);
            entityManager.flush();
            entityManager.clear();

            // when
            List<FileJpaEntity> result = fileJpaRepository
                    .findAllByOwnerTypeAndOwnerIdOrderByIdAsc(OwnerType.PRODUCT, 1L);

            // then
            assertThat(result)
                    .extracting(FileJpaEntity::getId)
                    .containsExactly(first.getId(), second.getId());
        }

        @Test
        @DisplayName("다른 소유자 타입의 파일은 조회되지 않는다")
        void excludesOtherOwnerTypes() {
            // given
            persistFile(OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE, 10L);
            persistFile(OwnerType.POST, 1L, FileSort.POST_IMAGE, 20L);
            entityManager.flush();
            entityManager.clear();

            // when
            List<FileJpaEntity> result = fileJpaRepository
                    .findAllByOwnerTypeAndOwnerIdOrderByIdAsc(OwnerType.PRODUCT, 1L);

            // then
            assertThat(result)
                    .hasSize(1)
                    .extracting(FileJpaEntity::getOwnerType)
                    .containsExactly(OwnerType.PRODUCT);
        }

        @Test
        @DisplayName("다른 소유자 ID의 파일은 조회되지 않는다")
        void excludesOtherOwnerIds() {
            // given
            persistFile(OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE, 10L);
            persistFile(OwnerType.PRODUCT, 2L, FileSort.PRODUCT_CATALOG_IMAGE, 20L);
            entityManager.flush();
            entityManager.clear();

            // when
            List<FileJpaEntity> result = fileJpaRepository
                    .findAllByOwnerTypeAndOwnerIdOrderByIdAsc(OwnerType.PRODUCT, 1L);

            // then
            assertThat(result)
                    .hasSize(1)
                    .extracting(FileJpaEntity::getOwnerId)
                    .containsExactly(1L);
        }

        @Test
        @DisplayName("ACTIVE가 아닌 상태의 파일은 조회되지 않는다")
        void excludesNonActiveStatus() {
            // given
            FileJpaEntity active = persistFile(OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE, 10L);
            FileJpaEntity inactive = persistFile(OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE, 20L);
            entityManager.flush();
            markStatus(inactive.getId(), EntityStatus.INACTIVE);

            // when
            List<FileJpaEntity> result = fileJpaRepository
                    .findAllByOwnerTypeAndOwnerIdOrderByIdAsc(OwnerType.PRODUCT, 1L);

            // then
            assertThat(result)
                    .extracting(FileJpaEntity::getId)
                    .containsExactly(active.getId());
        }

        @Test
        @DisplayName("해당 소유자의 파일이 없으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNoMatch() {
            // given
            entityManager.flush();
            entityManager.clear();

            // when
            List<FileJpaEntity> result = fileJpaRepository
                    .findAllByOwnerTypeAndOwnerIdOrderByIdAsc(OwnerType.PRODUCT, 999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOwnerTypeAndOwnerIdAndSort")
    class FindByOwnerTypeAndOwnerIdAndSort {

        @Test
        @DisplayName("소유자 타입, ID, 정렬 기준이 일치하는 ACTIVE 파일을 orderNum 내림차순으로 조회한다")
        void returnsFilesInOrderNumDescOrder() {
            // given
            FileJpaEntity lower = persistFileWithOrderNum(
                    OwnerType.PRODUCT, 1L, FileSort.PRODUCT_REPRESENTATIVE_IMAGE, 10L
            );
            FileJpaEntity higher = persistFileWithOrderNum(
                    OwnerType.PRODUCT, 1L, FileSort.PRODUCT_REPRESENTATIVE_IMAGE, 99L
            );
            entityManager.flush();
            entityManager.clear();

            // when
            List<FileJpaEntity> result = fileJpaRepository.findByOwnerTypeAndOwnerIdAndSort(
                    OwnerType.PRODUCT,
                    1L,
                    FileSort.PRODUCT_REPRESENTATIVE_IMAGE.name(),
                    PageRequest.of(0, 10)
            );

            // then
            assertThat(result)
                    .extracting(FileJpaEntity::getId)
                    .containsExactly(higher.getId(), lower.getId());
        }

        @Test
        @DisplayName("다른 정렬 기준의 파일은 제외된다")
        void excludesOtherSort() {
            // given
            persistFileWithOrderNum(OwnerType.PRODUCT, 1L, FileSort.PRODUCT_REPRESENTATIVE_IMAGE, 10L);
            persistFileWithOrderNum(OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE, 20L);
            entityManager.flush();
            entityManager.clear();

            // when
            List<FileJpaEntity> result = fileJpaRepository.findByOwnerTypeAndOwnerIdAndSort(
                    OwnerType.PRODUCT,
                    1L,
                    FileSort.PRODUCT_REPRESENTATIVE_IMAGE.name(),
                    PageRequest.of(0, 10)
            );

            // then
            assertThat(result)
                    .hasSize(1)
                    .extracting(FileJpaEntity::getSort)
                    .containsExactly(FileSort.PRODUCT_REPRESENTATIVE_IMAGE.name());
        }

        @Test
        @DisplayName("Pageable 크기만큼 결과가 제한된다")
        void limitsByPageableSize() {
            // given
            persistFileWithOrderNum(OwnerType.PRODUCT, 1L, FileSort.PRODUCT_REPRESENTATIVE_IMAGE, 30L);
            persistFileWithOrderNum(OwnerType.PRODUCT, 1L, FileSort.PRODUCT_REPRESENTATIVE_IMAGE, 20L);
            persistFileWithOrderNum(OwnerType.PRODUCT, 1L, FileSort.PRODUCT_REPRESENTATIVE_IMAGE, 10L);
            entityManager.flush();
            entityManager.clear();

            // when
            List<FileJpaEntity> result = fileJpaRepository.findByOwnerTypeAndOwnerIdAndSort(
                    OwnerType.PRODUCT,
                    1L,
                    FileSort.PRODUCT_REPRESENTATIVE_IMAGE.name(),
                    PageRequest.of(0, 2)
            );

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("ACTIVE가 아닌 상태의 파일은 조회되지 않는다")
        void excludesNonActiveStatus() {
            // given
            FileJpaEntity active = persistFileWithOrderNum(
                    OwnerType.PRODUCT, 1L, FileSort.PRODUCT_REPRESENTATIVE_IMAGE, 50L
            );
            FileJpaEntity inactive = persistFileWithOrderNum(
                    OwnerType.PRODUCT, 1L, FileSort.PRODUCT_REPRESENTATIVE_IMAGE, 60L
            );
            entityManager.flush();
            markStatus(inactive.getId(), EntityStatus.INACTIVE);

            // when
            List<FileJpaEntity> result = fileJpaRepository.findByOwnerTypeAndOwnerIdAndSort(
                    OwnerType.PRODUCT,
                    1L,
                    FileSort.PRODUCT_REPRESENTATIVE_IMAGE.name(),
                    PageRequest.of(0, 10)
            );

            // then
            assertThat(result)
                    .extracting(FileJpaEntity::getId)
                    .containsExactly(active.getId());
        }
    }

    @Nested
    @DisplayName("findByIds")
    class FindByIds {

        @Test
        @DisplayName("전달된 ID 목록에 포함된 ACTIVE 파일만 조회한다")
        void returnsActiveFilesMatchingIds() {
            // given
            FileJpaEntity first = persistFileWithOrderNum(
                    OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE, 10L
            );
            FileJpaEntity second = persistFileWithOrderNum(
                    OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE, 20L
            );
            FileJpaEntity other = persistFileWithOrderNum(
                    OwnerType.PRODUCT, 2L, FileSort.PRODUCT_CATALOG_IMAGE, 30L
            );
            entityManager.flush();
            entityManager.clear();

            // when
            List<FileJpaEntity> result = fileJpaRepository.findByIds(
                    List.of(first.getId(), second.getId())
            );

            // then
            assertThat(result)
                    .extracting(FileJpaEntity::getId)
                    .containsExactlyInAnyOrder(first.getId(), second.getId())
                    .doesNotContain(other.getId());
        }

        @Test
        @DisplayName("결과는 orderNum 내림차순으로 정렬된다")
        void returnsOrderedByOrderNumDesc() {
            // given
            FileJpaEntity low = persistFileWithOrderNum(
                    OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE, 10L
            );
            FileJpaEntity high = persistFileWithOrderNum(
                    OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE, 99L
            );
            entityManager.flush();
            entityManager.clear();

            // when
            List<FileJpaEntity> result = fileJpaRepository.findByIds(
                    List.of(low.getId(), high.getId())
            );

            // then
            assertThat(result)
                    .extracting(FileJpaEntity::getId)
                    .containsExactly(high.getId(), low.getId());
        }

        @Test
        @DisplayName("ACTIVE가 아닌 상태의 파일은 제외된다")
        void excludesNonActiveStatus() {
            // given
            FileJpaEntity active = persistFileWithOrderNum(
                    OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE, 10L
            );
            FileJpaEntity inactive = persistFileWithOrderNum(
                    OwnerType.PRODUCT, 1L, FileSort.PRODUCT_CATALOG_IMAGE, 20L
            );
            entityManager.flush();
            markStatus(inactive.getId(), EntityStatus.INACTIVE);

            // when
            List<FileJpaEntity> result = fileJpaRepository.findByIds(
                    List.of(active.getId(), inactive.getId())
            );

            // then
            assertThat(result)
                    .extracting(FileJpaEntity::getId)
                    .containsExactly(active.getId());
        }
    }

    private FileJpaEntity persistFile(OwnerType ownerType, Long ownerId, FileSort sort, Long orderNum) {
        return persistFileWithOrderNum(ownerType, ownerId, sort, orderNum);
    }

    private FileJpaEntity persistFileWithOrderNum(OwnerType ownerType, Long ownerId, FileSort sort, Long orderNum) {
        FileDomain domain = FileDomain.from(FileDomainSnapshotState.builder()
                .ownerType(ownerType)
                .ownerId(ownerId)
                .sort(sort)
                .extension("jpg")
                .name("test-image.jpg")
                .storageUrl("https://bucket.s3.amazonaws.com/" + ownerType.name() + "/" + ownerId + "/" + orderNum + ".jpg")
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .status(EntityStatus.ACTIVE)
                .orderNum(orderNum)
                .userId(100L)
                .ownerKey("owner-key")
                .build());
        FileJpaEntity entity = FileJpaEntity.from(domain, domain.getStorageUrl());
        FileJpaEntity saved = fileJpaRepository.saveAndFlush(entity);
        saved.setIdToOrderNum();
        if (orderNum != null) {
            entityManager.createNativeQuery("UPDATE files SET order_num = :orderNum WHERE id = :id")
                    .setParameter("orderNum", orderNum)
                    .setParameter("id", saved.getId())
                    .executeUpdate();
        }
        return saved;
    }

    private void markStatus(Long id, EntityStatus status) {
        entityManager.createNativeQuery("UPDATE files SET status = :status WHERE id = :id")
                .setParameter("status", status.name())
                .setParameter("id", id)
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }
}
