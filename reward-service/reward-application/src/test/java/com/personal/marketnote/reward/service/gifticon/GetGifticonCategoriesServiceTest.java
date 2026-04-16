package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.gifticon.GifticonCategory;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategorySnapshotState;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonCategoriesResult;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonCategoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetGifticonCategoriesServiceTest {

    @InjectMocks
    private GetGifticonCategoriesService getGifticonCategoriesService;

    @Mock
    private FindGifticonCategoryPort findGifticonCategoryPort;

    @Test
    @DisplayName("노출된 카테고리 목록을 조회하면 displayName과 iconUrl을 포함한 결과를 반환한다")
    void shouldReturnExposedCategoriesWithDisplayNameAndIconUrl() {
        // given
        GifticonCategory category1 = createCategory(1L, "1", "커피", "카페", "https://img.com/coffee.png", 1);
        GifticonCategory category2 = createCategory(2L, "2", "베이커리", null, "https://img.com/bakery.png", 2);
        when(findGifticonCategoryPort.findAllExposed()).thenReturn(List.of(category1, category2));

        // when
        GetGifticonCategoriesResult result = getGifticonCategoriesService.getCategories();

        // then
        assertThat(result.categories()).hasSize(2);
        assertThat(result.categories().get(0).categoryCode()).isEqualTo("1");
        assertThat(result.categories().get(0).displayName()).isEqualTo("카페");
        assertThat(result.categories().get(0).iconUrl()).isEqualTo("https://img.com/coffee.png");
        assertThat(result.categories().get(0).orderNum()).isEqualTo(1);
        assertThat(result.categories().get(1).displayName()).isEqualTo("베이커리");
        verify(findGifticonCategoryPort).findAllExposed();
    }

    @Test
    @DisplayName("노출된 카테고리가 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyListWhenNoCategoriesExposed() {
        // given
        when(findGifticonCategoryPort.findAllExposed()).thenReturn(List.of());

        // when
        GetGifticonCategoriesResult result = getGifticonCategoriesService.getCategories();

        // then
        assertThat(result.categories()).isEmpty();
        verify(findGifticonCategoryPort).findAllExposed();
    }

    @Test
    @DisplayName("displayName이 없으면 categoryName을 effectiveDisplayName으로 반환한다")
    void shouldUseCategoryNameWhenDisplayNameIsNull() {
        // given
        GifticonCategory category = createCategory(1L, "3", "아이스크림", null, "https://img.com/ice.png", 3);
        when(findGifticonCategoryPort.findAllExposed()).thenReturn(List.of(category));

        // when
        GetGifticonCategoriesResult result = getGifticonCategoriesService.getCategories();

        // then
        assertThat(result.categories().get(0).displayName()).isEqualTo("아이스크림");
    }

    private GifticonCategory createCategory(Long id, String categoryCode, String categoryName,
                                            String displayName, String iconUrl, Integer orderNum) {
        return GifticonCategory.from(GifticonCategorySnapshotState.builder()
                .id(id)
                .categoryCode(categoryCode)
                .categoryName(categoryName)
                .displayName(displayName)
                .iconUrl(iconUrl)
                .exposed(true)
                .orderNum(orderNum)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
