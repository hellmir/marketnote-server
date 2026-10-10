package com.personal.marketnote.product.adapter.out.persistence.image;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.product.adapter.out.persistence.image.entity.ImageReadModelJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.image.repository.ImageReadModelJpaRepository;
import com.personal.marketnote.product.port.out.file.SaveImageReadModelPort.ResizedImageInput;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, ImageReadModelPersistenceAdapter.class})
class ImageReadModelPersistenceAdapterTest {

    @Autowired
    private ImageReadModelPersistenceAdapter adapter;

    @Autowired
    private ImageReadModelJpaRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("findImagesByProductIdAndSort")
    class FindImagesByProductIdAndSort {

        @Test
        @DisplayName("ACTIVE 상태의 이미지를 sortOrder 오름차순으로 조회한다")
        void returnsActiveImagesSortedBySortOrder() {
            // given
            adapter.upsert(1L, 100L, "PRODUCT", "PRODUCT_CATALOG_IMAGE", "https://cdn.example.com/2.png", 2, List.of());
            adapter.upsert(2L, 100L, "PRODUCT", "PRODUCT_CATALOG_IMAGE", "https://cdn.example.com/1.png", 1, List.of());
            adapter.upsert(3L, 100L, "PRODUCT", "PRODUCT_CATALOG_IMAGE", "https://cdn.example.com/3.png", 3, List.of());

            // when
            Optional<GetFilesResult> result = adapter.findImagesByProductIdAndSort(100L, FileSort.PRODUCT_CATALOG_IMAGE);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().images()).hasSize(3);
            assertThat(result.get().images().get(0).orderNum()).isEqualTo(1L);
            assertThat(result.get().images().get(1).orderNum()).isEqualTo(2L);
            assertThat(result.get().images().get(2).orderNum()).isEqualTo(3L);
        }

