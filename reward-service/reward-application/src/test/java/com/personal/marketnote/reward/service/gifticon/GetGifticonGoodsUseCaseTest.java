package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoodsSnapshotState;
import com.personal.marketnote.reward.domain.gifticon.GoodsStatus;
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

import static com.personal.marketnote.common.utility.ApiConstant.FIRST_PAGE_CURSOR_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetGifticonGoodsUseCaseTest {

    @InjectMocks
    private GetGifticonGoodsService getGifticonGoodsService;

    @Mock
    private FindGifticonGoodsPort findGifticonGoodsPort;

    @Test
    @DisplayName("첫 페이지 조회 시 totalElements를 포함하여 반환한다")
    void shouldIncludeTotalElementsOnFirstPage() {
        String categoryCode = "1";
        String brandCode = "BR001";
        GifticonGoods goods = createGoods(1L, "GD001", "아메리카노", 4500L, 4000L, 1);
        when(findGifticonGoodsPort.countAllExposed(categoryCode, brandCode)).thenReturn(15L);
        when(findGifticonGoodsPort.findAllExposedByCursor(categoryCode, brandCode, FIRST_PAGE_CURSOR_VALUE, 21))
                .thenReturn(List.of(goods));

        GetGifticonGoodsResult result = getGifticonGoodsService.getGoods(
                new GetGifticonGoodsCommand(categoryCode, brandCode, FIRST_PAGE_CURSOR_VALUE, 20)
        );

        assertThat(result.totalElements()).isEqualTo(15L);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isEqualTo(1L);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).goodsCode()).isEqualTo("GD001");
        assertThat(result.items().get(0).goodsName()).isEqualTo("아메리카노");
        assertThat(result.items().get(0).salePrice()).isEqualTo(4500L);
        assertThat(result.items().get(0).cashPrice()).isEqualTo(4000L);
        verify(findGifticonGoodsPort).countAllExposed(categoryCode, brandCode);
        verify(findGifticonGoodsPort).findAllExposedByCursor(categoryCode, brandCode, FIRST_PAGE_CURSOR_VALUE, 21);
    }

    @Test
    @DisplayName("첫 페이지에서 pageSize를 초과하는 결과가 오면 hasNext=true, nextCursor는 마지막 노출 아이템 id이다")
    void shouldDetectHasNextAndNextCursorOnFirstPage() {
        GifticonGoods g1 = createGoods(1L, "G1", "상품1", 1000L, 900L, 1);
        GifticonGoods g2 = createGoods(2L, "G2", "상품2", 1000L, 900L, 2);
        GifticonGoods g3 = createGoods(3L, "G3", "상품3", 1000L, 900L, 3);
        when(findGifticonGoodsPort.countAllExposed(null, null)).thenReturn(10L);
        when(findGifticonGoodsPort.findAllExposedByCursor(null, null, FIRST_PAGE_CURSOR_VALUE, 3))
                .thenReturn(List.of(g1, g2, g3));

        GetGifticonGoodsResult result = getGifticonGoodsService.getGoods(
                new GetGifticonGoodsCommand(null, null, FIRST_PAGE_CURSOR_VALUE, 2)
        );

        assertThat(result.hasNext()).isTrue();
        assertThat(result.items()).hasSize(2);
        assertThat(result.nextCursor()).isEqualTo(2L);
        assertThat(result.totalElements()).isEqualTo(10L);
    }

    @Test
    @DisplayName("첫 페이지에서 결과가 비어 있으면 totalElements=0, hasNext=false, nextCursor=null이다")
    void shouldReturnEmptyFirstPage() {
        when(findGifticonGoodsPort.countAllExposed(null, null)).thenReturn(0L);
        when(findGifticonGoodsPort.findAllExposedByCursor(null, null, FIRST_PAGE_CURSOR_VALUE, 21))
                .thenReturn(List.of());

        GetGifticonGoodsResult result = getGifticonGoodsService.getGoods(
                new GetGifticonGoodsCommand(null, null, FIRST_PAGE_CURSOR_VALUE, 20)
        );

        assertThat(result.totalElements()).isZero();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("다음 페이지 조회 시 totalElements는 null이고 countAllExposed는 호출되지 않는다")
    void shouldSkipCountOnSubsequentPages() {
        GifticonGoods g10 = createGoods(10L, "G10", "상품10", 1000L, 900L, 10);
        when(findGifticonGoodsPort.findAllExposedByCursor(null, null, 5L, 21))
                .thenReturn(List.of(g10));

        GetGifticonGoodsResult result = getGifticonGoodsService.getGoods(
                new GetGifticonGoodsCommand(null, null, 5L, 20)
        );

        assertThat(result.totalElements()).isNull();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isEqualTo(10L);
        assertThat(result.items()).hasSize(1);
        verify(findGifticonGoodsPort, never()).countAllExposed(null, null);
    }

    @Test
    @DisplayName("다음 페이지에서 pageSize를 초과하면 hasNext=true이고 nextCursor는 잘라낸 마지막 아이템의 id이다")
    void shouldDetectHasNextOnSubsequentPage() {
        GifticonGoods g11 = createGoods(11L, "G11", "상품11", 1000L, 900L, 11);
        GifticonGoods g12 = createGoods(12L, "G12", "상품12", 1000L, 900L, 12);
        GifticonGoods g13 = createGoods(13L, "G13", "상품13", 1000L, 900L, 13);
        when(findGifticonGoodsPort.findAllExposedByCursor(null, null, 10L, 3))
                .thenReturn(List.of(g11, g12, g13));

        GetGifticonGoodsResult result = getGifticonGoodsService.getGoods(
                new GetGifticonGoodsCommand(null, null, 10L, 2)
        );

        assertThat(result.hasNext()).isTrue();
        assertThat(result.items()).hasSize(2);
        assertThat(result.nextCursor()).isEqualTo(12L);
        assertThat(result.totalElements()).isNull();
    }

    @Test
    @DisplayName("다음 페이지에서 pageSize 이하로 조회되면 hasNext=false이고 nextCursor는 마지막 아이템 id이다")
    void shouldMarkLastPageOnSubsequentPage() {
        GifticonGoods g20 = createGoods(20L, "G20", "상품20", 1000L, 900L, 20);
        when(findGifticonGoodsPort.findAllExposedByCursor(null, null, 15L, 21))
                .thenReturn(List.of(g20));

        GetGifticonGoodsResult result = getGifticonGoodsService.getGoods(
                new GetGifticonGoodsCommand(null, null, 15L, 20)
        );

        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isEqualTo(20L);
        assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("카테고리/브랜드가 null이면 port에도 null로 전달한다")
    void shouldPassNullFiltersAsNull() {
        when(findGifticonGoodsPort.countAllExposed(null, null)).thenReturn(0L);
        when(findGifticonGoodsPort.findAllExposedByCursor(null, null, FIRST_PAGE_CURSOR_VALUE, 21))
                .thenReturn(List.of());

        getGifticonGoodsService.getGoods(
                new GetGifticonGoodsCommand(null, null, FIRST_PAGE_CURSOR_VALUE, 20)
        );

        verify(findGifticonGoodsPort).findAllExposedByCursor(null, null, FIRST_PAGE_CURSOR_VALUE, 21);
        verify(findGifticonGoodsPort).countAllExposed(null, null);
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
                .goodsStatus(GoodsStatus.SALE)
                .exposed(true)
                .orderNum(orderNum)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build()
        );
    }
}
