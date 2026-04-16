package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoodsSnapshotState;
import com.personal.marketnote.reward.port.in.result.gifticon.GetPopularGifticonGoodsResult;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
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
class GetPopularGifticonGoodsServiceTest {

    @InjectMocks
    private GetPopularGifticonGoodsService getPopularGifticonGoodsService;

    @Mock
    private FindGifticonGoodsPort findGifticonGoodsPort;

    @Test
    @DisplayName("인기 상품 목록을 조회하면 최대 10개의 상품 정보를 반환한다")
    void shouldReturnPopularGoodsUpToLimit() {
        // given
        GifticonGoods goods1 = createGoods(1L, "GD001", "아메리카노", 4500L, 4000L);
        GifticonGoods goods2 = createGoods(2L, "GD002", "카페라떼", 5500L, 5000L);
        when(findGifticonGoodsPort.findAllPopularAndExposed(10)).thenReturn(List.of(goods1, goods2));

        // when
        GetPopularGifticonGoodsResult result = getPopularGifticonGoodsService.getPopularGoods();

        // then
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).goodsCode()).isEqualTo("GD001");
        assertThat(result.items().get(0).goodsName()).isEqualTo("아메리카노");
        assertThat(result.items().get(0).salePrice()).isEqualTo(4500L);
        assertThat(result.items().get(0).cashPrice()).isEqualTo(4000L);
        assertThat(result.items().get(0).imageUrl()).isEqualTo("https://img.com/goods.png");
        assertThat(result.items().get(1).goodsCode()).isEqualTo("GD002");
        verify(findGifticonGoodsPort).findAllPopularAndExposed(10);
    }

    @Test
    @DisplayName("인기 상품이 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyListWhenNoPopularGoods() {
        // given
        when(findGifticonGoodsPort.findAllPopularAndExposed(10)).thenReturn(List.of());

        // when
        GetPopularGifticonGoodsResult result = getPopularGifticonGoodsService.getPopularGoods();

        // then
        assertThat(result.items()).isEmpty();
        verify(findGifticonGoodsPort).findAllPopularAndExposed(10);
    }

    private GifticonGoods createGoods(Long id, String goodsCode, String goodsName,
                                      Long salePrice, Long cashPrice) {
        return GifticonGoods.from(GifticonGoodsSnapshotState.builder()
                .id(id)
                .goodsCode(goodsCode)
                .goodsName(goodsName)
                .brandCode("BR001")
                .brandName("스타벅스")
                .brandImageUrl("https://img.com/sb.png")
                .categoryCode("1")
                .realPrice(5000L)
                .salePrice(salePrice)
                .cashPrice(cashPrice)
                .imageUrl("https://img.com/goods.png")
                .description("설명")
                .validDays(30)
                .goodsStatus("SALE")
                .exposed(true)
                .popular(true)
                .orderNum(1)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
