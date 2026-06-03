package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.port.in.command.order.GetBuyerOrderHistoryQuery;
import com.personal.marketnote.commerce.port.in.result.order.GetOrderCountResult;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.order.FindOrderProductPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetBuyerOrderCountUseCaseTest {
    @Mock
    private FindOrderPort findOrderPort;
    @Mock
    private FindOrderProductPort findOrderProductPort;
    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-03-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private GetOrderService getOrderService;

    @Captor
    private ArgumentCaptor<Long> buyerIdCaptor;
    @Captor
    private ArgumentCaptor<LocalDateTime> startDateCaptor;
    @Captor
    private ArgumentCaptor<LocalDateTime> endDateCaptor;
    @Captor
    private ArgumentCaptor<List<OrderStatus>> statusesCaptor;
    @Captor
    private ArgumentCaptor<List<Long>> pricePolicyIdsCaptor;

    // ==================================================================================
    // 기본 조회 성공 케이스
    // ==================================================================================

    @Nested
    @DisplayName("구매자 주문 내역 개수 조회 성공 케이스")
    class GetBuyerOrderCountSuccessTest {

        @Test
        @DisplayName("주문이 1건 존재하면 1을 반환한다")
        void returnsOneWhenSingleOrderExists() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("주문이 3건 존재하면 3을 반환한다")
        void returnsThreeWhenThreeOrdersExist() {
            Long buyerId = 1L;
            Order order1 = createOrderWithProducts(1L, buyerId, List.of(100L));
            Order order2 = createOrderWithProducts(2L, buyerId, List.of(200L));
            Order order3 = createOrderWithProducts(3L, buyerId, List.of(300L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order1, order2, order3));

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("주문이 다수 존재하면 정확한 개수를 반환한다")
        void returnsExactCountForManyOrders() {
            Long buyerId = 1L;
            List<Order> orders = java.util.stream.LongStream.rangeClosed(1, 10)
                    .mapToObj(id -> createOrderWithProducts(id, buyerId, List.of(id * 100)))
                    .toList();
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(orders);

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(10);
        }

        @Test
        @DisplayName("반환된 결과의 totalCount는 음수가 아니다")
        void resultTotalCountIsNotNegative() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of());

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isGreaterThanOrEqualTo(0);
        }
    }

    // ==================================================================================
    // 빈 결과 케이스
    // ==================================================================================

    @Nested
    @DisplayName("빈 결과 케이스")
    class EmptyResultTest {

        @Test
        @DisplayName("findOrderPort가 빈 리스트를 반환하면 0을 반환한다")
        void returnsZeroWhenPortReturnsEmptyList() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of());

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("findOrderPort가 null을 반환하면 0을 반환한다")
        void returnsZeroWhenPortReturnsNull() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(null);

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(0);
        }
    }

    // ==================================================================================
    // 상품명 필터 케이스
    // ==================================================================================

    @Nested
    @DisplayName("상품명 필터 케이스")
    class ProductNameFilterTest {

        @Test
        @DisplayName("상품명이 null이면 전체 주문 개수를 반환한다")
        void returnsAllOrderCountWhenProductNameIsNull() {
            Long buyerId = 1L;
            Order order1 = createOrderWithProducts(1L, buyerId, List.of(100L));
            Order order2 = createOrderWithProducts(2L, buyerId, List.of(200L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order1, order2));

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("상품명이 빈 문자열이면 전체 주문 개수를 반환한다")
        void returnsAllOrderCountWhenProductNameIsEmpty() {
            Long buyerId = 1L;
            Order order1 = createOrderWithProducts(1L, buyerId, List.of(100L));
            Order order2 = createOrderWithProducts(2L, buyerId, List.of(200L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order1, order2));

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("상품명이 공백 문자열이면 전체 주문 개수를 반환한다")
        void returnsAllOrderCountWhenProductNameIsBlank() {
            Long buyerId = 1L;
            Order order1 = createOrderWithProducts(1L, buyerId, List.of(100L));
            Order order2 = createOrderWithProducts(2L, buyerId, List.of(200L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "   ");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order1, order2));

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("상품명과 일치하는 주문만 개수에 포함된다")
        void countsOnlyOrdersMatchingProductName() {
            Long buyerId = 1L;
            Order order1 = createOrderWithProducts(1L, buyerId, List.of(100L));
            Order order2 = createOrderWithProducts(2L, buyerId, List.of(200L));
            Order order3 = createOrderWithProducts(3L, buyerId, List.of(300L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "나이키");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order1, order2, order3));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "나이키 에어맥스"),
                            200L, createProductInfo(200L, "아디다스 울트라부스트"),
                            300L, createProductInfo(300L, "나이키 덩크")
                    ));

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("상품명과 부분 일치하는 주문도 개수에 포함된다")
        void countsOrdersWithPartialProductNameMatch() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "에어");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "나이키 에어맥스 90")));

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("상품명 필터링은 대소문자를 구분하지 않는다")
        void productNameFilterIsCaseInsensitive() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "NIKE");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "Nike Air Max")));

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("상품명과 일치하는 주문이 없으면 0을 반환한다")
        void returnsZeroWhenNoProductNameMatch() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "아디다스");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "나이키 에어맥스")));

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("상품명 앞뒤 공백은 trim 후 필터링된다")
        void productNameIsTrimmedBeforeFiltering() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "  나이키  ");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "나이키 에어맥스")));

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("상품 정보 맵에 pricePolicyId가 없으면 해당 주문은 개수에서 제외된다")
        void excludesOrderWhenProductInfoNotFoundForPricePolicyId() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "상품A");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(999L, createProductInfo(999L, "상품A")));

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("상품 정보의 name이 null이면 해당 주문은 개수에서 제외된다")
        void excludesOrderWhenProductNameIsNullInProductInfo() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "상품A");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, null)));

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(0);
        }
    }

    // ==================================================================================
    // 쿼리 파라미터 전달 검증
    // ==================================================================================

    @Nested
    @DisplayName("쿼리 파라미터 전달 검증")
    class QueryParameterPassingTest {

        @Test
        @DisplayName("buyerId를 정확히 findOrderPort에 전달한다")
        void passesBuyerIdToFindOrderPort() {
            Long buyerId = 42L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderCount(query);

            verify(findOrderPort).findByBuyerId(
                    buyerIdCaptor.capture(), any(), any(), anyList()
            );
            assertThat(buyerIdCaptor.getValue()).isEqualTo(42L);
        }

        @Test
        @DisplayName("endDate는 내일 자정으로 전달된다")
        void passesTomorrowMidnightAsEndDate() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderCount(query);

            verify(findOrderPort).findByBuyerId(
                    any(), any(), endDateCaptor.capture(), anyList()
            );
            LocalDate today = LocalDate.now();
            LocalDateTime expectedEndDate = LocalDateTime.of(today.plusDays(1), LocalTime.MIDNIGHT);
            assertThat(endDateCaptor.getValue()).isEqualTo(expectedEndDate);
        }

        @Test
        @DisplayName("period가 null이면 startDate가 null로 전달된다")
        void passesNullStartDateWhenPeriodIsNull() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderCount(query);

            verify(findOrderPort).findByBuyerId(
                    any(), startDateCaptor.capture(), any(), anyList()
            );
            assertThat(startDateCaptor.getValue()).isNull();
        }

        @Test
        @DisplayName("period가 ONE_MONTH이면 1개월 전 자정이 startDate로 전달된다")
        void passesOneMonthAgoStartDateWhenPeriodIsOneMonth() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, OrderPeriod.ONE_MONTH, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderCount(query);

            verify(findOrderPort).findByBuyerId(
                    any(), startDateCaptor.capture(), any(), anyList()
            );
            LocalDate today = LocalDate.now();
            LocalDateTime expectedStartDate = LocalDateTime.of(today.minusMonths(1), LocalTime.MIDNIGHT);
            assertThat(startDateCaptor.getValue()).isEqualTo(expectedStartDate);
        }

        @Test
        @DisplayName("period가 THREE_MONTHS이면 3개월 전 자정이 startDate로 전달된다")
        void passesThreeMonthsAgoStartDateWhenPeriodIsThreeMonths() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, OrderPeriod.THREE_MONTHS, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderCount(query);

            verify(findOrderPort).findByBuyerId(
                    any(), startDateCaptor.capture(), any(), anyList()
            );
            LocalDate today = LocalDate.now();
            LocalDateTime expectedStartDate = LocalDateTime.of(today.minusMonths(3), LocalTime.MIDNIGHT);
            assertThat(startDateCaptor.getValue()).isEqualTo(expectedStartDate);
        }

        @Test
        @DisplayName("period가 SIX_MONTHS이면 6개월 전 자정이 startDate로 전달된다")
        void passesSixMonthsAgoStartDateWhenPeriodIsSixMonths() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, OrderPeriod.SIX_MONTHS, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderCount(query);

            verify(findOrderPort).findByBuyerId(
                    any(), startDateCaptor.capture(), any(), anyList()
            );
            LocalDate today = LocalDate.now();
            LocalDateTime expectedStartDate = LocalDateTime.of(today.minusMonths(6), LocalTime.MIDNIGHT);
            assertThat(startDateCaptor.getValue()).isEqualTo(expectedStartDate);
        }

        @Test
        @DisplayName("period가 ONE_YEAR이면 12개월 전 자정이 startDate로 전달된다")
        void passesOneYearAgoStartDateWhenPeriodIsOneYear() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, OrderPeriod.ONE_YEAR, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderCount(query);

            verify(findOrderPort).findByBuyerId(
                    any(), startDateCaptor.capture(), any(), anyList()
            );
            LocalDate today = LocalDate.now();
            LocalDateTime expectedStartDate = LocalDateTime.of(today.minusMonths(12), LocalTime.MIDNIGHT);
            assertThat(startDateCaptor.getValue()).isEqualTo(expectedStartDate);
        }

        @Test
        @DisplayName("period가 ALL이면 startDate가 null로 전달된다")
        void passesNullStartDateWhenPeriodIsAll() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, OrderPeriod.ALL, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderCount(query);

            verify(findOrderPort).findByBuyerId(
                    any(), startDateCaptor.capture(), any(), anyList()
            );
            assertThat(startDateCaptor.getValue()).isNull();
        }

        @Test
        @DisplayName("status가 null이면 빈 상태 리스트가 전달된다")
        void passesEmptyStatusListWhenStatusIsNull() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderCount(query);

            verify(findOrderPort).findByBuyerId(
                    any(), any(), any(), statusesCaptor.capture()
            );
            assertThat(statusesCaptor.getValue()).isEmpty();
        }

        @Test
        @DisplayName("status가 SHIPPING이면 배송 관련 상태들이 전달된다")
        void passesShippingRelatedStatusesWhenStatusIsShipping() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, OrderStatusFilter.SHIPPING, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderCount(query);

            verify(findOrderPort).findByBuyerId(
                    any(), any(), any(), statusesCaptor.capture()
            );
            assertThat(statusesCaptor.getValue()).containsExactlyInAnyOrder(
                    OrderStatus.PREPARING,
                    OrderStatus.SHIPPING
            );
        }

        @Test
        @DisplayName("status가 DELIVERED이면 DELIVERED 상태가 전달된다")
        void passesDeliveredStatusWhenStatusIsDelivered() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, OrderStatusFilter.DELIVERED, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderCount(query);

            verify(findOrderPort).findByBuyerId(
                    any(), any(), any(), statusesCaptor.capture()
            );
            assertThat(statusesCaptor.getValue()).containsExactly(OrderStatus.DELIVERED);
        }

        @Test
        @DisplayName("status가 CONFIRMED이면 CONFIRMED 상태가 전달된다")
        void passesConfirmedStatusWhenStatusIsConfirmed() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, OrderStatusFilter.CONFIRMED, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderCount(query);

            verify(findOrderPort).findByBuyerId(
                    any(), any(), any(), statusesCaptor.capture()
            );
            assertThat(statusesCaptor.getValue()).containsExactlyInAnyOrder(
                    OrderStatus.PARTIALLY_CONFIRMED,
                    OrderStatus.CONFIRMED
            );
        }

        @Test
        @DisplayName("status가 CANCEL_RETURN이면 취소/반품 관련 상태들이 전달된다")
        void passesCancelReturnStatusesWhenStatusIsCancelReturn() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, OrderStatusFilter.CANCEL_RETURN, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderCount(query);

            verify(findOrderPort).findByBuyerId(
                    any(), any(), any(), statusesCaptor.capture()
            );
            assertThat(statusesCaptor.getValue()).containsExactlyInAnyOrder(
                    OrderStatus.CANCELLED,
                    OrderStatus.RETURN_REQUESTED,
                    OrderStatus.RETURN_IN_PROGRESS,
                    OrderStatus.RETURN_INSPECTING,
                    OrderStatus.PARTIALLY_RETURNED,
                    OrderStatus.RETURNED
            );
        }

        @Test
        @DisplayName("status가 ALL이면 빈 상태 리스트가 전달된다")
        void passesEmptyStatusListWhenStatusIsAll() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, OrderStatusFilter.ALL, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderCount(query);

            verify(findOrderPort).findByBuyerId(
                    any(), any(), any(), statusesCaptor.capture()
            );
            assertThat(statusesCaptor.getValue()).isEmpty();
        }
    }

    // ==================================================================================
    // 포트 호출 검증
    // ==================================================================================

    @Nested
    @DisplayName("포트 호출 검증")
    class PortInvocationTest {

        @Test
        @DisplayName("findOrderPort.findByBuyerId를 정확히 한 번 호출한다")
        void callsFindOrderPortExactlyOnce() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderCount(query);

            verify(findOrderPort, times(1)).findByBuyerId(
                    eq(buyerId), any(), any(), anyList()
            );
        }

        @Test
        @DisplayName("상품명 키워드 없이 주문이 존재하면 findProductByPricePolicyPort를 호출하지 않는다")
        void doesNotCallProductPortWhenNoProductNameKeywordAndOrdersExist() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));

            getOrderService.getBuyerOrderCount(query);

            verifyNoInteractions(findProductByPricePolicyPort);
        }

        @Test
        @DisplayName("상품명 키워드가 있고 주문이 존재하면 findProductByPricePolicyPort를 호출한다")
        void callsProductPortWhenProductNameKeywordExistsAndOrdersExist() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "상품");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            getOrderService.getBuyerOrderCount(query);

            verify(findProductByPricePolicyPort, times(1)).findByPricePolicyIds(anyList());
        }

        @Test
        @DisplayName("주문이 없으면 findProductByPricePolicyPort를 호출하지 않는다")
        void doesNotCallProductPortWhenNoOrders() {
            Long buyerId = 1L;
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "상품");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderCount(query);

            verifyNoInteractions(findProductByPricePolicyPort);
        }

        @Test
        @DisplayName("findOrderProductPort와 상호작용하지 않는다")
        void doesNotInteractWithFindOrderProductPort() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));

            getOrderService.getBuyerOrderCount(query);

            verifyNoInteractions(findOrderProductPort);
        }

        @Test
        @DisplayName("상품명 키워드가 빈 문자열이면 findProductByPricePolicyPort를 호출하지 않는다")
        void doesNotCallProductPortWhenProductNameIsEmpty() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));

            getOrderService.getBuyerOrderCount(query);

            verifyNoInteractions(findProductByPricePolicyPort);
        }

        @Test
        @DisplayName("상품명 키워드가 공백 문자열이면 findProductByPricePolicyPort를 호출하지 않는다")
        void doesNotCallProductPortWhenProductNameIsBlank() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "   ");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));

            getOrderService.getBuyerOrderCount(query);

            verifyNoInteractions(findProductByPricePolicyPort);
        }

        @Test
        @DisplayName("주문 상품의 pricePolicyId 중복이면 distinct하여 호출한다")
        void deduplicatesPricePolicyIdsBeforeCallingPort() {
            Long buyerId = 1L;
            Order order1 = createOrderWithProducts(1L, buyerId, List.of(100L, 200L));
            Order order2 = createOrderWithProducts(2L, buyerId, List.of(200L, 300L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "상품");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order1, order2));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            200L, createProductInfo(200L, "상품B"),
                            300L, createProductInfo(300L, "상품C")
                    ));

            getOrderService.getBuyerOrderCount(query);

            verify(findProductByPricePolicyPort).findByPricePolicyIds(pricePolicyIdsCaptor.capture());
            assertThat(pricePolicyIdsCaptor.getValue()).hasSize(3);
            assertThat(pricePolicyIdsCaptor.getValue()).containsExactlyInAnyOrder(100L, 200L, 300L);
        }

        @Test
        @DisplayName("null인 pricePolicyId는 제외하고 호출한다")
        void excludesNullPricePolicyIdsBeforeCallingPort() {
            Long buyerId = 1L;
            Order order = createOrderWithMixedPricePolicyProducts(1L, buyerId, List.of(100L), 2);
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "상품");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(100L, createProductInfo(100L, "상품A")));

            getOrderService.getBuyerOrderCount(query);

            verify(findProductByPricePolicyPort).findByPricePolicyIds(pricePolicyIdsCaptor.capture());
            assertThat(pricePolicyIdsCaptor.getValue()).doesNotContainNull();
            assertThat(pricePolicyIdsCaptor.getValue()).containsExactly(100L);
        }

        @Test
        @DisplayName("모든 pricePolicyId가 null이고 상품명 키워드가 없으면 findProductByPricePolicyPort를 호출하지 않는다")
        void doesNotCallProductPortWhenAllPricePolicyIdsAreNullWithoutKeyword() {
            Long buyerId = 1L;
            Order order = createOrderWithNullPricePolicyProducts(1L, buyerId, 3);
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));

            getOrderService.getBuyerOrderCount(query);

            verifyNoInteractions(findProductByPricePolicyPort);
        }

        @Test
        @DisplayName("주문 상품이 빈 목록이고 상품명 키워드가 있으면 findProductByPricePolicyPort를 호출하지 않는다")
        void doesNotCallProductPortWhenOrderProductsAreEmptyWithKeyword() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of());
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "상품");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));

            getOrderService.getBuyerOrderCount(query);

            verifyNoInteractions(findProductByPricePolicyPort);
        }
    }

    // ==================================================================================
    // 엣지 케이스
    // ==================================================================================

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCaseTest {

        @Test
        @DisplayName("주문 상품이 빈 목록인 주문도 개수에 포함된다")
        void countsOrderWithEmptyProductList() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of());
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, null);

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("findProductByPricePolicyPort가 null을 반환하면 빈 맵으로 처리한다")
        void handlesNullReturnFromProductPort() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "상품");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(null);

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("주문에 여러 상품 중 하나라도 상품명과 일치하면 해당 주문이 개수에 포함된다")
        void countsOrderWhenAnyProductMatchesName() {
            Long buyerId = 1L;
            Order order = createOrderWithProducts(1L, buyerId, List.of(100L, 200L, 300L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "상품B");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            200L, createProductInfo(200L, "상품B"),
                            300L, createProductInfo(300L, "상품C")
                    ));

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("상품명 필터로 모든 주문이 제거되면 0을 반환한다")
        void returnsZeroWhenAllOrdersFilteredByProductName() {
            Long buyerId = 1L;
            Order order1 = createOrderWithProducts(1L, buyerId, List.of(100L));
            Order order2 = createOrderWithProducts(2L, buyerId, List.of(200L));
            GetBuyerOrderHistoryQuery query = createQuery(buyerId, null, null, "존재하지않는상품");

            when(findOrderPort.findByBuyerId(eq(buyerId), any(), any(), anyList()))
                    .thenReturn(List.of(order1, order2));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(
                            100L, createProductInfo(100L, "상품A"),
                            200L, createProductInfo(200L, "상품B")
                    ));

            GetOrderCountResult result = getOrderService.getBuyerOrderCount(query);

            assertThat(result.totalCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("buyerId가 다른 값이면 각각 올바른 buyerId로 호출한다")
        void passesCorrectBuyerIdForDifferentBuyers() {
            Long buyerId1 = 10L;
            Long buyerId2 = 20L;
            GetBuyerOrderHistoryQuery query1 = createQuery(buyerId1, null, null, null);
            GetBuyerOrderHistoryQuery query2 = createQuery(buyerId2, null, null, null);

            when(findOrderPort.findByBuyerId(any(), any(), any(), anyList()))
                    .thenReturn(List.of());

            getOrderService.getBuyerOrderCount(query1);
            getOrderService.getBuyerOrderCount(query2);

            verify(findOrderPort).findByBuyerId(eq(10L), any(), any(), anyList());
            verify(findOrderPort).findByBuyerId(eq(20L), any(), any(), anyList());
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private GetBuyerOrderHistoryQuery createQuery(
            Long buyerId,
            OrderPeriod period,
            OrderStatusFilter status,
            String productName
    ) {
        return GetBuyerOrderHistoryQuery.of(buyerId, period, status, productName);
    }

    private Order createOrderWithProducts(Long orderId, Long buyerId, List<Long> pricePolicyIds) {
        List<OrderProductSnapshotState> productStates = pricePolicyIds.stream()
                .map(pricePolicyId -> OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(OrderStatus.PAID)
                        .build())
                .toList();

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithNullPricePolicyProducts(Long orderId, Long buyerId, int count) {
        List<OrderProductSnapshotState> productStates = java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(null)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(OrderStatus.PAID)
                        .build())
                .toList();

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithMixedPricePolicyProducts(
            Long orderId, Long buyerId, List<Long> validPricePolicyIds, int nullPricePolicyCount
    ) {
        List<OrderProductSnapshotState> validStates = validPricePolicyIds.stream()
                .map(pricePolicyId -> OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(OrderStatus.PAID)
                        .build())
                .toList();

        List<OrderProductSnapshotState> nullStates = java.util.stream.IntStream.range(0, nullPricePolicyCount)
                .mapToObj(i -> OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(null)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(OrderStatus.PAID)
                        .build())
                .toList();

        List<OrderProductSnapshotState> allStates = new java.util.ArrayList<>(validStates);
        allStates.addAll(nullStates);

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(allStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private ProductInfoResult createProductInfo(Long id, String name) {
        return new ProductInfoResult(id, null, name, "브랜드", null, null, null, List.of());
    }
}
