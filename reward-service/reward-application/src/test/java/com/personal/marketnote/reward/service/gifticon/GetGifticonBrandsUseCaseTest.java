package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.port.in.command.gifticon.GetGifticonBrandsCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonBrandsResult;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort.GifticonGoodsBrandProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetGifticonBrandsUseCaseTest {

    @InjectMocks
    private GetGifticonBrandsService getGifticonBrandsService;

    @Mock
    private FindGifticonGoodsPort findGifticonGoodsPort;

    @Test
    @DisplayName("카테고리 코드로 브랜드 목록을 조회하면 해당 카테고리의 브랜드를 반환한다")
    void shouldReturnBrandsForGivenCategoryCode() {
        // given
        String categoryCode = "1";
        GifticonGoodsBrandProjection brand1 = new GifticonGoodsBrandProjection("BR001", "스타벅스", "https://img.com/sb.png");
        GifticonGoodsBrandProjection brand2 = new GifticonGoodsBrandProjection("BR002", "이디야", "https://img.com/ediya.png");
        when(findGifticonGoodsPort.findDistinctBrandsByCategoryCode(categoryCode))
                .thenReturn(List.of(brand1, brand2));

        // when
        GetGifticonBrandsResult result = getGifticonBrandsService.getBrands(
                new GetGifticonBrandsCommand(categoryCode)
        );

        // then
        assertThat(result.brands()).hasSize(2);
        assertThat(result.brands().get(0).brandCode()).isEqualTo("BR001");
        assertThat(result.brands().get(0).brandName()).isEqualTo("스타벅스");
        assertThat(result.brands().get(0).brandImageUrl()).isEqualTo("https://img.com/sb.png");
        assertThat(result.brands().get(1).brandCode()).isEqualTo("BR002");
        verify(findGifticonGoodsPort).findDistinctBrandsByCategoryCode(categoryCode);
    }

    @Test
    @DisplayName("해당 카테고리에 브랜드가 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyListWhenNoBrandsInCategory() {
        // given
        String categoryCode = "99";
        when(findGifticonGoodsPort.findDistinctBrandsByCategoryCode(categoryCode))
                .thenReturn(List.of());

        // when
        GetGifticonBrandsResult result = getGifticonBrandsService.getBrands(
                new GetGifticonBrandsCommand(categoryCode)
        );

        // then
        assertThat(result.brands()).isEmpty();
        verify(findGifticonGoodsPort).findDistinctBrandsByCategoryCode(categoryCode);
    }
}
