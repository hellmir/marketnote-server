package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotExposedException;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoodsSnapshotState;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsOrderCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsOrderCommand.OrderItem;
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
class ManageGifticonGoodsOrderUseCaseTest {

    @InjectMocks
    private ManageGifticonGoodsOrderService manageGifticonGoodsOrderService;

    @Mock
    private FindGifticonGoodsPort findGifticonGoodsPort;

    @Mock
    private UpdateGifticonGoodsPort updateGifticonGoodsPort;

    @Test
    @DisplayName("노출된 상품의 정렬 순서를 변경하면 orderNum이 변경된다")
    void shouldChangeOrderNumWhenGoodsIsExposed() {
        // given
        GifticonGoods goods = createGoods("G00001", true, null);
        ManageGifticonGoodsOrderCommand command = new ManageGifticonGoodsOrderCommand(
                List.of(new OrderItem("G00001", 1))
        );

        when(findGifticonGoodsPort.findByGoodsCode("G00001"))
                .thenReturn(Optional.of(goods));

        // when
        manageGifticonGoodsOrderService.manageOrder(command);

        // then
        assertThat(goods.getOrderNum()).isEqualTo(1);
        verify(updateGifticonGoodsPort).update(goods);
    }

    @Test
    @DisplayName("미노출 상품에 정렬 순서를 설정하면 GifticonGoodsNotExposedException이 발생한다")
    void shouldThrowExceptionWhenGoodsNotExposed() {
        // given
        GifticonGoods goods = createGoods("G00001", false, null);
        ManageGifticonGoodsOrderCommand command = new ManageGifticonGoodsOrderCommand(
                List.of(new OrderItem("G00001", 1))
        );

        when(findGifticonGoodsPort.findByGoodsCode("G00001"))
                .thenReturn(Optional.of(goods));

        // when & then
        assertThatThrownBy(() -> manageGifticonGoodsOrderService.manageOrder(command))
                .isInstanceOf(GifticonGoodsNotExposedException.class);
        verify(updateGifticonGoodsPort, never()).update(any());
    }

    @Test
    @DisplayName("존재하지 않는 상품 코드로 요청하면 GifticonGoodsNotFoundException이 발생한다")
    void shouldThrowExceptionWhenGoodsNotFound() {
        // given
        ManageGifticonGoodsOrderCommand command = new ManageGifticonGoodsOrderCommand(
                List.of(new OrderItem("G99999", 1))
        );

        when(findGifticonGoodsPort.findByGoodsCode("G99999"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> manageGifticonGoodsOrderService.manageOrder(command))
                .isInstanceOf(GifticonGoodsNotFoundException.class);
        verify(updateGifticonGoodsPort, never()).update(any());
    }

    @Test
    @DisplayName("여러 상품의 정렬 순서를 한 번에 변경한다")
    void shouldManageMultipleGoodsOrder() {
        // given
        GifticonGoods goods1 = createGoods("G00001", true, null);
        GifticonGoods goods2 = createGoods("G00002", true, 5);
        ManageGifticonGoodsOrderCommand command = new ManageGifticonGoodsOrderCommand(
                List.of(
                        new OrderItem("G00001", 1),
                        new OrderItem("G00002", 2)
                )
        );

        when(findGifticonGoodsPort.findByGoodsCode("G00001"))
                .thenReturn(Optional.of(goods1));
        when(findGifticonGoodsPort.findByGoodsCode("G00002"))
                .thenReturn(Optional.of(goods2));

        // when
        manageGifticonGoodsOrderService.manageOrder(command);

        // then
        assertThat(goods1.getOrderNum()).isEqualTo(1);
        assertThat(goods2.getOrderNum()).isEqualTo(2);
        verify(updateGifticonGoodsPort).update(goods1);
        verify(updateGifticonGoodsPort).update(goods2);
    }

    @Test
    @DisplayName("orderNum을 null로 설정하면 정렬 순서가 해제된다")
    void shouldClearOrderNumWhenSetToNull() {
        // given
        GifticonGoods goods = createGoods("G00001", true, 3);
        ManageGifticonGoodsOrderCommand command = new ManageGifticonGoodsOrderCommand(
                List.of(new OrderItem("G00001", null))
        );

        when(findGifticonGoodsPort.findByGoodsCode("G00001"))
                .thenReturn(Optional.of(goods));

        // when
        manageGifticonGoodsOrderService.manageOrder(command);

        // then
        assertThat(goods.getOrderNum()).isNull();
        verify(updateGifticonGoodsPort).update(goods);
    }

    // --- Helper Methods ---

    private GifticonGoods createGoods(String goodsCode, boolean exposed, Integer orderNum) {
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
                .goodsStatus("SALE")
                .exposed(exposed)
                .orderNum(orderNum)
                .createdAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .build());
    }
}
