package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotExposedException;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoodsSnapshotState;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageFeaturedGifticonGoodsCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageFeaturedGifticonGoodsCommand.FeaturedGoodsItem;
import com.personal.marketnote.reward.port.out.gifticon.EvictGifticonGoodsCachePort;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonGoodsPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageFeaturedGifticonGoodsUseCaseTest {

    @InjectMocks
    private ManageFeaturedGifticonGoodsService manageFeaturedGifticonGoodsService;

    @Mock
    private FindGifticonGoodsPort findGifticonGoodsPort;

    @Mock
    private UpdateGifticonGoodsPort updateGifticonGoodsPort;

    @Mock
    private EvictGifticonGoodsCachePort evictGifticonGoodsCachePort;

    @Test
    @DisplayName("노출된 상품의 인기상품 여부를 설정한다")
    void shouldSetPopularForExposedGoods() {
        // given
        GifticonGoods goods = createExposedGoods("G001");
        when(findGifticonGoodsPort.findByGoodsCode("G001"))
                .thenReturn(Optional.of(goods));

        ManageFeaturedGifticonGoodsCommand command = new ManageFeaturedGifticonGoodsCommand(
                List.of(new FeaturedGoodsItem("G001", true, 1))
        );

        // when
        manageFeaturedGifticonGoodsService.manageFeatured(command);

        // then
        assertThat(goods.isPopular()).isTrue();
        assertThat(goods.getOrderNum()).isEqualTo(1);
        verify(updateGifticonGoodsPort).update(goods);
        verify(evictGifticonGoodsCachePort).evictFeaturedGoodsCache();
    }

    @Test
    @DisplayName("인기상품을 해제하면 popular이 false가 되고 orderNum이 null이 된다")
    void shouldUnmarkPopular() {
        // given
        GifticonGoods goods = createPopularGoods("G002");
        when(findGifticonGoodsPort.findByGoodsCode("G002"))
                .thenReturn(Optional.of(goods));

        ManageFeaturedGifticonGoodsCommand command = new ManageFeaturedGifticonGoodsCommand(
                List.of(new FeaturedGoodsItem("G002", false, null))
        );

        // when
        manageFeaturedGifticonGoodsService.manageFeatured(command);

        // then
        assertThat(goods.isPopular()).isFalse();
        assertThat(goods.getOrderNum()).isNull();
        verify(updateGifticonGoodsPort).update(goods);
        verify(evictGifticonGoodsCachePort).evictFeaturedGoodsCache();
    }

    @Test
    @DisplayName("존재하지 않는 상품 코드로 요청하면 GifticonGoodsNotFoundException이 발생한다")
    void shouldThrowWhenGoodsNotFound() {
        // given
        when(findGifticonGoodsPort.findByGoodsCode("INVALID"))
                .thenReturn(Optional.empty());

        ManageFeaturedGifticonGoodsCommand command = new ManageFeaturedGifticonGoodsCommand(
                List.of(new FeaturedGoodsItem("INVALID", true, 1))
        );

        // when & then
        assertThatThrownBy(() -> manageFeaturedGifticonGoodsService.manageFeatured(command))
                .isInstanceOf(GifticonGoodsNotFoundException.class);
        verifyNoInteractions(updateGifticonGoodsPort);
        verifyNoInteractions(evictGifticonGoodsCachePort);
    }

    @Test
    @DisplayName("노출되지 않은 상품을 인기상품으로 설정하면 GifticonGoodsNotExposedException이 발생한다")
    void shouldThrowWhenGoodsNotExposed() {
        // given
        GifticonGoods goods = createUnexposedGoods("G003");
        when(findGifticonGoodsPort.findByGoodsCode("G003"))
                .thenReturn(Optional.of(goods));

        ManageFeaturedGifticonGoodsCommand command = new ManageFeaturedGifticonGoodsCommand(
                List.of(new FeaturedGoodsItem("G003", true, 1))
        );

        // when & then
        assertThatThrownBy(() -> manageFeaturedGifticonGoodsService.manageFeatured(command))
                .isInstanceOf(GifticonGoodsNotExposedException.class);
        verifyNoInteractions(updateGifticonGoodsPort);
        verifyNoInteractions(evictGifticonGoodsCachePort);
    }

    private GifticonGoods createExposedGoods(String goodsCode) {
        return GifticonGoods.from(GifticonGoodsSnapshotState.builder()
                .id(1L)
                .goodsCode(goodsCode)
                .goodsName("테스트 상품")
                .brandCode("BR001")
                .brandName("테스트 브랜드")
                .brandImageUrl("https://example.com/brand.jpg")
                .categoryCode("1")
                .realPrice(10000L)
                .salePrice(9000L)
                .cashPrice(9000L)
                .imageUrl("https://example.com/img.jpg")
                .goodsStatus("SALE")
                .exposed(true)
                .popular(false)
                .build());
    }

    private GifticonGoods createPopularGoods(String goodsCode) {
        return GifticonGoods.from(GifticonGoodsSnapshotState.builder()
                .id(2L)
                .goodsCode(goodsCode)
                .goodsName("인기 상품")
                .brandCode("BR001")
                .brandName("테스트 브랜드")
                .brandImageUrl("https://example.com/brand.jpg")
                .categoryCode("1")
                .realPrice(10000L)
                .salePrice(9000L)
                .cashPrice(9000L)
                .imageUrl("https://example.com/img.jpg")
                .goodsStatus("SALE")
                .exposed(true)
                .popular(true)
                .orderNum(1)
                .build());
    }

    private GifticonGoods createUnexposedGoods(String goodsCode) {
        return GifticonGoods.from(GifticonGoodsSnapshotState.builder()
                .id(3L)
                .goodsCode(goodsCode)
                .goodsName("미노출 상품")
                .brandCode("BR001")
                .brandName("테스트 브랜드")
                .brandImageUrl("https://example.com/brand.jpg")
                .categoryCode("1")
                .realPrice(10000L)
                .salePrice(9000L)
                .cashPrice(9000L)
                .imageUrl("https://example.com/img.jpg")
                .goodsStatus("SALE")
                .exposed(false)
                .popular(false)
                .build());
    }
}
