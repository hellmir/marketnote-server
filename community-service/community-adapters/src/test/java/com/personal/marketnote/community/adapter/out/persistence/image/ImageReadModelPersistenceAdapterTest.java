package com.personal.marketnote.community.adapter.out.persistence.image;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.community.adapter.out.persistence.image.entity.ImageReadModelJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.image.repository.ImageReadModelJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, ImageReadModelPersistenceAdapter.class})
class ImageReadModelPersistenceAdapterTest {

    @Autowired
    private ImageReadModelPersistenceAdapter adapter;

    @Autowired
    private ImageReadModelJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("findImagesByPostIdAndSort")
    class FindImagesByPostIdAndSort {

        @Test
        @DisplayName("ACTIVE 상태의 게시글 이미지를 sortOrder 오름차순으로 조회한다")
        void returnsActiveImagesSortedBySortOrder() {
            // given
            adapter.upsert(1L, 100L, "POST", "POST_IMAGE", "https://cdn.example.com/2.png", 2);
            adapter.upsert(2L, 100L, "POST", "POST_IMAGE", "https://cdn.example.com/1.png", 1);
            adapter.upsert(3L, 100L, "POST", "POST_IMAGE", "https://cdn.example.com/3.png", 3);

            // when
            Optional<GetFilesResult> result = adapter.findImagesByPostIdAndSort(100L, FileSort.POST_IMAGE);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().images()).hasSize(3);
            assertThat(result.get().images().get(0).orderNum()).isEqualTo(1L);
            assertThat(result.get().images().get(1).orderNum()).isEqualTo(2L);
            assertThat(result.get().images().get(2).orderNum()).isEqualTo(3L);
        }

