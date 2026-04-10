package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.exception.GifticonOrderNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.*;
import com.personal.marketnote.reward.port.in.command.gifticon.GetMyGifticonOrderDetailCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetMyGifticonOrderDetailResult;
import com.personal.marketnote.reward.port.out.gifticon.*;
import com.personal.marketnote.reward.port.out.gifticon.QueryGifticonCouponStatusPort.CouponStatusResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMyGifticonOrderDetailServiceTest {

    @InjectMocks
    private GetMyGifticonOrderDetailService getMyGifticonOrderDetailService;

    @Mock
    private FindGifticonOrderPort findGifticonOrderPort;

    @Mock
    private FindGifticonGoodsPort findGifticonGoodsPort;

    @Mock
    private QueryGifticonCouponStatusPort queryGifticonCouponStatusPort;

    @Mock
    private UpdateGifticonOrderPort updateGifticonOrderPort;

    @Mock
    private DecryptGifticonPinPort decryptGifticonPinPort;

    @Spy
    private Clock clock = Clock.fixed(
            Instant.parse("2026-04-06T03:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    private static final Long USER_ID = 100L;
    private static final Long ORDER_ID = 1L;
    private static final String TR_ID = "NC100_260401100000";
    private static final String GOODS_CODE = "G00000280811";

    @Test
    @DisplayName("ISSUED 상태 주문의 상세 정보를 정상적으로 반환한다")
    void shouldReturnOrderDetailSuccessfully() {
        // given
        GifticonOrder order = createOrder(GifticonOrderStatus.ISSUED);
        GifticonGoods goods = createGoods();

        when(findGifticonOrderPort.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));
        when(queryGifticonCouponStatusPort.queryStatus(TR_ID)).thenReturn(
                CouponStatusResult.builder().success(true).pinStatusCd("01").validPrdEndDt("20260504").build()
        );
        when(findGifticonGoodsPort.findByGoodsCode(GOODS_CODE)).thenReturn(Optional.of(goods));
        when(decryptGifticonPinPort.decrypt("encryptedPin")).thenReturn("900343630367");

        GetMyGifticonOrderDetailCommand command = new GetMyGifticonOrderDetailCommand(USER_ID, ORDER_ID);

        // when
        GetMyGifticonOrderDetailResult result = getMyGifticonOrderDetailService.getMyGifticonOrderDetail(command);

        // then
        assertThat(result.orderId()).isEqualTo(ORDER_ID);
        assertThat(result.goodsName()).isEqualTo("테스트 기프티콘");
        assertThat(result.brandName()).isEqualTo("세븐일레븐");
        assertThat(result.brandImageUrl()).isEqualTo("https://example.com/brand.jpg");
        assertThat(result.productImageUrl()).isEqualTo("https://example.com/goods.jpg");
        assertThat(result.description()).isEqualTo("테스트 상품입니다");
        assertThat(result.cashPrice()).isEqualTo(5000L);
        assertThat(result.couponImageUrl()).isEqualTo("https://example.com/coupon.jpg");
        assertThat(result.pinNo()).isEqualTo("900343630367");
        assertThat(result.orderStatus()).isEqualTo("ISSUED");
    }

    @Test
    @DisplayName("주문이 존재하지 않으면 GifticonOrderNotFoundException이 발생한다")
    void shouldThrowExceptionWhenOrderNotFound() {
        // given
        when(findGifticonOrderPort.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.empty());
        GetMyGifticonOrderDetailCommand command = new GetMyGifticonOrderDetailCommand(USER_ID, ORDER_ID);

        // when & then
        assertThatThrownBy(() -> getMyGifticonOrderDetailService.getMyGifticonOrderDetail(command))
                .isInstanceOf(GifticonOrderNotFoundException.class);

        verifyNoInteractions(queryGifticonCouponStatusPort);
        verifyNoInteractions(decryptGifticonPinPort);
    }

    @Test
    @DisplayName("ISSUED 상태에서 기프티쇼 API 조회 결과 USED로 변경되면 상태가 동기화된다")
    void shouldSyncStatusWhenPinStatusChangedToUsed() {
        // given
        GifticonOrder order = createOrder(GifticonOrderStatus.ISSUED);
        GifticonGoods goods = createGoods();

        when(findGifticonOrderPort.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));
        when(queryGifticonCouponStatusPort.queryStatus(TR_ID)).thenReturn(
                CouponStatusResult.builder().success(true).pinStatusCd("02").validPrdEndDt("20260504").build()
        );
        when(findGifticonGoodsPort.findByGoodsCode(GOODS_CODE)).thenReturn(Optional.of(goods));
        when(decryptGifticonPinPort.decrypt("encryptedPin")).thenReturn("900343630367");

        GetMyGifticonOrderDetailCommand command = new GetMyGifticonOrderDetailCommand(USER_ID, ORDER_ID);

        // when
        GetMyGifticonOrderDetailResult result = getMyGifticonOrderDetailService.getMyGifticonOrderDetail(command);

        // then
        assertThat(result.orderStatus()).isEqualTo("USED");
        assertThat(result.statusLabel()).isEqualTo("사용완료");
        verify(updateGifticonOrderPort).update(order);
    }

    @Test
    @DisplayName("ISSUED 상태에서 기프티쇼 API 조회 결과 EXPIRED로 변경되면 상태가 동기화된다")
    void shouldSyncStatusWhenPinStatusChangedToExpired() {
        // given
        GifticonOrder order = createOrder(GifticonOrderStatus.ISSUED);
        GifticonGoods goods = createGoods();

        when(findGifticonOrderPort.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));
        when(queryGifticonCouponStatusPort.queryStatus(TR_ID)).thenReturn(
                CouponStatusResult.builder().success(true).pinStatusCd("08").validPrdEndDt("20260504").build()
        );
        when(findGifticonGoodsPort.findByGoodsCode(GOODS_CODE)).thenReturn(Optional.of(goods));
        when(decryptGifticonPinPort.decrypt("encryptedPin")).thenReturn("900343630367");

        GetMyGifticonOrderDetailCommand command = new GetMyGifticonOrderDetailCommand(USER_ID, ORDER_ID);

        // when
        GetMyGifticonOrderDetailResult result = getMyGifticonOrderDetailService.getMyGifticonOrderDetail(command);

        // then
        assertThat(result.orderStatus()).isEqualTo("EXPIRED");
        assertThat(result.statusLabel()).isEqualTo("기간만료");
        verify(updateGifticonOrderPort).update(order);
    }

    @Test
    @DisplayName("ISSUED 상태에서 기프티쇼 API 조회 결과 동일 상태이면 업데이트하지 않는다")
    void shouldNotUpdateWhenStatusUnchanged() {
        // given
        GifticonOrder order = createOrder(GifticonOrderStatus.ISSUED);
        GifticonGoods goods = createGoods();

        when(findGifticonOrderPort.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));
        when(queryGifticonCouponStatusPort.queryStatus(TR_ID)).thenReturn(
                CouponStatusResult.builder().success(true).pinStatusCd("01").validPrdEndDt("20260504").build()
        );
        when(findGifticonGoodsPort.findByGoodsCode(GOODS_CODE)).thenReturn(Optional.of(goods));
        when(decryptGifticonPinPort.decrypt("encryptedPin")).thenReturn("900343630367");

        GetMyGifticonOrderDetailCommand command = new GetMyGifticonOrderDetailCommand(USER_ID, ORDER_ID);

        // when
        getMyGifticonOrderDetailService.getMyGifticonOrderDetail(command);

        // then
        verify(updateGifticonOrderPort, never()).update(any());
    }

    @Test
    @DisplayName("USED 등 terminal 상태에서는 기프티쇼 API를 호출하지 않는다")
    void shouldNotCallApiForTerminalStatus() {
        // given
        GifticonOrder order = createOrder(GifticonOrderStatus.USED);
        GifticonGoods goods = createGoods();

        when(findGifticonOrderPort.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));
        when(findGifticonGoodsPort.findByGoodsCode(GOODS_CODE)).thenReturn(Optional.of(goods));
        when(decryptGifticonPinPort.decrypt("encryptedPin")).thenReturn("900343630367");

        GetMyGifticonOrderDetailCommand command = new GetMyGifticonOrderDetailCommand(USER_ID, ORDER_ID);

        // when
        GetMyGifticonOrderDetailResult result = getMyGifticonOrderDetailService.getMyGifticonOrderDetail(command);

        // then
        assertThat(result.orderStatus()).isEqualTo("USED");
        verifyNoInteractions(queryGifticonCouponStatusPort);
        verify(updateGifticonOrderPort, never()).update(any());
    }

    @Test
    @DisplayName("기프티쇼 API 호출 실패 시 기존 상태를 유지한다 (fail-open)")
    void shouldKeepCurrentStatusWhenApiCallFails() {
        // given
        GifticonOrder order = createOrder(GifticonOrderStatus.ISSUED);
        GifticonGoods goods = createGoods();

        when(findGifticonOrderPort.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));
        when(queryGifticonCouponStatusPort.queryStatus(TR_ID)).thenReturn(
                CouponStatusResult.builder().success(false).errorCode("COMM_ERROR").errorMessage("통신 오류").build()
        );
        when(findGifticonGoodsPort.findByGoodsCode(GOODS_CODE)).thenReturn(Optional.of(goods));
        when(decryptGifticonPinPort.decrypt("encryptedPin")).thenReturn("900343630367");

        GetMyGifticonOrderDetailCommand command = new GetMyGifticonOrderDetailCommand(USER_ID, ORDER_ID);

        // when
        GetMyGifticonOrderDetailResult result = getMyGifticonOrderDetailService.getMyGifticonOrderDetail(command);

        // then
        assertThat(result.orderStatus()).isEqualTo("ISSUED");
        verify(updateGifticonOrderPort, never()).update(any());
    }

    @Test
    @DisplayName("상품이 DB에 없으면 description과 brandImageUrl이 null로 반환된다")
    void shouldReturnNullFieldsWhenGoodsNotFound() {
        // given
        GifticonOrder order = createOrder(GifticonOrderStatus.USED);

        when(findGifticonOrderPort.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));
        when(findGifticonGoodsPort.findByGoodsCode(GOODS_CODE)).thenReturn(Optional.empty());
        when(decryptGifticonPinPort.decrypt("encryptedPin")).thenReturn("900343630367");

        GetMyGifticonOrderDetailCommand command = new GetMyGifticonOrderDetailCommand(USER_ID, ORDER_ID);

        // when
        GetMyGifticonOrderDetailResult result = getMyGifticonOrderDetailService.getMyGifticonOrderDetail(command);

        // then
        assertThat(result.description()).isNull();
        assertThat(result.brandImageUrl()).isNull();
    }

    @Test
    @DisplayName("expiryDate가 'YY.MM.DD까지 사용 가능' 포맷으로 반환된다")
    void shouldFormatExpiryDateCorrectly() {
        // given
        GifticonOrder order = createOrder(GifticonOrderStatus.USED);
        GifticonGoods goods = createGoods();

        when(findGifticonOrderPort.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));
        when(findGifticonGoodsPort.findByGoodsCode(GOODS_CODE)).thenReturn(Optional.of(goods));
        when(decryptGifticonPinPort.decrypt("encryptedPin")).thenReturn("900343630367");

        GetMyGifticonOrderDetailCommand command = new GetMyGifticonOrderDetailCommand(USER_ID, ORDER_ID);

        // when
        GetMyGifticonOrderDetailResult result = getMyGifticonOrderDetailService.getMyGifticonOrderDetail(command);

        // then
        assertThat(result.expiryDate()).isEqualTo("26.05.04까지 사용 가능");
    }

    @Test
    @DisplayName("daysRemaining이 유효기간까지 남은 일수로 계산된다")
    void shouldCalculateDaysRemainingCorrectly() {
        // given — clock은 2026-04-06 KST
        GifticonOrder order = createOrder(GifticonOrderStatus.USED);
        GifticonGoods goods = createGoods();

        when(findGifticonOrderPort.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));
        when(findGifticonGoodsPort.findByGoodsCode(GOODS_CODE)).thenReturn(Optional.of(goods));
        when(decryptGifticonPinPort.decrypt("encryptedPin")).thenReturn("900343630367");

        GetMyGifticonOrderDetailCommand command = new GetMyGifticonOrderDetailCommand(USER_ID, ORDER_ID);

        // when
        GetMyGifticonOrderDetailResult result = getMyGifticonOrderDetailService.getMyGifticonOrderDetail(command);

        // then — 2026-04-06 → 2026-05-04 = 30일
        assertThat(result.daysRemaining()).isEqualTo(30);
    }

    @Test
    @DisplayName("pinNo가 null이면 복호화를 시도하지 않고 null을 반환한다")
    void shouldReturnNullPinWhenPinNoIsNull() {
        // given
        GifticonOrder order = createOrderWithNullPin();
        GifticonGoods goods = createGoods();

        when(findGifticonOrderPort.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));
        when(findGifticonGoodsPort.findByGoodsCode(GOODS_CODE)).thenReturn(Optional.of(goods));

        GetMyGifticonOrderDetailCommand command = new GetMyGifticonOrderDetailCommand(USER_ID, ORDER_ID);

        // when
        GetMyGifticonOrderDetailResult result = getMyGifticonOrderDetailService.getMyGifticonOrderDetail(command);

        // then
        assertThat(result.pinNo()).isNull();
        verifyNoInteractions(decryptGifticonPinPort);
    }

    private GifticonOrder createOrder(GifticonOrderStatus status) {
        return GifticonOrder.from(GifticonOrderSnapshotState.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .goodsCode(GOODS_CODE)
                .goodsName("테스트 기프티콘")
                .brandName("세븐일레븐")
                .productImageUrl("https://example.com/goods.jpg")
                .trId(TR_ID)
                .orderNo("20260401000001")
                .cashPrice(5000L)
                .orderStatus(status)
                .couponImageUrl("https://example.com/coupon.jpg")
                .pinNo("encryptedPin")
                .validEndDate(LocalDate.of(2026, 5, 4))
                .createdAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .build());
    }

    private GifticonOrder createOrderWithNullPin() {
        return GifticonOrder.from(GifticonOrderSnapshotState.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .goodsCode(GOODS_CODE)
                .goodsName("테스트 기프티콘")
                .brandName("세븐일레븐")
                .productImageUrl("https://example.com/goods.jpg")
                .trId(TR_ID)
                .orderNo("20260401000001")
                .cashPrice(5000L)
                .orderStatus(GifticonOrderStatus.PENDING)
                .validEndDate(LocalDate.of(2026, 5, 4))
                .createdAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .build());
    }

    private GifticonGoods createGoods() {
        return GifticonGoods.from(GifticonGoodsSnapshotState.builder()
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
    }
}
