package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.gifticon.GifticonOrder;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderSnapshotState;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderSortType;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderStatus;
import com.personal.marketnote.reward.port.in.command.gifticon.GetMyGifticonOrdersCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetMyGifticonOrdersResult;
import com.personal.marketnote.reward.port.in.result.gifticon.GetMyGifticonOrdersResult.MyGifticonOrderItem;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonOrderPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMyGifticonOrdersUseCaseTest {

    @InjectMocks
    private GetMyGifticonOrdersService getMyGifticonOrdersService;

    @Mock
    private FindGifticonOrderPort findGifticonOrderPort;

    @Spy
    private Clock clock = Clock.fixed(
            Instant.parse("2026-04-06T03:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    private static final Long USER_ID = 100L;

    @Test
    @DisplayName("AVAILABLE 필터로 조회하면 ISSUED 상태 주문만 반환한다")
    void shouldReturnOnlyIssuedOrdersWhenFilterIsAvailable() {
        // given
        List<GifticonOrderStatus> availableStatuses = List.of(GifticonOrderStatus.ISSUED);
        GifticonOrder issuedOrder = createOrder(1L, GifticonOrderStatus.ISSUED, LocalDate.of(2026, 5, 4));

        when(findGifticonOrderPort.findByUserIdAndStatuses(
                eq(USER_ID), eq(availableStatuses), eq(GifticonOrderSortType.PURCHASE_LATEST), eq(-1L), eq(11)
        )).thenReturn(List.of(issuedOrder));

        when(findGifticonOrderPort.countByUserIdAndStatuses(USER_ID, availableStatuses)).thenReturn(1L);
        when(findGifticonOrderPort.countByUserIdAndStatuses(
                USER_ID, List.of(GifticonOrderStatus.USED, GifticonOrderStatus.EXPIRED, GifticonOrderStatus.CANCELLED)
        )).thenReturn(3L);

        GetMyGifticonOrdersCommand command = new GetMyGifticonOrdersCommand(
                USER_ID, "AVAILABLE", "PURCHASE_LATEST", -1L, 10
        );

        // when
        GetMyGifticonOrdersResult result = getMyGifticonOrdersService.getMyGifticonOrders(command);

        // then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).orderStatus()).isEqualTo("ISSUED");
        assertThat(result.availableCount()).isEqualTo(1L);
        assertThat(result.completedOrExpiredCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("COMPLETED_OR_EXPIRED 필터로 조회하면 USED, EXPIRED, CANCELLED 상태 주문을 반환한다")
    void shouldReturnTerminalOrdersWhenFilterIsCompletedOrExpired() {
        // given
        List<GifticonOrderStatus> terminalStatuses = List.of(
                GifticonOrderStatus.USED, GifticonOrderStatus.EXPIRED, GifticonOrderStatus.CANCELLED
        );
        GifticonOrder usedOrder = createOrder(1L, GifticonOrderStatus.USED, LocalDate.of(2026, 3, 1));
        GifticonOrder expiredOrder = createOrder(2L, GifticonOrderStatus.EXPIRED, LocalDate.of(2026, 3, 15));

        when(findGifticonOrderPort.findByUserIdAndStatuses(
                eq(USER_ID), eq(terminalStatuses), eq(GifticonOrderSortType.PURCHASE_LATEST), eq(-1L), eq(11)
        )).thenReturn(List.of(usedOrder, expiredOrder));

        when(findGifticonOrderPort.countByUserIdAndStatuses(
                USER_ID, List.of(GifticonOrderStatus.ISSUED)
        )).thenReturn(2L);
        when(findGifticonOrderPort.countByUserIdAndStatuses(USER_ID, terminalStatuses)).thenReturn(5L);

        GetMyGifticonOrdersCommand command = new GetMyGifticonOrdersCommand(
                USER_ID, "COMPLETED_OR_EXPIRED", "PURCHASE_LATEST", -1L, 10
        );

        // when
        GetMyGifticonOrdersResult result = getMyGifticonOrdersService.getMyGifticonOrders(command);

        // then
        assertThat(result.items()).hasSize(2);
        assertThat(result.availableCount()).isEqualTo(2L);
        assertThat(result.completedOrExpiredCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션에서 다음 페이지가 존재하면 hasNext가 true이고 nextCursor가 반환된다")
    void shouldReturnHasNextTrueWhenMoreItemsExist() {
        // given
        List<GifticonOrderStatus> availableStatuses = List.of(GifticonOrderStatus.ISSUED);
        List<GifticonOrder> orders = List.of(
                createOrder(10L, GifticonOrderStatus.ISSUED, LocalDate.of(2026, 5, 4)),
                createOrder(9L, GifticonOrderStatus.ISSUED, LocalDate.of(2026, 5, 3)),
                createOrder(8L, GifticonOrderStatus.ISSUED, LocalDate.of(2026, 5, 2))
        );

        when(findGifticonOrderPort.findByUserIdAndStatuses(
                eq(USER_ID), eq(availableStatuses), eq(GifticonOrderSortType.PURCHASE_LATEST), eq(-1L), eq(3)
        )).thenReturn(orders);

        when(findGifticonOrderPort.countByUserIdAndStatuses(USER_ID, availableStatuses)).thenReturn(5L);
        when(findGifticonOrderPort.countByUserIdAndStatuses(
                USER_ID, List.of(GifticonOrderStatus.USED, GifticonOrderStatus.EXPIRED, GifticonOrderStatus.CANCELLED)
        )).thenReturn(0L);

        GetMyGifticonOrdersCommand command = new GetMyGifticonOrdersCommand(
                USER_ID, "AVAILABLE", "PURCHASE_LATEST", -1L, 2
        );

        // when
        GetMyGifticonOrdersResult result = getMyGifticonOrdersService.getMyGifticonOrders(command);

        // then
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(9L);
        assertThat(result.items()).hasSize(2);
    }

    @Test
    @DisplayName("마지막 페이지이면 hasNext가 false이고 nextCursor가 null이다")
    void shouldReturnHasNextFalseWhenNoMoreItems() {
        // given
        List<GifticonOrderStatus> availableStatuses = List.of(GifticonOrderStatus.ISSUED);
        GifticonOrder order = createOrder(1L, GifticonOrderStatus.ISSUED, LocalDate.of(2026, 5, 4));

        when(findGifticonOrderPort.findByUserIdAndStatuses(
                eq(USER_ID), eq(availableStatuses), eq(GifticonOrderSortType.PURCHASE_LATEST), eq(-1L), eq(11)
        )).thenReturn(List.of(order));

        when(findGifticonOrderPort.countByUserIdAndStatuses(USER_ID, availableStatuses)).thenReturn(1L);
        when(findGifticonOrderPort.countByUserIdAndStatuses(
                USER_ID, List.of(GifticonOrderStatus.USED, GifticonOrderStatus.EXPIRED, GifticonOrderStatus.CANCELLED)
        )).thenReturn(0L);

        GetMyGifticonOrdersCommand command = new GetMyGifticonOrdersCommand(
                USER_ID, "AVAILABLE", "PURCHASE_LATEST", -1L, 10
        );

        // when
        GetMyGifticonOrdersResult result = getMyGifticonOrdersService.getMyGifticonOrders(command);

        // then
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
    }

    @Test
    @DisplayName("daysRemaining이 유효기간까지 남은 일수로 정확히 계산된다")
    void shouldCalculateDaysRemainingCorrectly() {
        // given — clock은 2026-04-06 12:00 KST
        List<GifticonOrderStatus> availableStatuses = List.of(GifticonOrderStatus.ISSUED);
        GifticonOrder order = createOrder(1L, GifticonOrderStatus.ISSUED, LocalDate.of(2026, 5, 4));

        when(findGifticonOrderPort.findByUserIdAndStatuses(
                eq(USER_ID), eq(availableStatuses), eq(GifticonOrderSortType.PURCHASE_LATEST), eq(-1L), eq(11)
        )).thenReturn(List.of(order));

        when(findGifticonOrderPort.countByUserIdAndStatuses(USER_ID, availableStatuses)).thenReturn(1L);
        when(findGifticonOrderPort.countByUserIdAndStatuses(
                USER_ID, List.of(GifticonOrderStatus.USED, GifticonOrderStatus.EXPIRED, GifticonOrderStatus.CANCELLED)
        )).thenReturn(0L);

        GetMyGifticonOrdersCommand command = new GetMyGifticonOrdersCommand(
                USER_ID, "AVAILABLE", "PURCHASE_LATEST", -1L, 10
        );

        // when
        GetMyGifticonOrdersResult result = getMyGifticonOrdersService.getMyGifticonOrders(command);

        // then — 2026-04-06 → 2026-05-04 = 30일
        assertThat(result.items().get(0).daysRemaining()).isEqualTo(30);
    }

    @Test
    @DisplayName("statusLabel이 USED이면 '사용완료', EXPIRED이면 '기간만료', CANCELLED이면 '취소됨'을 반환한다")
    void shouldReturnCorrectStatusLabel() {
        // given
        List<GifticonOrderStatus> terminalStatuses = List.of(
                GifticonOrderStatus.USED, GifticonOrderStatus.EXPIRED, GifticonOrderStatus.CANCELLED
        );
        GifticonOrder usedOrder = createOrder(1L, GifticonOrderStatus.USED, LocalDate.of(2026, 3, 1));
        GifticonOrder expiredOrder = createOrder(2L, GifticonOrderStatus.EXPIRED, LocalDate.of(2026, 3, 15));
        GifticonOrder cancelledOrder = createOrder(3L, GifticonOrderStatus.CANCELLED, LocalDate.of(2026, 4, 1));

        when(findGifticonOrderPort.findByUserIdAndStatuses(
                eq(USER_ID), eq(terminalStatuses), eq(GifticonOrderSortType.PURCHASE_LATEST), eq(-1L), eq(11)
        )).thenReturn(List.of(usedOrder, expiredOrder, cancelledOrder));

        when(findGifticonOrderPort.countByUserIdAndStatuses(
                USER_ID, List.of(GifticonOrderStatus.ISSUED)
        )).thenReturn(0L);
        when(findGifticonOrderPort.countByUserIdAndStatuses(USER_ID, terminalStatuses)).thenReturn(3L);

        GetMyGifticonOrdersCommand command = new GetMyGifticonOrdersCommand(
                USER_ID, "COMPLETED_OR_EXPIRED", "PURCHASE_LATEST", -1L, 10
        );

        // when
        GetMyGifticonOrdersResult result = getMyGifticonOrdersService.getMyGifticonOrders(command);

        // then
        assertThat(result.items().get(0).statusLabel()).isEqualTo("사용완료");
        assertThat(result.items().get(1).statusLabel()).isEqualTo("기간만료");
        assertThat(result.items().get(2).statusLabel()).isEqualTo("취소됨");
    }

    @Test
    @DisplayName("expiryDate가 'YY.MM.DD까지 사용 가능' 포맷으로 반환된다")
    void shouldFormatExpiryDateCorrectly() {
        // given
        List<GifticonOrderStatus> availableStatuses = List.of(GifticonOrderStatus.ISSUED);
        GifticonOrder order = createOrder(1L, GifticonOrderStatus.ISSUED, LocalDate.of(2026, 5, 4));

        when(findGifticonOrderPort.findByUserIdAndStatuses(
                eq(USER_ID), eq(availableStatuses), eq(GifticonOrderSortType.PURCHASE_LATEST), eq(-1L), eq(11)
        )).thenReturn(List.of(order));

        when(findGifticonOrderPort.countByUserIdAndStatuses(USER_ID, availableStatuses)).thenReturn(1L);
        when(findGifticonOrderPort.countByUserIdAndStatuses(
                USER_ID, List.of(GifticonOrderStatus.USED, GifticonOrderStatus.EXPIRED, GifticonOrderStatus.CANCELLED)
        )).thenReturn(0L);

        GetMyGifticonOrdersCommand command = new GetMyGifticonOrdersCommand(
                USER_ID, "AVAILABLE", "PURCHASE_LATEST", -1L, 10
        );

        // when
        GetMyGifticonOrdersResult result = getMyGifticonOrdersService.getMyGifticonOrders(command);

        // then
        assertThat(result.items().get(0).expiryDate()).isEqualTo("26.05.04까지 사용 가능");
    }

    @Test
    @DisplayName("주문이 없으면 빈 목록과 0건수를 반환한다")
    void shouldReturnEmptyResultWhenNoOrders() {
        // given
        List<GifticonOrderStatus> availableStatuses = List.of(GifticonOrderStatus.ISSUED);

        when(findGifticonOrderPort.findByUserIdAndStatuses(
                eq(USER_ID), eq(availableStatuses), eq(GifticonOrderSortType.PURCHASE_LATEST), eq(-1L), eq(11)
        )).thenReturn(Collections.emptyList());

        when(findGifticonOrderPort.countByUserIdAndStatuses(USER_ID, availableStatuses)).thenReturn(0L);
        when(findGifticonOrderPort.countByUserIdAndStatuses(
                USER_ID, List.of(GifticonOrderStatus.USED, GifticonOrderStatus.EXPIRED, GifticonOrderStatus.CANCELLED)
        )).thenReturn(0L);

        GetMyGifticonOrdersCommand command = new GetMyGifticonOrdersCommand(
                USER_ID, "AVAILABLE", "PURCHASE_LATEST", -1L, 10
        );

        // when
        GetMyGifticonOrdersResult result = getMyGifticonOrdersService.getMyGifticonOrders(command);

        // then
        assertThat(result.items()).isEmpty();
        assertThat(result.availableCount()).isZero();
        assertThat(result.completedOrExpiredCount()).isZero();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
    }

    @Test
    @DisplayName("EXPIRY_SOONEST 정렬로 조회하면 Port에 해당 정렬 타입이 전달된다")
    void shouldPassExpirySoonestSortTypeToPort() {
        // given
        List<GifticonOrderStatus> availableStatuses = List.of(GifticonOrderStatus.ISSUED);

        when(findGifticonOrderPort.findByUserIdAndStatuses(
                eq(USER_ID), eq(availableStatuses), eq(GifticonOrderSortType.EXPIRY_SOONEST), eq(-1L), eq(11)
        )).thenReturn(Collections.emptyList());

        when(findGifticonOrderPort.countByUserIdAndStatuses(USER_ID, availableStatuses)).thenReturn(0L);
        when(findGifticonOrderPort.countByUserIdAndStatuses(
                USER_ID, List.of(GifticonOrderStatus.USED, GifticonOrderStatus.EXPIRED, GifticonOrderStatus.CANCELLED)
        )).thenReturn(0L);

        GetMyGifticonOrdersCommand command = new GetMyGifticonOrdersCommand(
                USER_ID, "AVAILABLE", "EXPIRY_SOONEST", -1L, 10
        );

        // when
        getMyGifticonOrdersService.getMyGifticonOrders(command);

        // then
        verify(findGifticonOrderPort).findByUserIdAndStatuses(
                USER_ID, availableStatuses, GifticonOrderSortType.EXPIRY_SOONEST, -1L, 11
        );
    }

    @Test
    @DisplayName("EXPIRY_SOONEST 정렬에서 다음 페이지가 존재하면 nextCursor가 offset 기반으로 반환된다")
    void shouldReturnOffsetBasedNextCursorForExpirySoonest() {
        // given
        List<GifticonOrderStatus> availableStatuses = List.of(GifticonOrderStatus.ISSUED);
        List<GifticonOrder> orders = List.of(
                createOrder(5L, GifticonOrderStatus.ISSUED, LocalDate.of(2026, 5, 1)),
                createOrder(3L, GifticonOrderStatus.ISSUED, LocalDate.of(2026, 5, 2)),
                createOrder(7L, GifticonOrderStatus.ISSUED, LocalDate.of(2026, 5, 3))
        );

        when(findGifticonOrderPort.findByUserIdAndStatuses(
                eq(USER_ID), eq(availableStatuses), eq(GifticonOrderSortType.EXPIRY_SOONEST), eq(-1L), eq(3)
        )).thenReturn(orders);

        when(findGifticonOrderPort.countByUserIdAndStatuses(USER_ID, availableStatuses)).thenReturn(5L);
        when(findGifticonOrderPort.countByUserIdAndStatuses(
                USER_ID, List.of(GifticonOrderStatus.USED, GifticonOrderStatus.EXPIRED, GifticonOrderStatus.CANCELLED)
        )).thenReturn(0L);

        GetMyGifticonOrdersCommand command = new GetMyGifticonOrdersCommand(
                USER_ID, "AVAILABLE", "EXPIRY_SOONEST", -1L, 2
        );

        // when
        GetMyGifticonOrdersResult result = getMyGifticonOrdersService.getMyGifticonOrders(command);

        // then — EXPIRY_SOONEST: nextCursor = currentOffset(0) + pageSize(2) = 2
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(2L);
        assertThat(result.items()).hasSize(2);
    }

    @Test
    @DisplayName("ISSUED 상태 주문의 statusLabel은 null이다")
    void shouldReturnNullStatusLabelForIssuedOrder() {
        // given
        List<GifticonOrderStatus> availableStatuses = List.of(GifticonOrderStatus.ISSUED);
        GifticonOrder order = createOrder(1L, GifticonOrderStatus.ISSUED, LocalDate.of(2026, 5, 4));

        when(findGifticonOrderPort.findByUserIdAndStatuses(
                eq(USER_ID), eq(availableStatuses), eq(GifticonOrderSortType.PURCHASE_LATEST), eq(-1L), eq(11)
        )).thenReturn(List.of(order));

        when(findGifticonOrderPort.countByUserIdAndStatuses(USER_ID, availableStatuses)).thenReturn(1L);
        when(findGifticonOrderPort.countByUserIdAndStatuses(
                USER_ID, List.of(GifticonOrderStatus.USED, GifticonOrderStatus.EXPIRED, GifticonOrderStatus.CANCELLED)
        )).thenReturn(0L);

        GetMyGifticonOrdersCommand command = new GetMyGifticonOrdersCommand(
                USER_ID, "AVAILABLE", "PURCHASE_LATEST", -1L, 10
        );

        // when
        GetMyGifticonOrdersResult result = getMyGifticonOrdersService.getMyGifticonOrders(command);

        // then
        assertThat(result.items().get(0).statusLabel()).isNull();
    }

    @Test
    @DisplayName("각 주문 항목에 goodsName, brandName, productImageUrl, cashPrice, createdAt이 포함된다")
    void shouldIncludeAllFieldsInOrderItem() {
        // given
        List<GifticonOrderStatus> availableStatuses = List.of(GifticonOrderStatus.ISSUED);
        GifticonOrder order = createOrder(1L, GifticonOrderStatus.ISSUED, LocalDate.of(2026, 5, 4));

        when(findGifticonOrderPort.findByUserIdAndStatuses(
                eq(USER_ID), eq(availableStatuses), eq(GifticonOrderSortType.PURCHASE_LATEST), eq(-1L), eq(11)
        )).thenReturn(List.of(order));

        when(findGifticonOrderPort.countByUserIdAndStatuses(USER_ID, availableStatuses)).thenReturn(1L);
        when(findGifticonOrderPort.countByUserIdAndStatuses(
                USER_ID, List.of(GifticonOrderStatus.USED, GifticonOrderStatus.EXPIRED, GifticonOrderStatus.CANCELLED)
        )).thenReturn(0L);

        GetMyGifticonOrdersCommand command = new GetMyGifticonOrdersCommand(
                USER_ID, "AVAILABLE", "PURCHASE_LATEST", -1L, 10
        );

        // when
        GetMyGifticonOrdersResult result = getMyGifticonOrdersService.getMyGifticonOrders(command);

        // then
        MyGifticonOrderItem item = result.items().get(0);
        assertThat(item.orderId()).isEqualTo(1L);
        assertThat(item.goodsName()).isEqualTo("테스트 기프티콘");
        assertThat(item.brandName()).isEqualTo("세븐일레븐");
        assertThat(item.productImageUrl()).isEqualTo("https://example.com/goods.jpg");
        assertThat(item.cashPrice()).isEqualTo(5000L);
        assertThat(item.createdAt()).isEqualTo(LocalDateTime.of(2026, 4, 1, 10, 0));
    }

    private GifticonOrder createOrder(Long id, GifticonOrderStatus status, LocalDate validEndDate) {
        return GifticonOrder.from(GifticonOrderSnapshotState.builder()
                .id(id)
                .userId(USER_ID)
                .goodsCode("G00000280811")
                .goodsName("테스트 기프티콘")
                .brandName("세븐일레븐")
                .productImageUrl("https://example.com/goods.jpg")
                .trId("NC100_260401100000")
                .orderNo("20260401000001")
                .cashPrice(5000L)
                .orderStatus(status)
                .couponImageUrl("https://example.com/coupon.jpg")
                .pinNo("encryptedPin")
                .validEndDate(validEndDate)
                .createdAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .build());
    }
}
