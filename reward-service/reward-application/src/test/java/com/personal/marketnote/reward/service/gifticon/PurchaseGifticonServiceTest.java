package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.exception.GifticonCouponSendFailedException;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoodsSnapshotState;
import com.personal.marketnote.reward.port.in.command.gifticon.PurchaseGifticonCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.PurchaseGifticonResult;
import com.personal.marketnote.reward.port.out.gifticon.CancelGifticonSendFailPort;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import com.personal.marketnote.reward.port.out.gifticon.SendGifticonCouponPort;
import com.personal.marketnote.reward.port.out.gifticon.SendGifticonCouponPort.SendCouponResult;
import com.personal.marketnote.reward.service.gifticon.PurchaseGifticonTransactionHelper.DeductCashAndCreateOrderContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseGifticonServiceTest {

    @InjectMocks
    private PurchaseGifticonService purchaseGifticonService;

    @Mock
    private FindGifticonGoodsPort findGifticonGoodsPort;

    @Mock
    private PurchaseGifticonTransactionHelper transactionHelper;

    @Mock
    private SendGifticonCouponPort sendGifticonCouponPort;

    @Mock
    private CancelGifticonSendFailPort cancelGifticonSendFailPort;

    private static final Long USER_ID = 100L;
    private static final String GOODS_CODE = "G00000280811";

    @Test
    @DisplayName("캐시가 충분하면 기프티콘 구매에 성공한다")
    void shouldPurchaseGifticonSuccessfully() {
        // given
        GifticonGoods goods = createExposedSaleGoods();
        when(findGifticonGoodsPort.findByGoodsCode(GOODS_CODE)).thenReturn(Optional.of(goods));

        DeductCashAndCreateOrderContext context = new DeductCashAndCreateOrderContext(
                1L, "NC100_260404210000", GOODS_CODE, "테스트 기프티콘", 5000L, USER_ID
        );
        when(transactionHelper.deductCashAndCreateOrder(
                eq(USER_ID), eq(GOODS_CODE), any(), any(), any(), eq(5000L)
        )).thenReturn(context);

        SendCouponResult sendResult = SendCouponResult.builder()
                .success(true)
                .orderNo("20260404000001")
                .pinNo("900343630367")
                .couponImageUrl("http://t.giftishow.co.kr/coupon.jpg")
                .validEndDate("20260504")
                .build();
        when(sendGifticonCouponPort.sendCoupon("NC100_260404210000", GOODS_CODE, String.valueOf(USER_ID)))
                .thenReturn(sendResult);

        PurchaseGifticonCommand command = new PurchaseGifticonCommand(USER_ID, GOODS_CODE);

        // when
        PurchaseGifticonResult result = purchaseGifticonService.purchase(command);

        // then
        assertThat(result.orderId()).isEqualTo(1L);
        assertThat(result.orderNo()).isEqualTo("20260404000001");
        assertThat(result.cashAmount()).isEqualTo(5000L);
        assertThat(result.goodsName()).isEqualTo("테스트 기프티콘");
        verify(transactionHelper).commitSuccess(context, sendResult);
        verify(transactionHelper, never()).commitFailure(any());
    }

    @Test
    @DisplayName("존재하지 않는 상품 코드로 구매 시 GifticonGoodsNotFoundException이 발생한다")
    void shouldThrowExceptionWhenGoodsNotFound() {
        // given
        when(findGifticonGoodsPort.findByGoodsCode(GOODS_CODE)).thenReturn(Optional.empty());
        PurchaseGifticonCommand command = new PurchaseGifticonCommand(USER_ID, GOODS_CODE);

        // when & then
        assertThatThrownBy(() -> purchaseGifticonService.purchase(command))
                .isInstanceOf(GifticonGoodsNotFoundException.class);

        verify(transactionHelper, never()).deductCashAndCreateOrder(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("노출되지 않은 상품으로 구매 시 GifticonGoodsNotFoundException이 발생한다")
    void shouldThrowExceptionWhenGoodsNotExposed() {
        // given
        GifticonGoods goods = createUnexposedGoods();
        when(findGifticonGoodsPort.findByGoodsCode(GOODS_CODE)).thenReturn(Optional.of(goods));
        PurchaseGifticonCommand command = new PurchaseGifticonCommand(USER_ID, GOODS_CODE);

        // when & then
        assertThatThrownBy(() -> purchaseGifticonService.purchase(command))
                .isInstanceOf(GifticonGoodsNotFoundException.class);

        verify(transactionHelper, never()).deductCashAndCreateOrder(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("판매 중이 아닌 상품으로 구매 시 GifticonGoodsNotFoundException이 발생한다")
    void shouldThrowExceptionWhenGoodsNotSale() {
        // given
        GifticonGoods goods = createSuspendedGoods();
        when(findGifticonGoodsPort.findByGoodsCode(GOODS_CODE)).thenReturn(Optional.of(goods));
        PurchaseGifticonCommand command = new PurchaseGifticonCommand(USER_ID, GOODS_CODE);

        // when & then
        assertThatThrownBy(() -> purchaseGifticonService.purchase(command))
                .isInstanceOf(GifticonGoodsNotFoundException.class);

        verify(transactionHelper, never()).deductCashAndCreateOrder(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("쿠폰 발송 실패 시 commitFailure가 호출되고 발송실패 취소 API가 호출된다")
    void shouldCallCommitFailureAndCancelSendFailOnCouponSendFailure() {
        // given
        GifticonGoods goods = createExposedSaleGoods();
        when(findGifticonGoodsPort.findByGoodsCode(GOODS_CODE)).thenReturn(Optional.of(goods));

        DeductCashAndCreateOrderContext context = new DeductCashAndCreateOrderContext(
                1L, "NC100_260404210000", GOODS_CODE, "테스트 기프티콘", 5000L, USER_ID
        );
        when(transactionHelper.deductCashAndCreateOrder(
                eq(USER_ID), eq(GOODS_CODE), any(), any(), any(), eq(5000L)
        )).thenReturn(context);

        SendCouponResult failResult = SendCouponResult.builder()
                .success(false)
                .errorCode("ERR0999")
                .errorMessage("쿠폰발송오류")
                .build();
        when(sendGifticonCouponPort.sendCoupon("NC100_260404210000", GOODS_CODE, String.valueOf(USER_ID)))
                .thenReturn(failResult);

        PurchaseGifticonCommand command = new PurchaseGifticonCommand(USER_ID, GOODS_CODE);

        // when & then
        assertThatThrownBy(() -> purchaseGifticonService.purchase(command))
                .isInstanceOf(GifticonCouponSendFailedException.class);

        verify(transactionHelper).commitFailure(context);
        verify(cancelGifticonSendFailPort).cancelSendFailed("NC100_260404210000", String.valueOf(USER_ID));
        verify(transactionHelper, never()).commitSuccess(any(), any());
    }

    @Test
    @DisplayName("발송실패 취소 API 호출 실패 시에도 예외가 전파되지 않는다")
    void shouldNotPropagateExceptionWhenCancelSendFailFails() {
        // given
        GifticonGoods goods = createExposedSaleGoods();
        when(findGifticonGoodsPort.findByGoodsCode(GOODS_CODE)).thenReturn(Optional.of(goods));

        DeductCashAndCreateOrderContext context = new DeductCashAndCreateOrderContext(
                1L, "NC100_260404210000", GOODS_CODE, "테스트 기프티콘", 5000L, USER_ID
        );
        when(transactionHelper.deductCashAndCreateOrder(
                eq(USER_ID), eq(GOODS_CODE), any(), any(), any(), eq(5000L)
        )).thenReturn(context);

        SendCouponResult failResult = SendCouponResult.builder()
                .success(false)
                .errorCode("ERR0999")
                .errorMessage("쿠폰발송오류")
                .build();
        when(sendGifticonCouponPort.sendCoupon("NC100_260404210000", GOODS_CODE, String.valueOf(USER_ID)))
                .thenReturn(failResult);

        doThrow(new RuntimeException("발송실패 취소 API 호출 실패"))
                .when(cancelGifticonSendFailPort).cancelSendFailed(any(), any());

        PurchaseGifticonCommand command = new PurchaseGifticonCommand(USER_ID, GOODS_CODE);

        // when & then
        assertThatThrownBy(() -> purchaseGifticonService.purchase(command))
                .isInstanceOf(GifticonCouponSendFailedException.class);

        verify(transactionHelper).commitFailure(context);
    }

    private GifticonGoods createExposedSaleGoods() {
        GifticonGoods goods = GifticonGoods.from(GifticonGoodsSnapshotState.builder()
                .id(1L)
                .goodsCode(GOODS_CODE)
                .goodsName("테스트 기프티콘")
                .brandCode("BR00046")
                .brandName("세븐일레븐")
                .brandImageUrl("https://example.com/brand.jpg")
                .categoryCode("CAT001")
                .realPrice(6000L)
                .salePrice(5500L)
                .cashPrice(5000L)
                .imageUrl("https://example.com/goods.jpg")
                .description("테스트 상품입니다")
                .validDays(30)
                .goodsStatus("SALE")
                .exposed(true)
                .popular(false)
                .build());
        return goods;
    }

    private GifticonGoods createUnexposedGoods() {
        return GifticonGoods.from(GifticonGoodsSnapshotState.builder()
                .id(1L)
                .goodsCode(GOODS_CODE)
                .goodsName("테스트 기프티콘")
                .brandCode("BR00046")
                .brandName("세븐일레븐")
                .categoryCode("CAT001")
                .realPrice(6000L)
                .salePrice(5500L)
                .cashPrice(5000L)
                .goodsStatus("SALE")
                .exposed(false)
                .popular(false)
                .build());
    }

    private GifticonGoods createSuspendedGoods() {
        return GifticonGoods.from(GifticonGoodsSnapshotState.builder()
                .id(1L)
                .goodsCode(GOODS_CODE)
                .goodsName("테스트 기프티콘")
                .brandCode("BR00046")
                .brandName("세븐일레븐")
                .categoryCode("CAT001")
                .realPrice(6000L)
                .salePrice(5500L)
                .cashPrice(5000L)
                .goodsStatus("SUS")
                .exposed(true)
                .popular(false)
                .build());
    }
}
