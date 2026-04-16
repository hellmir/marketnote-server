package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoodsSnapshotState;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointSnapshotState;
import com.personal.marketnote.reward.port.in.command.gifticon.GetGifticonGoodsDetailCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonGoodsDetailResult;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetGifticonGoodsDetailServiceTest {

    @InjectMocks
    private GetGifticonGoodsDetailService getGifticonGoodsDetailService;

    @Mock
    private FindGifticonGoodsPort findGifticonGoodsPort;

    @Mock
    private GetUserPointUseCase getUserPointUseCase;

    @Test
    @DisplayName("노출된 판매 중 상품을 조회하면 상세 정보와 캐시 잔액을 반환한다")
    void shouldReturnGoodsDetailWithCashBalance() {
        // given
        String goodsCode = "GD001";
        Long userId = 1L;
        GifticonGoods goods = createGoods(goodsCode, true, "SALE");
        UserPoint userPoint = createUserPoint(userId, 10000L);
        when(findGifticonGoodsPort.findByGoodsCode(goodsCode)).thenReturn(Optional.of(goods));
        when(getUserPointUseCase.getUserPoint(userId)).thenReturn(userPoint);

        // when
        GetGifticonGoodsDetailResult result = getGifticonGoodsDetailService.getGoodsDetail(
                new GetGifticonGoodsDetailCommand(goodsCode, userId)
        );

        // then
        assertThat(result.goodsCode()).isEqualTo("GD001");
        assertThat(result.goodsName()).isEqualTo("아메리카노");
        assertThat(result.salePrice()).isEqualTo(4500L);
        assertThat(result.cashPrice()).isEqualTo(4000L);
        assertThat(result.description()).isEqualTo("맛있는 커피");
        assertThat(result.validDays()).isEqualTo(30);
        assertThat(result.userCashBalance()).isEqualTo(10000L);
        verify(findGifticonGoodsPort).findByGoodsCode(goodsCode);
        verify(getUserPointUseCase).getUserPoint(userId);
    }

    @Test
    @DisplayName("존재하지 않는 상품 코드로 조회하면 GifticonGoodsNotFoundException이 발생한다")
    void shouldThrowWhenGoodsNotFound() {
        // given
        String goodsCode = "INVALID";
        Long userId = 1L;
        when(findGifticonGoodsPort.findByGoodsCode(goodsCode)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getGifticonGoodsDetailService.getGoodsDetail(
                new GetGifticonGoodsDetailCommand(goodsCode, userId)
        )).isInstanceOf(GifticonGoodsNotFoundException.class);
    }

    @Test
    @DisplayName("노출되지 않은 상품을 조회하면 GifticonGoodsNotFoundException이 발생한다")
    void shouldThrowWhenGoodsNotExposed() {
        // given
        String goodsCode = "GD002";
        Long userId = 1L;
        GifticonGoods goods = createGoods(goodsCode, false, "SALE");
        when(findGifticonGoodsPort.findByGoodsCode(goodsCode)).thenReturn(Optional.of(goods));

        // when & then
        assertThatThrownBy(() -> getGifticonGoodsDetailService.getGoodsDetail(
                new GetGifticonGoodsDetailCommand(goodsCode, userId)
        )).isInstanceOf(GifticonGoodsNotFoundException.class);
    }

    @Test
    @DisplayName("판매 중이 아닌 상품을 조회하면 GifticonGoodsNotFoundException이 발생한다")
    void shouldThrowWhenGoodsNotOnSale() {
        // given
        String goodsCode = "GD003";
        Long userId = 1L;
        GifticonGoods goods = createGoods(goodsCode, true, "SUS");
        when(findGifticonGoodsPort.findByGoodsCode(goodsCode)).thenReturn(Optional.of(goods));

        // when & then
        assertThatThrownBy(() -> getGifticonGoodsDetailService.getGoodsDetail(
                new GetGifticonGoodsDetailCommand(goodsCode, userId)
        )).isInstanceOf(GifticonGoodsNotFoundException.class);
    }

    private GifticonGoods createGoods(String goodsCode, boolean exposed, String goodsStatus) {
        return GifticonGoods.from(GifticonGoodsSnapshotState.builder()
                .id(1L)
                .goodsCode(goodsCode)
                .goodsName("아메리카노")
                .brandCode("BR001")
                .brandName("스타벅스")
                .brandImageUrl("https://img.com/sb.png")
                .categoryCode("1")
                .realPrice(5000L)
                .salePrice(4500L)
                .cashPrice(4000L)
                .imageUrl("https://img.com/goods.png")
                .description("맛있는 커피")
                .validDays(30)
                .goodsStatus(goodsStatus)
                .exposed(exposed)
                .orderNum(1)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private UserPoint createUserPoint(Long userId, Long amount) {
        return UserPoint.from(UserPointSnapshotState.builder()
                .userId(userId)
                .amount(amount)
                .addExpectedAmount(0L)
                .expireExpectedAmount(0L)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
