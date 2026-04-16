package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.gifticon.GifticonCategory;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategorySnapshotState;
import com.personal.marketnote.reward.port.in.result.gifticon.GetAdminGifticonCategoriesResult;
import com.personal.marketnote.reward.port.in.result.gifticon.GifticonCategoryItemResult;
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
class GetAdminGifticonCategoriesServiceTest {

    @InjectMocks
    private GetAdminGifticonCategoriesService getAdminGifticonCategoriesService;

    @Mock
    private FindGifticonCategoryPort findGifticonCategoryPort;

    @Test
    @DisplayName("전체 카테고리 목록을 orderNum 오름차순으로 조회한다")
    void shouldReturnAllCategoriesOrderedByOrderNumAsc() {
        // given
        GifticonCategory category1 = createCategory(1L, "1", "커피", null, true, 1);
        GifticonCategory category2 = createCategory(2L, "2", "편의점", "편의점/마트", true, 2);
        GifticonCategory category3 = createCategory(3L, "3", "외식", null, false, null);
        when(findGifticonCategoryPort.findAllOrderByOrderNumAsc())
                .thenReturn(List.of(category1, category2, category3));

        // when
        GetAdminGifticonCategoriesResult result = getAdminGifticonCategoriesService.getAdminGifticonCategories();

        // then
        assertThat(result.categories()).hasSize(3);
        verify(findGifticonCategoryPort).findAllOrderByOrderNumAsc();

        GifticonCategoryItemResult first = result.categories().get(0);
        assertThat(first.id()).isEqualTo(1L);
        assertThat(first.categoryCode()).isEqualTo("1");
        assertThat(first.categoryName()).isEqualTo("커피");
        assertThat(first.displayName()).isNull();
        assertThat(first.effectiveDisplayName()).isEqualTo("커피");
        assertThat(first.exposed()).isTrue();
        assertThat(first.orderNum()).isEqualTo(1);

        GifticonCategoryItemResult second = result.categories().get(1);
        assertThat(second.displayName()).isEqualTo("편의점/마트");
        assertThat(second.effectiveDisplayName()).isEqualTo("편의점/마트");
    }

    @Test
    @DisplayName("카테고리가 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyListWhenNoCategoriesExist() {
        // given
        when(findGifticonCategoryPort.findAllOrderByOrderNumAsc())
                .thenReturn(List.of());

        // when
        GetAdminGifticonCategoriesResult result = getAdminGifticonCategoriesService.getAdminGifticonCategories();

        // then
        assertThat(result.categories()).isEmpty();
        verify(findGifticonCategoryPort).findAllOrderByOrderNumAsc();
    }

    private GifticonCategory createCategory(Long id, String categoryCode, String categoryName,
                                            String displayName, boolean exposed, Integer orderNum) {
        return GifticonCategory.from(GifticonCategorySnapshotState.builder()
                .id(id)
                .categoryCode(categoryCode)
                .categoryName(categoryName)
                .displayName(displayName)
                .exposed(exposed)
                .orderNum(orderNum)
                .createdAt(LocalDateTime.of(2026, 4, 3, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 3, 10, 0))
                .build());
    }
}
