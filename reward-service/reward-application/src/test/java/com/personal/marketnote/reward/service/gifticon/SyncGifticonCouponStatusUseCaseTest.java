package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.gifticon.GifticonOrder;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderSnapshotState;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderStatus;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonOrderPort;
import com.personal.marketnote.reward.port.out.gifticon.QueryGifticonCouponStatusPort;
import com.personal.marketnote.reward.port.out.gifticon.QueryGifticonCouponStatusPort.CouponStatusResult;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonOrderPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncGifticonCouponStatusUseCaseTest {

    @InjectMocks
    private SyncGifticonCouponStatusService syncGifticonCouponStatusService;

    @Mock
    private FindGifticonOrderPort findGifticonOrderPort;

    @Mock
    private QueryGifticonCouponStatusPort queryGifticonCouponStatusPort;

    @Mock
    private UpdateGifticonOrderPort updateGifticonOrderPort;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Test
    @DisplayName("ISSUED 상태 쿠폰이 없으면 API 호출 없이 정상 종료한다")
    void shouldCompleteWithoutApiCallWhenNoIssuedOrders() {
        // given
        when(findGifticonOrderPort.findAllByOrderStatus(GifticonOrderStatus.ISSUED))
                .thenReturn(List.of());

        // when
        syncGifticonCouponStatusService.syncCouponStatuses();

        // then
        verifyNoInteractions(queryGifticonCouponStatusPort);
        verifyNoInteractions(updateGifticonOrderPort);
    }

    @Test
    @DisplayName("pinStatusCd가 02(교환)이면 USED로 상태를 전이한다")
    void shouldTransitionToUsedWhenPinStatusIs02() {
        // given
        GifticonOrder order = createIssuedOrder("TR001");
        when(findGifticonOrderPort.findAllByOrderStatus(GifticonOrderStatus.ISSUED))
                .thenReturn(List.of(order));
        when(queryGifticonCouponStatusPort.queryStatus("TR001"))
                .thenReturn(successResult("02"));
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class)
                    .doInTransaction(null);
            return null;
        });

        // when
        syncGifticonCouponStatusService.syncCouponStatuses();

        // then
        verify(updateGifticonOrderPort).update(order);
    }

    @Test
    @DisplayName("pinStatusCd가 08(기간만료)이면 EXPIRED로 상태를 전이한다")
    void shouldTransitionToExpiredWhenPinStatusIs08() {
        // given
        GifticonOrder order = createIssuedOrder("TR002");
        when(findGifticonOrderPort.findAllByOrderStatus(GifticonOrderStatus.ISSUED))
                .thenReturn(List.of(order));
        when(queryGifticonCouponStatusPort.queryStatus("TR002"))
                .thenReturn(successResult("08"));
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class)
                    .doInTransaction(null);
            return null;
        });

        // when
        syncGifticonCouponStatusService.syncCouponStatuses();

        // then
        verify(updateGifticonOrderPort).update(order);
    }

    @Test
    @DisplayName("pinStatusCd가 11(잔액기간만료)이면 EXPIRED로 상태를 전이한다")
    void shouldTransitionToExpiredWhenPinStatusIs11() {
        // given
        GifticonOrder order = createIssuedOrder("TR003");
        when(findGifticonOrderPort.findAllByOrderStatus(GifticonOrderStatus.ISSUED))
                .thenReturn(List.of(order));
        when(queryGifticonCouponStatusPort.queryStatus("TR003"))
                .thenReturn(successResult("11"));
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class)
                    .doInTransaction(null);
            return null;
        });

        // when
        syncGifticonCouponStatusService.syncCouponStatuses();

        // then
        verify(updateGifticonOrderPort).update(order);
    }

    @Test
    @DisplayName("pinStatusCd가 07(구매취소)이면 CANCELLED로 상태를 전이한다")
    void shouldTransitionToCancelledWhenPinStatusIs07() {
        // given
        GifticonOrder order = createIssuedOrder("TR004");
        when(findGifticonOrderPort.findAllByOrderStatus(GifticonOrderStatus.ISSUED))
                .thenReturn(List.of(order));
        when(queryGifticonCouponStatusPort.queryStatus("TR004"))
                .thenReturn(successResult("07"));
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class)
                    .doInTransaction(null);
            return null;
        });

        // when
        syncGifticonCouponStatusService.syncCouponStatuses();

        // then
        verify(updateGifticonOrderPort).update(order);
    }

    @Test
    @DisplayName("pinStatusCd가 03(반품)이면 CANCELLED로 상태를 전이한다")
    void shouldTransitionToCancelledWhenPinStatusIs03() {
        // given
        GifticonOrder order = createIssuedOrder("TR005");
        when(findGifticonOrderPort.findAllByOrderStatus(GifticonOrderStatus.ISSUED))
                .thenReturn(List.of(order));
        when(queryGifticonCouponStatusPort.queryStatus("TR005"))
                .thenReturn(successResult("03"));
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class)
                    .doInTransaction(null);
            return null;
        });

        // when
        syncGifticonCouponStatusService.syncCouponStatuses();

        // then
        verify(updateGifticonOrderPort).update(order);
    }

    @Test
    @DisplayName("pinStatusCd가 01(발행)이면 상태를 변경하지 않는다")
    void shouldSkipWhenPinStatusIs01Issued() {
        // given
        GifticonOrder order = createIssuedOrder("TR006");
        when(findGifticonOrderPort.findAllByOrderStatus(GifticonOrderStatus.ISSUED))
                .thenReturn(List.of(order));
        when(queryGifticonCouponStatusPort.queryStatus("TR006"))
                .thenReturn(successResult("01"));

        // when
        syncGifticonCouponStatusService.syncCouponStatuses();

        // then
        verifyNoInteractions(updateGifticonOrderPort);
        verifyNoInteractions(transactionTemplate);
    }

    @Test
    @DisplayName("pinStatusCd가 06(재발행)이면 상태를 변경하지 않는다")
    void shouldSkipWhenPinStatusIs06Reissued() {
        // given
        GifticonOrder order = createIssuedOrder("TR007");
        when(findGifticonOrderPort.findAllByOrderStatus(GifticonOrderStatus.ISSUED))
                .thenReturn(List.of(order));
        when(queryGifticonCouponStatusPort.queryStatus("TR007"))
                .thenReturn(successResult("06"));

        // when
        syncGifticonCouponStatusService.syncCouponStatuses();

        // then
        verifyNoInteractions(updateGifticonOrderPort);
        verifyNoInteractions(transactionTemplate);
    }

    @Test
    @DisplayName("매핑되지 않는 핀상태(04)이면 상태를 변경하지 않는다")
    void shouldSkipWhenPinStatusIsUnmapped() {
        // given
        GifticonOrder order = createIssuedOrder("TR008");
        when(findGifticonOrderPort.findAllByOrderStatus(GifticonOrderStatus.ISSUED))
                .thenReturn(List.of(order));
        when(queryGifticonCouponStatusPort.queryStatus("TR008"))
                .thenReturn(successResult("04"));

        // when
        syncGifticonCouponStatusService.syncCouponStatuses();

        // then
        verifyNoInteractions(updateGifticonOrderPort);
        verifyNoInteractions(transactionTemplate);
    }

    @Test
    @DisplayName("API 호출 실패 시 해당 건만 건너뛰고 나머지 쿠폰 동기화를 계속 진행한다")
    void shouldContinueWhenApiCallFails() {
        // given
        GifticonOrder failOrder = createIssuedOrder("TR_FAIL");
        GifticonOrder successOrder = createIssuedOrder("TR_SUCCESS");
        when(findGifticonOrderPort.findAllByOrderStatus(GifticonOrderStatus.ISSUED))
                .thenReturn(List.of(failOrder, successOrder));
        when(queryGifticonCouponStatusPort.queryStatus("TR_FAIL"))
                .thenReturn(failureResult());
        when(queryGifticonCouponStatusPort.queryStatus("TR_SUCCESS"))
                .thenReturn(successResult("02"));
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class)
                    .doInTransaction(null);
            return null;
        });

        // when
        syncGifticonCouponStatusService.syncCouponStatuses();

        // then
        verify(updateGifticonOrderPort).update(successOrder);
        verify(updateGifticonOrderPort, never()).update(failOrder);
    }

    @Test
    @DisplayName("개별 쿠폰 처리 중 예외가 발생해도 나머지 쿠폰 동기화를 계속 진행한다")
    void shouldContinueWhenExceptionOccursDuringProcessing() {
        // given
        GifticonOrder exceptionOrder = createIssuedOrder("TR_EX");
        GifticonOrder normalOrder = createIssuedOrder("TR_NORMAL");
        when(findGifticonOrderPort.findAllByOrderStatus(GifticonOrderStatus.ISSUED))
                .thenReturn(List.of(exceptionOrder, normalOrder));
        when(queryGifticonCouponStatusPort.queryStatus("TR_EX"))
                .thenThrow(new RuntimeException("연결 오류"));
        when(queryGifticonCouponStatusPort.queryStatus("TR_NORMAL"))
                .thenReturn(successResult("02"));
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class)
                    .doInTransaction(null);
            return null;
        });

        // when
        syncGifticonCouponStatusService.syncCouponStatuses();

        // then
        verify(updateGifticonOrderPort).update(normalOrder);
    }

    private GifticonOrder createIssuedOrder(String trId) {
        return GifticonOrder.from(GifticonOrderSnapshotState.builder()
                .id(1L)
                .userId(100L)
                .goodsCode("G001")
                .goodsName("테스트 상품")
                .brandName("테스트 브랜드")
                .productImageUrl("https://example.com/img.jpg")
                .trId(trId)
                .orderNo("ORD001")
                .cashPrice(10000L)
                .orderStatus(GifticonOrderStatus.ISSUED)
                .couponImageUrl("https://example.com/coupon.jpg")
                .pinNo("encrypted-pin")
                .validEndDate(LocalDate.of(2026, 5, 1))
                .createdAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .build());
    }

    private CouponStatusResult successResult(String pinStatusCd) {
        return CouponStatusResult.builder()
                .success(true)
                .pinStatusCd(pinStatusCd)
                .build();
    }

    private CouponStatusResult failureResult() {
        return CouponStatusResult.builder()
                .success(false)
                .errorCode("ERR0000")
                .errorMessage("조회 실패")
                .build();
    }
}
