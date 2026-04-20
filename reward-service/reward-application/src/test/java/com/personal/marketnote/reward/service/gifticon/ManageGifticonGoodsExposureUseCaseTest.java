package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotFoundException;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotSaleException;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoodsSnapshotState;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsExposureCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsExposureCommand.ExposureItem;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageGifticonGoodsExposureUseCaseTest {

    @InjectMocks
    private ManageGifticonGoodsExposureService manageGifticonGoodsExposureService;

    @Mock
    private FindGifticonGoodsPort findGifticonGoodsPort;

    @Mock
    private UpdateGifticonGoodsPort updateGifticonGoodsPort;

    @Test
    @DisplayName("SALE 상태 상품을 노출 설정하면 exposed가 true로 변경된다")
    void shouldExposeGoodsWhenStatusIsSale() {
        // given
        GifticonGoods goods = createGoods("G00001", "SALE", false);
        ManageGifticonGoodsExposureCommand command = new ManageGifticonGoodsExposureCommand(
                List.of(new ExposureItem("G00001", true))
        );

        when(findGifticonGoodsPort.findByGoodsCode("G00001"))
                .thenReturn(Optional.of(goods));

        // when
        manageGifticonGoodsExposureService.manageExposure(command);

        // then
        assertThat(goods.isExposed()).isTrue();
        verify(updateGifticonGoodsPort).update(goods);
    }

    @Test
    @DisplayName("SALE이 아닌 상품을 노출 설정하면 GifticonGoodsNotSaleException이 발생한다")
    void shouldThrowExceptionWhenExposingNonSaleGoods() {
        // given
        GifticonGoods goods = createGoods("G00001", "SUS", false);
        ManageGifticonGoodsExposureCommand command = new ManageGifticonGoodsExposureCommand(
                List.of(new ExposureItem("G00001", true))
        );

        when(findGifticonGoodsPort.findByGoodsCode("G00001"))
                .thenReturn(Optional.of(goods));

        // when & then
        assertThatThrownBy(() -> manageGifticonGoodsExposureService.manageExposure(command))
                .isInstanceOf(GifticonGoodsNotSaleException.class);
        verify(updateGifticonGoodsPort, never()).update(any());
    }

    @Test
    @DisplayName("상품을 노출 해제하면 goodsStatus와 무관하게 exposed가 false로 변경된다")
    void shouldUnexposeGoodsRegardlessOfStatus() {
        // given
        GifticonGoods goods = createGoods("G00001", "SUS", true);
        ManageGifticonGoodsExposureCommand command = new ManageGifticonGoodsExposureCommand(
                List.of(new ExposureItem("G00001", false))
        );

        when(findGifticonGoodsPort.findByGoodsCode("G00001"))
                .thenReturn(Optional.of(goods));

        // when
        manageGifticonGoodsExposureService.manageExposure(command);

        // then
        assertThat(goods.isExposed()).isFalse();
        verify(updateGifticonGoodsPort).update(goods);
    }

    @Test
    @DisplayName("존재하지 않는 상품 코드로 요청하면 GifticonGoodsNotFoundException이 발생한다")
    void shouldThrowExceptionWhenGoodsNotFound() {
        // given
        ManageGifticonGoodsExposureCommand command = new ManageGifticonGoodsExposureCommand(
                List.of(new ExposureItem("G99999", true))
        );

        when(findGifticonGoodsPort.findByGoodsCode("G99999"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> manageGifticonGoodsExposureService.manageExposure(command))
                .isInstanceOf(GifticonGoodsNotFoundException.class);
        verify(updateGifticonGoodsPort, never()).update(any());
    }

    @Test
    @DisplayName("여러 상품의 노출 상태를 한 번에 변경한다")
    void shouldManageMultipleGoodsExposure() {
        // given
        GifticonGoods goods1 = createGoods("G00001", "SALE", false);
        GifticonGoods goods2 = createGoods("G00002", "SALE", true);
        ManageGifticonGoodsExposureCommand command = new ManageGifticonGoodsExposureCommand(
                List.of(
                        new ExposureItem("G00001", true),
                        new ExposureItem("G00002", false)
                )
        );

        when(findGifticonGoodsPort.findByGoodsCode("G00001"))
                .thenReturn(Optional.of(goods1));
        when(findGifticonGoodsPort.findByGoodsCode("G00002"))
                .thenReturn(Optional.of(goods2));

        // when
        manageGifticonGoodsExposureService.manageExposure(command);

        // then
        assertThat(goods1.isExposed()).isTrue();
        assertThat(goods2.isExposed()).isFalse();
        verify(updateGifticonGoodsPort).update(goods1);
        verify(updateGifticonGoodsPort).update(goods2);
    }

    // --- Helper Methods ---

    private GifticonGoods createGoods(String goodsCode, String goodsStatus, boolean exposed) {
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
                .cashPrice(3500L)
                .imageUrl("https://img.com/goods.png")
                .description("설명")
                .validDays(30)
                .goodsStatus(goodsStatus)
                .exposed(exposed)
                .orderNum(null)
                .createdAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .build());
    }
}
