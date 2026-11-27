package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.domain.money.Money;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoodsSnapshotState;
import com.personal.marketnote.reward.domain.gifticon.GoodsStatus;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsCashPriceCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsCashPriceCommand.CashPriceItem;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonGoodsPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageGifticonGoodsCashPriceUseCaseTest {

    @InjectMocks
    private ManageGifticonGoodsCashPriceService manageGifticonGoodsCashPriceService;

    @Mock
    private FindGifticonGoodsPort findGifticonGoodsPort;

    @Mock
    private UpdateGifticonGoodsPort updateGifticonGoodsPort;

    @Test
    @DisplayName("배치 수정 시 모든 항목의 cashPrice가 변경되고 update가 호출된다")
    void shouldUpdateAllItemsCashPrice() {
        // given
        GifticonGoods goods1 = createGoods("G00001", 3500L);
        GifticonGoods goods2 = createGoods("G00002", 4000L);
        ManageGifticonGoodsCashPriceCommand command = new ManageGifticonGoodsCashPriceCommand(
            List.of(
                new CashPriceItem("G00001", 2900L),
                new CashPriceItem("G00002", 3300L)
            )
        );

        when(findGifticonGoodsPort.findByGoodsCode("G00001"))
            .thenReturn(Optional.of(goods1));
        when(findGifticonGoodsPort.findByGoodsCode("G00002"))
            .thenReturn(Optional.of(goods2));

        // when
        manageGifticonGoodsCashPriceService.manageCashPrice(command);

        // then
        assertThat(goods1.getCashPrice()).isEqualTo(Money.of(2900L));
        assertThat(goods2.getCashPrice()).isEqualTo(Money.of(3300L));
        verify(updateGifticonGoodsPort).update(goods1);
        verify(updateGifticonGoodsPort).update(goods2);
    }

    @Test
    @DisplayName("존재하지 않는 goodsCode로 요청하면 GifticonGoodsNotFoundException이 발생한다")
    void shouldThrowExceptionWhenGoodsNotFound() {
        // given
        ManageGifticonGoodsCashPriceCommand command = new ManageGifticonGoodsCashPriceCommand(
            List.of(new CashPriceItem("G99999", 1000L))
        );

        when(findGifticonGoodsPort.findByGoodsCode("G99999"))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> manageGifticonGoodsCashPriceService.manageCashPrice(command))
            .isInstanceOf(GifticonGoodsNotFoundException.class);
        verify(updateGifticonGoodsPort, never()).update(any());
    }

    @Test
    @DisplayName("부분 실패 시 첫 항목 update 후 두 번째 NotFound로 예외가 전파되어 트랜잭션 롤백 대상이 된다")
    void shouldPropagateExceptionAfterPartialUpdate() {
        // given
        GifticonGoods goods1 = createGoods("G00001", 3500L);
        ManageGifticonGoodsCashPriceCommand command = new ManageGifticonGoodsCashPriceCommand(
            List.of(
                new CashPriceItem("G00001", 2900L),
                new CashPriceItem("G99999", 1000L)
            )
        );

        when(findGifticonGoodsPort.findByGoodsCode("G00001"))
            .thenReturn(Optional.of(goods1));
        when(findGifticonGoodsPort.findByGoodsCode("G99999"))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> manageGifticonGoodsCashPriceService.manageCashPrice(command))
            .isInstanceOf(GifticonGoodsNotFoundException.class);

        // 첫 항목은 도메인 변경 + update 호출까지 완료 (실제 DB 롤백은 @Transactional이 처리)
        assertThat(goods1.getCashPrice()).isEqualTo(Money.of(2900L));
        verify(updateGifticonGoodsPort).update(goods1);
    }

    private GifticonGoods createGoods(String goodsCode, Long cashPrice) {
        return GifticonGoods.from(GifticonGoodsSnapshotState.builder()
            .id(1L)
            .goodsCode(goodsCode)
            .goodsName("테스트 상품")
            .brandCode("BR001")
            .brandName("스타벅스")
            .brandImageUrl("https://img.com/brand.png")
            .categoryCode("1")
            .realPrice(5000L)
            .salePrice(4500L)
            .cashPrice(cashPrice)
            .imageUrl("https://img.com/goods.png")
            .description("설명")
            .validDays(30)
            .goodsStatus(GoodsStatus.SALE)
            .exposed(false)
            .orderNum(null)
            .createdAt(LocalDateTime.of(2026, 4, 26, 10, 0))
            .modifiedAt(LocalDateTime.of(2026, 4, 26, 10, 0))
            .build()
        );
    }
}
