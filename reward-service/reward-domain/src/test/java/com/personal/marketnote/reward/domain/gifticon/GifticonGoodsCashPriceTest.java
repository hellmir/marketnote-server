package com.personal.marketnote.reward.domain.gifticon;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidMoneyAmountException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.MoneyAmountNoValueException;
import com.personal.marketnote.common.domain.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GifticonGoodsCashPriceTest {

    @Test
    @DisplayName("changeCashPrice 호출 시 cashPrice가 새 값으로 변경된다")
    void changeCashPriceUpdatesValue() {
        // given
        GifticonGoods goods = createGoods(3500L);

        // when
        goods.changeCashPrice(Money.of(2900L));

        // then
        assertThat(goods.getCashPrice()).isEqualTo(Money.of(2900L));
    }

    @Test
    @DisplayName("changeCashPrice에 null Long을 전달하면 MoneyAmountNoValueException이 발생한다")
    void changeCashPriceWithNullThrows() {
        // given
        GifticonGoods goods = createGoods(3500L);
        Long nullAmount = null;

        // when & then
        assertThatThrownBy(() -> goods.changeCashPrice(Money.of(nullAmount)))
                .isInstanceOf(MoneyAmountNoValueException.class);
    }

    @Test
    @DisplayName("changeCashPrice에 음수를 전달하면 InvalidMoneyAmountException이 발생한다")
    void changeCashPriceWithNegativeThrows() {
        // given
        GifticonGoods goods = createGoods(3500L);

        // when & then
        assertThatThrownBy(() -> goods.changeCashPrice(Money.of(-1L)))
                .isInstanceOf(InvalidMoneyAmountException.class);
    }

    @Test
    @DisplayName("syncFromApi 호출 후에도 관리자가 설정한 cashPrice는 보존된다")
    void syncFromApiPreservesCashPrice() {
        // given
        GifticonGoods goods = createGoods(3500L);
        goods.changeCashPrice(Money.of(2900L));

        GifticonGoodsSyncState syncState = GifticonGoodsSyncState.builder()
                .goodsName("스타벅스 아메리카노 (변경)")
                .brandCode("B001")
                .brandName("스타벅스")
                .brandImageUrl("https://example.com/brand.jpg")
                .categoryCode("3")
                .realPrice(6000L)
                .salePrice(5500L)
                .imageUrl("https://example.com/image.jpg")
                .description("상품 설명 변경")
                .validDays(90)
                .goodsStatus(GoodsStatus.SALE)
                .build();

        // when
        goods.syncFromApi(syncState);

        // then
        assertThat(goods.getCashPrice()).isEqualTo(Money.of(2900L));
        assertThat(goods.getSalePrice()).isEqualTo(Money.of(5500L));
        assertThat(goods.getGoodsName()).isEqualTo("스타벅스 아메리카노 (변경)");
    }

    private GifticonGoods createGoods(Long cashPrice) {
        return GifticonGoods.from(GifticonGoodsSnapshotState.builder()
                .id(1L)
                .goodsCode("G001")
                .goodsName("스타벅스 아메리카노")
                .brandCode("B001")
                .brandName("스타벅스")
                .brandImageUrl("https://example.com/brand.jpg")
                .categoryCode("3")
                .realPrice(5000L)
                .salePrice(4500L)
                .cashPrice(cashPrice)
                .imageUrl("https://example.com/image.jpg")
                .description("상품 설명")
                .validDays(90)
                .goodsStatus(GoodsStatus.SALE)
                .exposed(false)
                .orderNum(null)
                .createdAt(LocalDateTime.of(2026, 4, 26, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 26, 10, 0))
                .build()
        );
    }
}
