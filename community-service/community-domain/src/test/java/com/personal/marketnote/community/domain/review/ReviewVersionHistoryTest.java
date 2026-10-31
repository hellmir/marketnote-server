package com.personal.marketnote.community.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReviewVersionHistory")
class ReviewVersionHistoryTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 3, 1, 10, 0);

    @Nested
    @DisplayName("from(CreateState)")
    class FromCreateState {

        @Test
        @DisplayName("CreateState로 생성하면 id와 createdAt은 null이고 나머지 필드가 매핑된다")
        void shouldMapFieldsFromCreateState() {
            ReviewVersionHistoryCreateState state = ReviewVersionHistoryCreateState.builder()
                    .reviewId(100L)
                    .rating(Rating.of(5f))
                    .content("신규 버전 리뷰 내용")
                    .isPhoto(true)
                    .build();

            ReviewVersionHistory history = ReviewVersionHistory.from(state);

            assertThat(history.getId()).isNull();
            assertThat(history.getReviewId()).isEqualTo(100L);
            assertThat(history.getRating().getValue()).isEqualTo(5);
            assertThat(history.getContent()).isEqualTo("신규 버전 리뷰 내용");
            assertThat(history.getIsPhoto()).isTrue();
            assertThat(history.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("isPhoto가 false로 설정되면 false로 매핑된다")
        void shouldMapIsPhotoFalse() {
            ReviewVersionHistoryCreateState state = ReviewVersionHistoryCreateState.builder()
                    .reviewId(200L)
                    .rating(Rating.of(3f))
                    .content("텍스트 전용 리뷰")
                    .isPhoto(false)
                    .build();

            ReviewVersionHistory history = ReviewVersionHistory.from(state);

            assertThat(history.getIsPhoto()).isFalse();
        }
    }

    @Nested
    @DisplayName("from(SnapshotState)")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState로 복원하면 id와 createdAt까지 모든 필드가 매핑된다")
        void shouldRestoreAllFields() {
            ReviewVersionHistorySnapshotState state = ReviewVersionHistorySnapshotState.builder()
                    .id(999L)
                    .reviewId(200L)
                    .rating(Rating.of(1f))
                    .content("복원된 리뷰")
                    .isPhoto(false)
                    .createdAt(CREATED_AT)
                    .build();

            ReviewVersionHistory history = ReviewVersionHistory.from(state);

            assertThat(history.getId()).isEqualTo(999L);
            assertThat(history.getReviewId()).isEqualTo(200L);
            assertThat(history.getRating().getValue()).isEqualTo(1);
            assertThat(history.getContent()).isEqualTo("복원된 리뷰");
            assertThat(history.getIsPhoto()).isFalse();
            assertThat(history.getCreatedAt()).isEqualTo(CREATED_AT);
        }
    }
}
