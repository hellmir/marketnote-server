package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoodsSnapshotState;
import com.personal.marketnote.reward.port.in.command.gifticon.GetGifticonGoodsCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonGoodsResult;
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
class GetGifticonGoodsServiceTest {

    @InjectMocks
    private GetGifticonGoodsService getGifticonGoodsService;

    @Mock
    private FindGifticonGoodsPort findGifticonGoodsPort;

    @Test
    @DisplayName("카테고리와 브랜드로 상품 목록을 조회하면 페이징 결과를 반환한다")
    void shouldReturnPaginatedGoodsForCategoryAndBrand() {
        // given
        String categoryCode = "1";
        String brandCode = "BR001";
        GifticonGoods goods = createGoods(1L, "GD001", "아메리카노", 4500L, 4000L, 1);
        when(findGifticonGoodsPort.countAllExposed(categoryCode, brandCode)).thenReturn(1L);
        when(findGifticonGoodsPort.findAllExposed(categoryCode, brandCode, 1, 20)).thenReturn(List.of(goods));

        // when
        GetGifticonGoodsResult result = getGifticonGoodsService.getGoods(
                new GetGifticonGoodsCommand(categoryCode, brandCode, 1, 20)
        );

        // then
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(20);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).goodsCode()).isEqualTo("GD001");
        assertThat(result.items().get(0).goodsName()).isEqualTo("아메리카노");
        assertThat(result.items().get(0).salePrice()).isEqualTo(4500L);
        assertThat(result.items().get(0).cashPrice()).isEqualTo(4000L);
        verify(findGifticonGoodsPort).countAllExposed(categoryCode, brandCode);
        verify(findGifticonGoodsPort).findAllExposed(categoryCode, brandCode, 1, 20);
    }

    @Test
    @DisplayName("상품이 없으면 빈 목록과 totalPages 0을 반환한다")
    void shouldReturnEmptyListWhenNoGoods() {
        // given
        when(findGifticonGoodsPort.countAllExposed(null, null)).thenReturn(0L);
        when(findGifticonGoodsPort.findAllExposed(null, null, 1, 20)).thenReturn(List.of());

        // when
        GetGifticonGoodsResult result = getGifticonGoodsService.getGoods(
                new GetGifticonGoodsCommand(null, null, 1, 20)
        );

        // then
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("총 상품 수가 pageSize보다 크면 totalPages가 올바르게 계산된다")
    void shouldCalculateTotalPagesCorrectly() {
        // given
        when(findGifticonGoodsPort.countAllExposed(null, null)).thenReturn(45L);
        when(findGifticonGoodsPort.findAllExposed(null, null, 1, 20)).thenReturn(List.of());

        // when
        GetGifticonGoodsResult result = getGifticonGoodsService.getGoods(
                new GetGifticonGoodsCommand(null, null, 1, 20)
        );

        // then
        assertThat(result.totalPages()).isEqualTo(3);
    }

    private GifticonGoods createGoods(Long id, String goodsCode, String goodsName,
                                       Long salePrice, Long cashPrice, Integer orderNum) {
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
                .orderNum(orderNum)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
