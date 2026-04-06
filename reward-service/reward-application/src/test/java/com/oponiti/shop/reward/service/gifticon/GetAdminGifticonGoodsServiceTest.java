package com.oponiti.shop.reward.service.gifticon;

import com.oponiti.shop.reward.domain.gifticon.GifticonGoods;
import com.oponiti.shop.reward.domain.gifticon.GifticonGoodsSnapshotState;
import com.oponiti.shop.reward.port.in.command.gifticon.GetAdminGifticonGoodsCommand;
import com.oponiti.shop.reward.port.in.result.gifticon.GetAdminGifticonGoodsResult;
import com.oponiti.shop.reward.port.out.gifticon.FindGifticonGoodsPort;
import com.oponiti.shop.reward.port.out.gifticon.FindGifticonGoodsPort.FindAllForAdminResult;
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
class GetAdminGifticonGoodsServiceTest {

    @InjectMocks
    private GetAdminGifticonGoodsService getAdminGifticonGoodsService;

    @Mock
    private FindGifticonGoodsPort findGifticonGoodsPort;

    @Test
    @DisplayName("필터 없이 전체 상품 목록을 조회하면 페이징된 결과를 반환한다")
    void shouldReturnPaginatedResultWhenNoFilter() {
        // given
        GetAdminGifticonGoodsCommand command = new GetAdminGifticonGoodsCommand(1, 20, null, null, null);
        GifticonGoods goods = createGoods(1L, "G00001", "아메리카노", "BR001", "스타벅스", "SALE", true, 1);
        FindAllForAdminResult portResult = new FindAllForAdminResult(List.of(goods), 1L);

        when(findGifticonGoodsPort.findAllForAdmin(1, 20, "", null, ""))
                .thenReturn(portResult);

        // when
        GetAdminGifticonGoodsResult result = getAdminGifticonGoodsService.getAdminGifticonGoods(command);

        // then
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(20);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).goodsCode()).isEqualTo("G00001");
        assertThat(result.items().get(0).goodsName()).isEqualTo("아메리카노");
        verify(findGifticonGoodsPort).findAllForAdmin(1, 20, "", null, "");
    }

    @Test
    @DisplayName("goodsStatus 필터로 SALE 상품만 조회한다")
    void shouldFilterByGoodsStatus() {
        // given
        GetAdminGifticonGoodsCommand command = new GetAdminGifticonGoodsCommand(1, 10, "SALE", null, null);
        GifticonGoods goods = createGoods(1L, "G00001", "아메리카노", "BR001", "스타벅스", "SALE", false, null);
        FindAllForAdminResult portResult = new FindAllForAdminResult(List.of(goods), 1L);

        when(findGifticonGoodsPort.findAllForAdmin(1, 10, "SALE", null, ""))
                .thenReturn(portResult);

        // when
        GetAdminGifticonGoodsResult result = getAdminGifticonGoodsService.getAdminGifticonGoods(command);

        // then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).goodsStatus()).isEqualTo("SALE");
        verify(findGifticonGoodsPort).findAllForAdmin(1, 10, "SALE", null, "");
    }

    @Test
    @DisplayName("exposed 필터로 노출 상품만 조회한다")
    void shouldFilterByExposed() {
        // given
        GetAdminGifticonGoodsCommand command = new GetAdminGifticonGoodsCommand(1, 10, null, true, null);
        GifticonGoods goods = createGoods(1L, "G00001", "아메리카노", "BR001", "스타벅스", "SALE", true, 1);
        FindAllForAdminResult portResult = new FindAllForAdminResult(List.of(goods), 1L);

        when(findGifticonGoodsPort.findAllForAdmin(1, 10, "", true, ""))
                .thenReturn(portResult);

        // when
        GetAdminGifticonGoodsResult result = getAdminGifticonGoodsService.getAdminGifticonGoods(command);

        // then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).exposed()).isTrue();
        verify(findGifticonGoodsPort).findAllForAdmin(1, 10, "", true, "");
    }

    @Test
    @DisplayName("keyword 필터로 상품명 검색한다")
    void shouldFilterByKeyword() {
        // given
        GetAdminGifticonGoodsCommand command = new GetAdminGifticonGoodsCommand(1, 10, null, null, "아메리카노");
        GifticonGoods goods = createGoods(1L, "G00001", "아메리카노", "BR001", "스타벅스", "SALE", false, null);
        FindAllForAdminResult portResult = new FindAllForAdminResult(List.of(goods), 1L);

        when(findGifticonGoodsPort.findAllForAdmin(1, 10, "", null, "아메리카노"))
                .thenReturn(portResult);

        // when
        GetAdminGifticonGoodsResult result = getAdminGifticonGoodsService.getAdminGifticonGoods(command);

        // then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).goodsName()).isEqualTo("아메리카노");
        verify(findGifticonGoodsPort).findAllForAdmin(1, 10, "", null, "아메리카노");
    }

    @Test
    @DisplayName("결과가 없으면 빈 목록과 0건을 반환한다")
    void shouldReturnEmptyResultWhenNoGoods() {
        // given
        GetAdminGifticonGoodsCommand command = new GetAdminGifticonGoodsCommand(1, 20, null, null, null);
        FindAllForAdminResult portResult = new FindAllForAdminResult(List.of(), 0L);

        when(findGifticonGoodsPort.findAllForAdmin(1, 20, "", null, ""))
                .thenReturn(portResult);

        // when
        GetAdminGifticonGoodsResult result = getAdminGifticonGoodsService.getAdminGifticonGoods(command);

        // then
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(20);
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("totalPages는 totalElements와 pageSize로 올림 계산된다")
    void shouldCalculateTotalPagesCorrectly() {
        // given
        GetAdminGifticonGoodsCommand command = new GetAdminGifticonGoodsCommand(1, 10, null, null, null);
        GifticonGoods goods = createGoods(1L, "G00001", "아메리카노", "BR001", "스타벅스", "SALE", false, null);
        FindAllForAdminResult portResult = new FindAllForAdminResult(List.of(goods), 25L);

        when(findGifticonGoodsPort.findAllForAdmin(1, 10, "", null, ""))
                .thenReturn(portResult);

        // when
        GetAdminGifticonGoodsResult result = getAdminGifticonGoodsService.getAdminGifticonGoods(command);

        // then
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.totalElements()).isEqualTo(25L);
    }

    @Test
    @DisplayName("Result의 각 항목에 모든 필드가 올바르게 매핑된다")
    void shouldMapAllFieldsCorrectly() {
        // given
        GetAdminGifticonGoodsCommand command = new GetAdminGifticonGoodsCommand(1, 20, null, null, null);
        GifticonGoods goods = createGoods(1L, "G00001", "아메리카노", "BR001", "스타벅스", "SALE", true, 3);
        FindAllForAdminResult portResult = new FindAllForAdminResult(List.of(goods), 1L);

        when(findGifticonGoodsPort.findAllForAdmin(1, 20, "", null, ""))
                .thenReturn(portResult);

        // when
        GetAdminGifticonGoodsResult result = getAdminGifticonGoodsService.getAdminGifticonGoods(command);

        // then
        assertThat(result.items().get(0).goodsCode()).isEqualTo("G00001");
        assertThat(result.items().get(0).goodsName()).isEqualTo("아메리카노");
        assertThat(result.items().get(0).brandCode()).isEqualTo("BR001");
        assertThat(result.items().get(0).brandName()).isEqualTo("스타벅스");
        assertThat(result.items().get(0).categoryCode()).isEqualTo("1");
        assertThat(result.items().get(0).realPrice()).isEqualTo(5000L);
        assertThat(result.items().get(0).salePrice()).isEqualTo(4500L);
        assertThat(result.items().get(0).cashPrice()).isEqualTo(3500L);
        assertThat(result.items().get(0).goodsStatus()).isEqualTo("SALE");
        assertThat(result.items().get(0).exposed()).isTrue();
        assertThat(result.items().get(0).orderNum()).isEqualTo(3);
        assertThat(result.items().get(0).imageUrl()).isEqualTo("https://img.com/goods.png");
    }

    // --- Helper Methods ---

    private GifticonGoods createGoods(Long id, String goodsCode, String goodsName,
                                      String brandCode, String brandName,
                                      String goodsStatus, boolean exposed, Integer orderNum) {
        return GifticonGoods.from(GifticonGoodsSnapshotState.builder()
                .id(id)
                .goodsCode(goodsCode)
                .goodsName(goodsName)
                .brandCode(brandCode)
                .brandName(brandName)
                .brandImageUrl("https://img.com/brand.png")
                .categoryCode("1")
                .realPrice(5000L)
                .salePrice(4500L)
                .cashPrice(3500L)
                .imageUrl("https://img.com/goods.png")
                .description("설명")
                .validDays(30)
                .goodsStatus(goodsStatus)
                .exposed(exposed)
                .orderNum(orderNum)
                .createdAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .build());
    }
}