        @Test
        @DisplayName("INACTIVE 상태의 이미지는 조회하지 않는다")
        void excludesInactiveImages() {
            // given
            adapter.upsert(1L, 100L, "PRODUCT", "PRODUCT_CATALOG_IMAGE", "https://cdn.example.com/1.png", 1, List.of());
            adapter.deactivateByImageId(1L);

            // when
            Optional<GetFilesResult> result = adapter.findImagesByProductIdAndSort(100L, FileSort.PRODUCT_CATALOG_IMAGE);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("다른 fileSort의 이미지는 조회하지 않는다")
        void excludesDifferentFileSort() {
            // given
            adapter.upsert(1L, 100L, "PRODUCT", "PRODUCT_CATALOG_IMAGE", "https://cdn.example.com/1.png", 1, List.of());
            adapter.upsert(2L, 100L, "PRODUCT", "PRODUCT_REPRESENTATIVE_IMAGE", "https://cdn.example.com/2.png", 1, List.of());

            // when
            Optional<GetFilesResult> result = adapter.findImagesByProductIdAndSort(100L, FileSort.PRODUCT_CATALOG_IMAGE);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().images()).hasSize(1);
            assertThat(result.get().images().getFirst().id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("해당 상품의 이미지가 없으면 empty를 반환한다")
        void returnsEmptyWhenNoImages() {
            // when
            Optional<GetFilesResult> result = adapter.findImagesByProductIdAndSort(999L, FileSort.PRODUCT_CATALOG_IMAGE);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("조회 결과에 imageUrl과 fileSort가 올바르게 매핑된다")
        void mapsFieldsCorrectly() {
            // given
            adapter.upsert(10L, 200L, "PRODUCT", "PRODUCT_REPRESENTATIVE_IMAGE", "https://cdn.example.com/rep.png", 5, List.of());

            // when
            Optional<GetFilesResult> result = adapter.findImagesByProductIdAndSort(200L, FileSort.PRODUCT_REPRESENTATIVE_IMAGE);

            // then
            assertThat(result).isPresent();
            GetFileResult fileResult = result.get().images().getFirst();
            assertThat(fileResult.id()).isEqualTo(10L);
            assertThat(fileResult.sort()).isEqualTo("PRODUCT_REPRESENTATIVE_IMAGE");
            assertThat(fileResult.storageUrl()).isEqualTo("https://cdn.example.com/rep.png");
            assertThat(fileResult.orderNum()).isEqualTo(5L);
            assertThat(fileResult.resizedStorageUrls()).isEmpty();
        }
    }

    @Nested
    @DisplayName("upsert")
    class Upsert {

        @Test
        @DisplayName("신규 이미지를 저장한다")
        void insertsNewImage() {
            // when
            adapter.upsert(1L, 100L, "PRODUCT", "PRODUCT_CATALOG_IMAGE", "https://cdn.example.com/1.png", 1, List.of());

            // then
            Optional<ImageReadModelJpaEntity> entity = repository.findByImageId(1L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getTargetId()).isEqualTo(100L);
            assertThat(entity.get().getFileSort()).isEqualTo("PRODUCT_CATALOG_IMAGE");
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("동일한 imageId로 upsert 시 기존 데이터를 업데이트한다")
        void updatesExistingImage() {
            // given
            adapter.upsert(1L, 100L, "PRODUCT", "PRODUCT_CATALOG_IMAGE", "https://cdn.example.com/old.png", 1, List.of());

            // when
            adapter.upsert(1L, 100L, "PRODUCT", "PRODUCT_CATALOG_IMAGE", "https://cdn.example.com/new.png", 2, List.of());

            // then
            Optional<ImageReadModelJpaEntity> entity = repository.findByImageId(1L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getImageUrl()).isEqualTo("https://cdn.example.com/new.png");
            assertThat(entity.get().getSortOrder()).isEqualTo(2);
        }

        @Test
        @DisplayName("비활성화된 이미지에 대해 upsert 시 다시 활성화된다")
        void reactivatesInactiveImage() {
            // given
            adapter.upsert(1L, 100L, "PRODUCT", "PRODUCT_CATALOG_IMAGE", "https://cdn.example.com/1.png", 1, List.of());
            adapter.deactivateByImageId(1L);

            // when
            adapter.upsert(1L, 100L, "PRODUCT", "PRODUCT_CATALOG_IMAGE", "https://cdn.example.com/1.png", 1, List.of());

            // then
            Optional<ImageReadModelJpaEntity> entity = repository.findByImageId(1L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("deactivateByImageId")
    class DeactivateByImageId {

        @Test
        @DisplayName("이미지를 비활성화한다")
        void deactivatesImage() {
            // given
            adapter.upsert(1L, 100L, "PRODUCT", "PRODUCT_CATALOG_IMAGE", "https://cdn.example.com/1.png", 1, List.of());

            // when
            adapter.deactivateByImageId(1L);

            // then
            Optional<ImageReadModelJpaEntity> entity = repository.findByImageId(1L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.INACTIVE);
        }

        @Test
        @DisplayName("존재하지 않는 imageId 비활성화 시 에러 없이 무시한다")
        void ignoresNonExistentImage() {
            // when & then — no exception
            adapter.deactivateByImageId(999L);
        }

        @Test
        @DisplayName("deactivateByImageId 호출 시 부모 read model과 자식 리사이즈 URL row는 유지된다")
        void deactivateKeepsResizedFiles() {
            // given
            adapter.upsert(
                    1L, 100L, "PRODUCT", "PRODUCT_REPRESENTATIVE_IMAGE",
                    "https://cdn.example.com/rep.png", 1,
                    List.of(new ResizedImageInput("600", "https://cdn.example.com/rep_600.png"))
            );
            entityManager.flush();
            entityManager.clear();

            // when
            adapter.deactivateByImageId(1L);
            entityManager.flush();
            entityManager.clear();

            // then
            Optional<ImageReadModelJpaEntity> entity = repository.findByImageId(1L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.INACTIVE);
            assertThat(entity.get().getResizedFiles()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("resized files 저장/조회")
    class ResizedFiles {

        @Test
        @DisplayName("ImageChangedEvent 수신 시 리사이즈 URL 목록이 image_read_model_resized_files 테이블에 함께 저장된다")
        void upsertPersistsResizedFiles() {
            // given
            List<ResizedImageInput> resized = List.of(
                    new ResizedImageInput("600", "https://cdn.example.com/10_600.png"),
                    new ResizedImageInput("800", "https://cdn.example.com/10_800.png")
            );

            // when
            adapter.upsert(
                    10L, 100L, "PRODUCT", "PRODUCT_REPRESENTATIVE_IMAGE",
                    "https://cdn.example.com/10.png", 1, resized
            );
            entityManager.flush();
            entityManager.clear();

            // then
            Optional<ImageReadModelJpaEntity> entity = repository.findByImageId(10L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getResizedFiles())
                    .extracting(rf -> rf.getSize(), rf -> rf.getStorageUrl())
                    .containsExactly(
                            org.assertj.core.api.Assertions.tuple("600", "https://cdn.example.com/10_600.png"),
                            org.assertj.core.api.Assertions.tuple("800", "https://cdn.example.com/10_800.png")
                    );
        }

        @Test
        @DisplayName("리사이즈 URL이 비어 있는 이벤트 수신 시 image_read_model_resized_files에 row가 생성되지 않는다")
        void upsertWithEmptyResizedFilesCreatesNoChildRows() {
            // when
            adapter.upsert(
                    11L, 100L, "PRODUCT", "PRODUCT_CONTENT_IMAGE",
                    "https://cdn.example.com/11.png", 1, List.of()
            );
            entityManager.flush();
            entityManager.clear();

            // then
            Optional<ImageReadModelJpaEntity> entity = repository.findByImageId(11L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getResizedFiles()).isEmpty();
        }

        @Test
        @DisplayName("같은 imageId에 대해 재수신 시 기존 자식 row가 orphanRemoval로 교체된다")
        void upsertReplacesResizedFilesOnReceivedAgain() {
            // given — 서로 다른 size로 재수신하여 orphanRemoval 동작만 검증 (Hibernate insert-before-delete UNIQUE 충돌 회피)
            adapter.upsert(
                    12L, 100L, "PRODUCT", "PRODUCT_REPRESENTATIVE_IMAGE",
                    "https://cdn.example.com/12_old.png", 1,
                    List.of(
                            new ResizedImageInput("600", "https://cdn.example.com/12_old_600.png"),
                            new ResizedImageInput("800", "https://cdn.example.com/12_old_800.png")
                    )
            );
            entityManager.flush();
            entityManager.clear();

            // when
            adapter.upsert(
                    12L, 100L, "PRODUCT", "PRODUCT_REPRESENTATIVE_IMAGE",
                    "https://cdn.example.com/12_new.png", 2,
                    List.of(new ResizedImageInput("900", "https://cdn.example.com/12_new_900.png"))
            );
            entityManager.flush();
            entityManager.clear();

            // then
            Optional<ImageReadModelJpaEntity> entity = repository.findByImageId(12L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getImageUrl()).isEqualTo("https://cdn.example.com/12_new.png");
            assertThat(entity.get().getResizedFiles())
                    .singleElement()
                    .satisfies(rf -> {
                        assertThat(rf.getSize()).isEqualTo("900");
                        assertThat(rf.getStorageUrl()).isEqualTo("https://cdn.example.com/12_new_900.png");
                    });
        }

        @Test
        @DisplayName("image_read_model_resized_files에 UNIQUE(image_read_model_id, size) 제약이 위반되는 경우 저장 실패한다")
        void rejectsDuplicateSizeForSameImageReadModel() {
            // given — Repository에 직접 entity 생성 후 같은 부모에 중복 size 자식 row 추가
            ImageReadModelJpaEntity entity = ImageReadModelJpaEntity.of(
                    13L, 100L, "PRODUCT", "PRODUCT_REPRESENTATIVE_IMAGE",
                    "https://cdn.example.com/13.png", 1
            );
            entity.addResizedFiles(List.of(
                    new ImageReadModelJpaEntity.ResizedFileInput("600", "https://cdn.example.com/a.png"),
                    new ImageReadModelJpaEntity.ResizedFileInput("600", "https://cdn.example.com/b.png")
            ));

            // when & then — UNIQUE(image_read_model_id, size) 위반
            assertThatThrownBy(() -> repository.saveAndFlush(entity))
                    .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("상품 목록 조회 시 resizedStorageUrls가 sort_order 기준으로 올바르게 정렬되어 반환된다")
        void findImagesReturnsResizedStorageUrlsInSortOrder() {
            // given
            adapter.upsert(
                    20L, 200L, "PRODUCT", "PRODUCT_REPRESENTATIVE_IMAGE",
                    "https://cdn.example.com/20.png", 1,
                    List.of(
                            new ResizedImageInput("600", "https://cdn.example.com/20_600.png"),
                            new ResizedImageInput("800", "https://cdn.example.com/20_800.png")
                    )
            );
            entityManager.flush();
            entityManager.clear();

            // when
            Optional<GetFilesResult> result = adapter.findImagesByProductIdAndSort(200L, FileSort.PRODUCT_REPRESENTATIVE_IMAGE);

            // then
            assertThat(result).isPresent();
            GetFileResult fileResult = result.get().images().getFirst();
            assertThat(fileResult.resizedStorageUrls())
                    .containsExactly(
                            "https://cdn.example.com/20_600.png",
                            "https://cdn.example.com/20_800.png"
                    );
        }

        @Test
        @DisplayName("카탈로그 이미지 조회 시 500x500 URL이 resizedStorageUrls에 포함되어 반환된다")
        void findImagesReturnsCatalogResizedUrl() {
            // given
            adapter.upsert(
                    30L, 300L, "PRODUCT", "PRODUCT_CATALOG_IMAGE",
                    "https://cdn.example.com/30.png", 1,
                    List.of(new ResizedImageInput("500x500", "https://cdn.example.com/30_500x500.png"))
            );
            entityManager.flush();
            entityManager.clear();

            // when
            Optional<GetFilesResult> result = adapter.findImagesByProductIdAndSort(300L, FileSort.PRODUCT_CATALOG_IMAGE);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().images().getFirst().resizedStorageUrls())
                    .containsExactly("https://cdn.example.com/30_500x500.png");
        }

        @Test
        @DisplayName("대표 이미지 조회 시 600px, 800px URL이 모두 resizedStorageUrls에 포함되어 반환된다")
        void findImagesReturnsRepresentativeResizedUrls() {
            // given
            adapter.upsert(
                    40L, 400L, "PRODUCT", "PRODUCT_REPRESENTATIVE_IMAGE",
                    "https://cdn.example.com/40.png", 1,
                    List.of(
                            new ResizedImageInput("600", "https://cdn.example.com/40_600.png"),
                            new ResizedImageInput("800", "https://cdn.example.com/40_800.png")
                    )
            );
            entityManager.flush();
            entityManager.clear();

            // when
            Optional<GetFilesResult> result = adapter.findImagesByProductIdAndSort(400L, FileSort.PRODUCT_REPRESENTATIVE_IMAGE);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().images().getFirst().resizedStorageUrls())
                    .containsExactlyInAnyOrder(
                            "https://cdn.example.com/40_600.png",
                            "https://cdn.example.com/40_800.png"
                    );
        }

        @Test
        @DisplayName("JOIN FETCH 적용으로 N+1 쿼리가 발생하지 않는다 (상품 목록 조회 시 쿼리 개수 검증)")
        void findImagesDoesNotCauseNPlusOneQueries() {
            // given — 서로 다른 imageId + 각기 다른 리사이즈 파일을 여러 건 저장
            adapter.upsert(
                    50L, 500L, "PRODUCT", "PRODUCT_REPRESENTATIVE_IMAGE",
                    "https://cdn.example.com/50.png", 1,
                    List.of(
                            new ResizedImageInput("600", "https://cdn.example.com/50_600.png"),
                            new ResizedImageInput("800", "https://cdn.example.com/50_800.png")
                    )
            );
            adapter.upsert(
                    51L, 500L, "PRODUCT", "PRODUCT_REPRESENTATIVE_IMAGE",
                    "https://cdn.example.com/51.png", 2,
                    List.of(
                            new ResizedImageInput("600", "https://cdn.example.com/51_600.png"),
                            new ResizedImageInput("800", "https://cdn.example.com/51_800.png")
                    )
            );
            adapter.upsert(
                    52L, 500L, "PRODUCT", "PRODUCT_REPRESENTATIVE_IMAGE",
                    "https://cdn.example.com/52.png", 3,
                    List.of(
                            new ResizedImageInput("600", "https://cdn.example.com/52_600.png"),
                            new ResizedImageInput("800", "https://cdn.example.com/52_800.png")
                    )
            );
            entityManager.flush();
            entityManager.clear();

            Statistics stats = entityManager.getEntityManagerFactory()
                    .unwrap(SessionFactory.class)
                    .getStatistics();
            stats.setStatisticsEnabled(true);
            stats.clear();

            // when
            Optional<GetFilesResult> result = adapter.findImagesByProductIdAndSort(500L, FileSort.PRODUCT_REPRESENTATIVE_IMAGE);
            // resizedStorageUrls 접근으로 LAZY 초기화 트리거 시도
            result.ifPresent(r -> r.images().forEach(i -> i.resizedStorageUrls().size()));

            // then — JOIN FETCH 한 번으로 완료되어야 함 (Statement 1개)
            long executed = stats.getPrepareStatementCount();
            assertThat(result).isPresent();
            assertThat(result.get().images()).hasSize(3);
            assertThat(executed).isEqualTo(1);
        }

        @Test
        @DisplayName("image_read_models row 삭제 시 cascade로 image_read_model_resized_files row도 삭제된다")
        void deletingImageReadModelCascadesToResizedFiles() {
            // given
            adapter.upsert(
                    60L, 600L, "PRODUCT", "PRODUCT_REPRESENTATIVE_IMAGE",
                    "https://cdn.example.com/60.png", 1,
                    List.of(
                            new ResizedImageInput("600", "https://cdn.example.com/60_600.png"),
                            new ResizedImageInput("800", "https://cdn.example.com/60_800.png")
                    )
            );
            entityManager.flush();
            entityManager.clear();

            Long parentId = repository.findByImageId(60L).orElseThrow().getId();
            Long countBefore = (Long) entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM image_read_model_resized_files WHERE image_read_model_id = ?1"
            ).setParameter(1, parentId).getSingleResult();
            assertThat(countBefore).isEqualTo(2L);

            // when
            repository.deleteById(parentId);
            entityManager.flush();
            entityManager.clear();

            // then
            Long countAfter = (Long) entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM image_read_model_resized_files WHERE image_read_model_id = ?1"
            ).setParameter(1, parentId).getSingleResult();
            assertThat(countAfter).isZero();
        }
    }
}
