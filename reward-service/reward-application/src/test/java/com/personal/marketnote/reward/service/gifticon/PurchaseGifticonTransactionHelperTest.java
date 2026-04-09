package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.exception.GifticonInsufficientCashException;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrder;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderSnapshotState;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderStatus;
import com.personal.marketnote.reward.domain.point.PointAmount;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyUserPointUseCase;
import com.personal.marketnote.reward.port.out.gifticon.*;
import com.personal.marketnote.reward.port.out.gifticon.SendGifticonCouponPort.SendCouponResult;
import com.personal.marketnote.reward.service.gifticon.PurchaseGifticonTransactionHelper.DeductCashAndCreateOrderContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseGifticonTransactionHelperTest {

    @InjectMocks
    private PurchaseGifticonTransactionHelper helper;

    @Mock
    private GetUserPointUseCase getUserPointUseCase;

    @Mock
    private ModifyUserPointUseCase modifyUserPointUseCase;

    @Mock
    private SaveGifticonOrderPort saveGifticonOrderPort;

    @Mock
    private FindGifticonOrderPort findGifticonOrderPort;

    @Mock
    private UpdateGifticonOrderPort updateGifticonOrderPort;

    @Mock
    private EncryptGifticonPinPort encryptGifticonPinPort;

    @Spy
    private Clock clock = Clock.fixed(
            Instant.parse("2026-04-06T12:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    private static final Long USER_ID = 100L;
    private static final String GOODS_CODE = "G00000280811";
    private static final String GOODS_NAME = "테스트 기프티콘";
    private static final String BRAND_NAME = "세븐일레븐";
    private static final String PRODUCT_IMAGE_URL = "https://example.com/image.jpg";
    private static final Long CASH_PRICE = 5000L;

    @Test
    @DisplayName("캐시 차감 후 GifticonOrder가 PENDING 상태로 저장된다")
    void shouldSaveOrderAsPendingAfterCashDeduction() {
        // given
        UserPoint userPoint = createUserPointWithAmount(10000L);
        when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(userPoint);
        when(findGifticonOrderPort.existsByTrId(any())).thenReturn(false);
        when(saveGifticonOrderPort.save(any())).thenAnswer(invocation -> {
            GifticonOrder order = invocation.getArgument(0);
            return GifticonOrder.from(GifticonOrderSnapshotState.builder()
                    .id(1L)
                    .userId(order.getUserId())
                    .goodsCode(order.getGoodsCode())
                    .goodsName(order.getGoodsName())
                    .brandName(order.getBrandName())
                    .productImageUrl(order.getProductImageUrl())
                    .trId(order.getTrId())
                    .cashPrice(order.getCashPrice())
                    .orderStatus(order.getOrderStatus())
                    .build());
        });

        // when
        DeductCashAndCreateOrderContext context = helper.deductCashAndCreateOrder(
                USER_ID, GOODS_CODE, GOODS_NAME, BRAND_NAME, PRODUCT_IMAGE_URL, CASH_PRICE
        );

        // then
        ArgumentCaptor<GifticonOrder> orderCaptor = ArgumentCaptor.forClass(GifticonOrder.class);
        verify(saveGifticonOrderPort).save(orderCaptor.capture());
        GifticonOrder savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getOrderStatus()).isEqualTo(GifticonOrderStatus.PENDING);
        assertThat(savedOrder.getGoodsCode()).isEqualTo(GOODS_CODE);
        assertThat(savedOrder.getCashPrice()).isEqualTo(CASH_PRICE);
    }

    @Test
    @DisplayName("캐시 부족 시 GifticonInsufficientCashException이 발생한다")
    void shouldThrowExceptionWhenInsufficientCash() {
        // given
        UserPoint userPoint = createUserPointWithAmount(3000L);
        when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(userPoint);

        // when & then
        assertThatThrownBy(() -> helper.deductCashAndCreateOrder(
                USER_ID, GOODS_CODE, GOODS_NAME, BRAND_NAME, PRODUCT_IMAGE_URL, CASH_PRICE
        ))
                .isInstanceOf(GifticonInsufficientCashException.class)
                .satisfies(ex -> {
                    GifticonInsufficientCashException e = (GifticonInsufficientCashException) ex;
                    assertThat(e.getShortfallAmount()).isEqualTo(2000L);
                });

        verify(saveGifticonOrderPort, never()).save(any());
    }

    @Test
    @DisplayName("TR_ID가 NC{userId}_{yyMMddHHmmss} 형식으로 생성된다")
    void shouldGenerateTrIdInCorrectFormat() {
        // given
        UserPoint userPoint = createUserPointWithAmount(10000L);
        when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(userPoint);
        when(findGifticonOrderPort.existsByTrId(any())).thenReturn(false);
        when(saveGifticonOrderPort.save(any())).thenAnswer(invocation -> {
            GifticonOrder order = invocation.getArgument(0);
            return GifticonOrder.from(GifticonOrderSnapshotState.builder()
                    .id(1L)
                    .userId(order.getUserId())
                    .goodsCode(order.getGoodsCode())
                    .goodsName(order.getGoodsName())
                    .brandName(order.getBrandName())
                    .productImageUrl(order.getProductImageUrl())
                    .trId(order.getTrId())
                    .cashPrice(order.getCashPrice())
                    .orderStatus(order.getOrderStatus())
                    .build());
        });

        // when
        DeductCashAndCreateOrderContext context = helper.deductCashAndCreateOrder(
                USER_ID, GOODS_CODE, GOODS_NAME, BRAND_NAME, PRODUCT_IMAGE_URL, CASH_PRICE
        );

        // then
        // NC{userId}_{yyMMddHHmmss} = "NC" + userId + "_" + 12자
        assertThat(context.trId()).startsWith("NC" + USER_ID + "_");
        assertThat(context.trId()).matches("NC\\d+_\\d{12}");
        assertThat(context.trId().length()).isLessThanOrEqualTo(25);
    }

    @Test
    @DisplayName("포인트 이력에 GIFTICON_PURCHASE sourceType으로 기록된다")
    void shouldRecordGifticonPurchaseSourceType() {
        // given
        UserPoint userPoint = createUserPointWithAmount(10000L);
        when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(userPoint);
        when(findGifticonOrderPort.existsByTrId(any())).thenReturn(false);
        when(saveGifticonOrderPort.save(any())).thenAnswer(invocation -> {
            GifticonOrder order = invocation.getArgument(0);
            return GifticonOrder.from(GifticonOrderSnapshotState.builder()
                    .id(1L)
                    .userId(order.getUserId())
                    .goodsCode(order.getGoodsCode())
                    .goodsName(order.getGoodsName())
                    .brandName(order.getBrandName())
                    .productImageUrl(order.getProductImageUrl())
                    .trId(order.getTrId())
                    .cashPrice(order.getCashPrice())
                    .orderStatus(order.getOrderStatus())
                    .build());
        });

        // when
        helper.deductCashAndCreateOrder(
                USER_ID, GOODS_CODE, GOODS_NAME, BRAND_NAME, PRODUCT_IMAGE_URL, CASH_PRICE
        );

        // then
        ArgumentCaptor<ModifyUserPointCommand> commandCaptor = ArgumentCaptor.forClass(ModifyUserPointCommand.class);
        verify(modifyUserPointUseCase).modify(commandCaptor.capture());
        ModifyUserPointCommand captured = commandCaptor.getValue();
        assertThat(captured.sourceType()).isEqualTo(UserPointSourceType.GIFTICON_PURCHASE);
        assertThat(captured.changeType()).isEqualTo(UserPointChangeType.DEDUCTION);
        assertThat(captured.amount()).isEqualTo(CASH_PRICE);
        assertThat(captured.userId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("쿠폰 발송 성공 시 ISSUED 상태로 전이되고 orderNo/pinNo/couponImageUrl이 저장된다")
    void shouldTransitionToIssuedOnSuccess() {
        // given
        GifticonOrder pendingOrder = createPendingOrder();
        when(findGifticonOrderPort.findByTrId("NC100_260404210000")).thenReturn(Optional.of(pendingOrder));
        when(encryptGifticonPinPort.encrypt("900343630367")).thenReturn("encryptedPin123");

        DeductCashAndCreateOrderContext context = new DeductCashAndCreateOrderContext(
                1L, "NC100_260404210000", GOODS_CODE, GOODS_NAME, CASH_PRICE, USER_ID
        );
        SendCouponResult sendResult = SendCouponResult.builder()
                .success(true)
                .orderNo("20260404000001")
                .pinNo("900343630367")
                .couponImageUrl("http://t.giftishow.co.kr/coupon_image.jpg")
                .validEndDate("20260504")
                .build();

        // when
        helper.commitSuccess(context, sendResult);

        // then
        ArgumentCaptor<GifticonOrder> orderCaptor = ArgumentCaptor.forClass(GifticonOrder.class);
        verify(updateGifticonOrderPort).update(orderCaptor.capture());
        GifticonOrder updatedOrder = orderCaptor.getValue();
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(GifticonOrderStatus.ISSUED);
        assertThat(updatedOrder.getOrderNo()).isEqualTo("20260404000001");
        assertThat(updatedOrder.getPinNo()).isEqualTo("encryptedPin123");
        assertThat(updatedOrder.getCouponImageUrl()).isEqualTo("http://t.giftishow.co.kr/coupon_image.jpg");
        assertThat(updatedOrder.getValidEndDate()).isEqualTo(LocalDate.of(2026, 5, 4));
    }

    @Test
    @DisplayName("commitSuccess 시 pinNo가 암호화되어 저장된다")
    void shouldEncryptPinNoOnCommitSuccess() {
        // given
        GifticonOrder pendingOrder = createPendingOrder();
        when(findGifticonOrderPort.findByTrId("NC100_260404210000")).thenReturn(Optional.of(pendingOrder));
        when(encryptGifticonPinPort.encrypt("plainPin")).thenReturn("encryptedPin");

        DeductCashAndCreateOrderContext context = new DeductCashAndCreateOrderContext(
                1L, "NC100_260404210000", GOODS_CODE, GOODS_NAME, CASH_PRICE, USER_ID
        );
        SendCouponResult sendResult = SendCouponResult.builder()
                .success(true)
                .orderNo("20260404000001")
                .pinNo("plainPin")
                .couponImageUrl("http://example.com/coupon.jpg")
                .validEndDate("20260504")
                .build();

        // when
        helper.commitSuccess(context, sendResult);

        // then
        verify(encryptGifticonPinPort).encrypt("plainPin");
        ArgumentCaptor<GifticonOrder> orderCaptor = ArgumentCaptor.forClass(GifticonOrder.class);
        verify(updateGifticonOrderPort).update(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getPinNo()).isEqualTo("encryptedPin");
    }

    @Test
    @DisplayName("commitFailure 시 캐시가 환불된다")
    void shouldRefundCashOnCommitFailure() {
        // given
        GifticonOrder pendingOrder = createPendingOrder();
        when(findGifticonOrderPort.findByTrId("NC100_260404210000")).thenReturn(Optional.of(pendingOrder));

        DeductCashAndCreateOrderContext context = new DeductCashAndCreateOrderContext(
                1L, "NC100_260404210000", GOODS_CODE, GOODS_NAME, CASH_PRICE, USER_ID
        );

        // when
        helper.commitFailure(context);

        // then
        ArgumentCaptor<ModifyUserPointCommand> commandCaptor = ArgumentCaptor.forClass(ModifyUserPointCommand.class);
        verify(modifyUserPointUseCase).modify(commandCaptor.capture());
        ModifyUserPointCommand captured = commandCaptor.getValue();
        assertThat(captured.sourceType()).isEqualTo(UserPointSourceType.GIFTICON_REFUND);
        assertThat(captured.changeType()).isEqualTo(UserPointChangeType.ACCRUAL);
        assertThat(captured.amount()).isEqualTo(CASH_PRICE);
        assertThat(captured.userId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("commitFailure 시 SEND_FAILED 상태로 전이된다")
    void shouldTransitionToSendFailedOnCommitFailure() {
        // given
        GifticonOrder pendingOrder = createPendingOrder();
        when(findGifticonOrderPort.findByTrId("NC100_260404210000")).thenReturn(Optional.of(pendingOrder));

        DeductCashAndCreateOrderContext context = new DeductCashAndCreateOrderContext(
                1L, "NC100_260404210000", GOODS_CODE, GOODS_NAME, CASH_PRICE, USER_ID
        );

        // when
        helper.commitFailure(context);

        // then
        ArgumentCaptor<GifticonOrder> orderCaptor = ArgumentCaptor.forClass(GifticonOrder.class);
        verify(updateGifticonOrderPort).update(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getOrderStatus()).isEqualTo(GifticonOrderStatus.SEND_FAILED);
    }

    private UserPoint createUserPointWithAmount(Long amount) {
        return UserPoint.from(
                com.personal.marketnote.reward.domain.point.UserPointSnapshotState.builder()
                        .userId(USER_ID)
                        .amount(amount)
                        .addExpectedAmount(0L)
                        .expireExpectedAmount(0L)
                        .build()
        );
    }

    private GifticonOrder createPendingOrder() {
        return GifticonOrder.from(GifticonOrderSnapshotState.builder()
                .id(1L)
                .userId(USER_ID)
                .goodsCode(GOODS_CODE)
                .goodsName(GOODS_NAME)
                .brandName(BRAND_NAME)
                .productImageUrl(PRODUCT_IMAGE_URL)
                .trId("NC100_260404210000")
                .cashPrice(CASH_PRICE)
                .orderStatus(GifticonOrderStatus.PENDING)
                .build());
    }
}
