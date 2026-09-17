package com.personal.marketnote.reward.domain.gifticon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class GifticonCategoryTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 11, 10, 0);

    @Test
    @DisplayName("CreateState로 기프티콘 카테고리를 생성하면 비노출 상태이다")
    void shouldCreateGifticonCategoryWithUnexposedStatus() {
        // given
        GifticonCategoryCreateState state = GifticonCategoryCreateState.builder()
                .categoryCode("CAT001")
                .categoryName("커피/음료")
                .build();

        // when
        GifticonCategory category = GifticonCategory.from(state);

        // then
        assertThat(category.getCategoryCode()).isEqualTo("CAT001");
        assertThat(category.getCategoryName()).isEqualTo("커피/음료");
        assertThat(category.isExposed()).isFalse();
        assertThat(category.getOrderNum()).isNull();
    }

    @Test
    @DisplayName("expose 호출 시 노출 상태로 변경된다")
    void shouldChangeToExposedWhenExposeCalled() {
        // given
        GifticonCategory category = GifticonCategory.from(GifticonCategoryCreateState.builder()
                .categoryCode("CAT001")
                .categoryName("커피/음료")
                .build());
        assertThat(category.isExposed()).isFalse();

        // when
        category.expose();

        // then
        assertThat(category.isExposed()).isTrue();
    }

    @Test
    @DisplayName("unexpose 호출 시 비노출 상태로 변경된다")
    void shouldChangeToUnexposedWhenUnexposeCalled() {
        // given
        GifticonCategory category = GifticonCategory.from(GifticonCategorySnapshotState.builder()
                .id(1L)
                .categoryCode("CAT001")
                .categoryName("커피/음료")
                .exposed(true)
                .createdAt(NOW)
                .modifiedAt(NOW)
                .build());
        assertThat(category.isExposed()).isTrue();

        // when
        category.unexpose();

        // then
        assertThat(category.isExposed()).isFalse();
    }

    @Test
    @DisplayName("displayName이 있으면 getEffectiveDisplayName은 displayName을 반환한다")
    void shouldReturnDisplayNameWhenPresent() {
        // given
        GifticonCategory category = GifticonCategory.from(GifticonCategorySnapshotState.builder()
                .id(1L)
                .categoryCode("CAT001")
                .categoryName("커피/음료")
                .displayName("카페 음료")
                .exposed(true)
                .createdAt(NOW)
                .modifiedAt(NOW)
                .build());

        // when & then
        assertThat(category.getEffectiveDisplayName()).isEqualTo("카페 음료");
    }

    @Test
    @DisplayName("displayName이 없으면 getEffectiveDisplayName은 categoryName으로 폴백한다")
    void shouldFallbackToCategoryNameWhenDisplayNameIsNull() {
        // given
        GifticonCategory category = GifticonCategory.from(GifticonCategorySnapshotState.builder()
                .id(1L)
                .categoryCode("CAT001")
                .categoryName("커피/음료")
                .displayName(null)
                .exposed(true)
                .createdAt(NOW)
                .modifiedAt(NOW)
                .build());

        // when & then
        assertThat(category.getEffectiveDisplayName()).isEqualTo("커피/음료");
    }
}