        @Test
        @DisplayName("INACTIVE 상태의 게시글 이미지는 조회하지 않는다")
        void excludesInactiveImages() {
            // given
            adapter.upsert(1L, 100L, "POST", "POST_IMAGE", "https://cdn.example.com/1.png", 1);
            adapter.deactivateByImageId(1L);

            // when
            Optional<GetFilesResult> result = adapter.findImagesByPostIdAndSort(100L, FileSort.POST_IMAGE);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("다른 fileSort의 이미지는 조회하지 않는다")
        void excludesDifferentFileSort() {
            // given
            adapter.upsert(1L, 100L, "POST", "POST_IMAGE", "https://cdn.example.com/1.png", 1);
            adapter.upsert(2L, 100L, "REVIEW", "REVIEW_IMAGE", "https://cdn.example.com/2.png", 1);

            // when
            Optional<GetFilesResult> result = adapter.findImagesByPostIdAndSort(100L, FileSort.POST_IMAGE);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().images()).hasSize(1);
            assertThat(result.get().images().getFirst().id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("해당 게시글의 이미지가 없으면 empty를 반환한다")
        void returnsEmptyWhenNoImages() {
            // when
            Optional<GetFilesResult> result = adapter.findImagesByPostIdAndSort(999L, FileSort.POST_IMAGE);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("조회 결과에 imageUrl과 fileSort가 올바르게 매핑된다")
        void mapsFieldsCorrectly() {
            // given
            adapter.upsert(10L, 200L, "POST", "POST_IMAGE", "https://cdn.example.com/post.png", 5);

            // when
            Optional<GetFilesResult> result = adapter.findImagesByPostIdAndSort(200L, FileSort.POST_IMAGE);

            // then
            assertThat(result).isPresent();
            GetFileResult fileResult = result.get().images().getFirst();
            assertThat(fileResult.id()).isEqualTo(10L);
            assertThat(fileResult.sort()).isEqualTo("POST_IMAGE");
            assertThat(fileResult.storageUrl()).isEqualTo("https://cdn.example.com/post.png");
            assertThat(fileResult.orderNum()).isEqualTo(5L);
            assertThat(fileResult.resizedStorageUrls()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findImagesByReviewIdAndSort")
    class FindImagesByReviewIdAndSort {

        @Test
        @DisplayName("ACTIVE 상태의 리뷰 이미지를 sortOrder 오름차순으로 조회한다")
        void returnsActiveImagesSortedBySortOrder() {
            // given
            adapter.upsert(1L, 100L, "REVIEW", "REVIEW_IMAGE", "https://cdn.example.com/2.png", 2);
            adapter.upsert(2L, 100L, "REVIEW", "REVIEW_IMAGE", "https://cdn.example.com/1.png", 1);
            adapter.upsert(3L, 100L, "REVIEW", "REVIEW_IMAGE", "https://cdn.example.com/3.png", 3);

            // when
            Optional<GetFilesResult> result = adapter.findImagesByReviewIdAndSort(100L, FileSort.REVIEW_IMAGE);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().images()).hasSize(3);
            assertThat(result.get().images().get(0).orderNum()).isEqualTo(1L);
            assertThat(result.get().images().get(1).orderNum()).isEqualTo(2L);
            assertThat(result.get().images().get(2).orderNum()).isEqualTo(3L);
        }

        @Test
        @DisplayName("INACTIVE 상태의 리뷰 이미지는 조회하지 않는다")
        void excludesInactiveImages() {
            // given
            adapter.upsert(1L, 100L, "REVIEW", "REVIEW_IMAGE", "https://cdn.example.com/1.png", 1);
            adapter.deactivateByImageId(1L);

            // when
            Optional<GetFilesResult> result = adapter.findImagesByReviewIdAndSort(100L, FileSort.REVIEW_IMAGE);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("다른 fileSort의 이미지는 조회하지 않는다")
        void excludesDifferentFileSort() {
            // given
            adapter.upsert(1L, 100L, "REVIEW", "REVIEW_IMAGE", "https://cdn.example.com/1.png", 1);
            adapter.upsert(2L, 100L, "POST", "POST_IMAGE", "https://cdn.example.com/2.png", 1);

            // when
            Optional<GetFilesResult> result = adapter.findImagesByReviewIdAndSort(100L, FileSort.REVIEW_IMAGE);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().images()).hasSize(1);
            assertThat(result.get().images().getFirst().id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("해당 리뷰의 이미지가 없으면 empty를 반환한다")
        void returnsEmptyWhenNoImages() {
            // when
            Optional<GetFilesResult> result = adapter.findImagesByReviewIdAndSort(999L, FileSort.REVIEW_IMAGE);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("upsert")
    class Upsert {

        @Test
        @DisplayName("신규 이미지를 저장한다")
        void insertsNewImage() {
            // when
            adapter.upsert(1L, 100L, "POST", "POST_IMAGE", "https://cdn.example.com/1.png", 1);

            // then
            Optional<ImageReadModelJpaEntity> entity = repository.findByImageId(1L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getTargetId()).isEqualTo(100L);
            assertThat(entity.get().getFileSort()).isEqualTo("POST_IMAGE");
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("동일한 imageId로 upsert 시 기존 데이터를 업데이트한다")
        void updatesExistingImage() {
            // given
            adapter.upsert(1L, 100L, "REVIEW", "REVIEW_IMAGE", "https://cdn.example.com/old.png", 1);

            // when
            adapter.upsert(1L, 100L, "REVIEW", "REVIEW_IMAGE", "https://cdn.example.com/new.png", 2);

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
            adapter.upsert(1L, 100L, "POST", "POST_IMAGE", "https://cdn.example.com/1.png", 1);
            adapter.deactivateByImageId(1L);

            // when
            adapter.upsert(1L, 100L, "POST", "POST_IMAGE", "https://cdn.example.com/1.png", 1);

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
            adapter.upsert(1L, 100L, "REVIEW", "REVIEW_IMAGE", "https://cdn.example.com/1.png", 1);

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
    }
}
