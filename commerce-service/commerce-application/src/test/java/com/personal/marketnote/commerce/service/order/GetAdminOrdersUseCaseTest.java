package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.InvalidOrderDateRangeException;
import com.personal.marketnote.commerce.port.in.command.order.GetAdminOrdersQuery;
import com.personal.marketnote.commerce.port.in.result.order.GetAdminOrdersResult;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAdminOrdersUseCase 테스트")
class GetAdminOrdersUseCaseTest {

    @InjectMocks
    private GetAdminOrdersService getAdminOrdersService;

    @Mock
    private FindOrderPort findOrderPort;

    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;

    private Order createOrder(Long id, Long buyerId, OrderStatus status, Long pricePolicyId) {
        return Order.from(OrderSnapshotState.builder()
                .id(id)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD" + id)
                .orderStatus(status)
                .amount(OrderAmount.of(100000L, 95000L, 3000L, 2000L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(List.of(
                        OrderProductSnapshotState.builder()
                                .orderId(id)
                                .sellerId(10L)
                                .pricePolicyId(pricePolicyId)
                                .quantity(1)
                                .unitAmount(50000L)
                                .imageUrl("https://example.com/image.jpg")
                                .orderStatus(status)
                                .isReviewed(false)
                                .build()
                ))
                .createdAt(LocalDateTime.of(2026, 2, 20, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 2, 20, 10, 0))
                .build());
    }

    private ProductInfoResult createProductInfo(Long id, String name, String brandName) {
        return new ProductInfoResult(id, 10L, name, brandName, 50000L, null, null, List.of());
    }

    @Test
    @DisplayName("주문이 존재하면 주문 목록과 상품 정보를 반환한다")
    void shouldReturnOrdersWithProductInfo() {
        // given
        GetAdminOrdersQuery query = GetAdminOrdersQuery.of(null, null, null, null);
        Order order = createOrder(1L, 100L, OrderStatus.PAID, 30L);

        when(findOrderPort.findAllWithFilters(null, null, null, null))
                .thenReturn(List.of(order));
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(30L)))
                .thenReturn(Map.of(30L, createProductInfo(50L, "테스트 상품", "테스트 브랜드")));

        // when
        GetAdminOrdersResult result = getAdminOrdersService.getAdminOrders(query);

        // then
        assertThat(result.orders()).hasSize(1);
        assertThat(result.orders().get(0).id()).isEqualTo(1L);
        assertThat(result.orders().get(0).buyerId()).isEqualTo(100L);
        assertThat(result.orders().get(0).orderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(result.orders().get(0).orderProducts()).hasSize(1);
        assertThat(result.orders().get(0).orderProducts().get(0).productName()).isEqualTo("테스트 상품");
        verify(findOrderPort).findAllWithFilters(null, null, null, null);
        verify(findProductByPricePolicyPort).findByPricePolicyIds(List.of(30L));
    }

    @Test
    @DisplayName("여러 주문이 존재하면 모든 주문을 반환한다")
    void shouldReturnMultipleOrders() {
        // given
        GetAdminOrdersQuery query = GetAdminOrdersQuery.of(null, null, null, null);
        Order order1 = createOrder(1L, 100L, OrderStatus.PAID, 30L);
        Order order2 = createOrder(2L, 101L, OrderStatus.PREPARING, 31L);

        when(findOrderPort.findAllWithFilters(null, null, null, null))
                .thenReturn(List.of(order1, order2));
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(30L, 31L)))
                .thenReturn(Map.of(
                        30L, createProductInfo(50L, "상품A", "브랜드A"),
                        31L, createProductInfo(51L, "상품B", "브랜드B")
                ));

        // when
        GetAdminOrdersResult result = getAdminOrdersService.getAdminOrders(query);

        // then
        assertThat(result.orders()).hasSize(2);
        assertThat(result.orders().get(0).id()).isEqualTo(1L);
        assertThat(result.orders().get(1).id()).isEqualTo(2L);
    }

    @Test
    @DisplayName("주문이 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyWhenNoOrders() {
        // given
        GetAdminOrdersQuery query = GetAdminOrdersQuery.of(null, null, null, null);

        when(findOrderPort.findAllWithFilters(null, null, null, null))
                .thenReturn(List.of());

        // when
        GetAdminOrdersResult result = getAdminOrdersService.getAdminOrders(query);

        // then
        assertThat(result.orders()).isEmpty();
        verify(findOrderPort).findAllWithFilters(null, null, null, null);
        verifyNoInteractions(findProductByPricePolicyPort);
    }

    @Test
    @DisplayName("findOrderPort가 null을 반환하면 빈 목록을 반환한다")
    void shouldReturnEmptyWhenPortReturnsNull() {
        // given
        GetAdminOrdersQuery query = GetAdminOrdersQuery.of(null, null, null, null);

        when(findOrderPort.findAllWithFilters(null, null, null, null))
                .thenReturn(null);

        // when
        GetAdminOrdersResult result = getAdminOrdersService.getAdminOrders(query);

        // then
        assertThat(result.orders()).isEmpty();
        verifyNoInteractions(findProductByPricePolicyPort);
    }

    @Test
    @DisplayName("sellerId 필터를 정확히 전달한다")
    void shouldPassSellerIdFilter() {
        // given
        Long sellerId = 10L;
        GetAdminOrdersQuery query = GetAdminOrdersQuery.of(sellerId, null, null, null);

        when(findOrderPort.findAllWithFilters(sellerId, null, null, null))
                .thenReturn(List.of());

        // when
        getAdminOrdersService.getAdminOrders(query);

        // then
        verify(findOrderPort).findAllWithFilters(sellerId, null, null, null);
    }

    @Test
    @DisplayName("기간 필터를 정확히 전달한다")
    void shouldPassDateFilters() {
        // given
        LocalDateTime startDate = LocalDateTime.of(2026, 2, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 2, 28, 23, 59, 59);
        GetAdminOrdersQuery query = GetAdminOrdersQuery.of(null, startDate, endDate, null);

        when(findOrderPort.findAllWithFilters(null, startDate, endDate, null))
                .thenReturn(List.of());

        // when
        getAdminOrdersService.getAdminOrders(query);

        // then
        verify(findOrderPort).findAllWithFilters(null, startDate, endDate, null);
    }

    @Test
    @DisplayName("orderStatus 필터를 정확히 전달한다")
    void shouldPassOrderStatusFilter() {
        // given
        GetAdminOrdersQuery query = GetAdminOrdersQuery.of(null, null, null, OrderStatus.PAID);

        when(findOrderPort.findAllWithFilters(null, null, null, OrderStatus.PAID))
                .thenReturn(List.of());

        // when
        getAdminOrdersService.getAdminOrders(query);

        // then
        verify(findOrderPort).findAllWithFilters(null, null, null, OrderStatus.PAID);
    }

    @Test
    @DisplayName("모든 필터를 동시에 전달한다")
    void shouldPassAllFilters() {
        // given
        Long sellerId = 10L;
        LocalDateTime startDate = LocalDateTime.of(2026, 2, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 2, 28, 23, 59, 59);
        OrderStatus status = OrderStatus.CANCELLED;
        GetAdminOrdersQuery query = GetAdminOrdersQuery.of(sellerId, startDate, endDate, status);

        when(findOrderPort.findAllWithFilters(sellerId, startDate, endDate, status))
                .thenReturn(List.of());

        // when
        getAdminOrdersService.getAdminOrders(query);

        // then
        verify(findOrderPort).findAllWithFilters(sellerId, startDate, endDate, status);
    }

    @Test
    @DisplayName("주문이 없으면 상품 정보 포트를 호출하지 않는다")
    void shouldNotCallProductPortWhenNoOrders() {
        // given
        GetAdminOrdersQuery query = GetAdminOrdersQuery.of(null, null, null, null);

        when(findOrderPort.findAllWithFilters(null, null, null, null))
                .thenReturn(List.of());

        // when
        getAdminOrdersService.getAdminOrders(query);

        // then
        verifyNoInteractions(findProductByPricePolicyPort);
    }

    @Test
    @DisplayName("findProductByPricePolicyPort가 null을 반환하면 빈 맵으로 처리한다")
    void shouldHandleNullProductInfoMap() {
        // given
        GetAdminOrdersQuery query = GetAdminOrdersQuery.of(null, null, null, null);
        Order order = createOrder(1L, 100L, OrderStatus.PAID, 30L);

        when(findOrderPort.findAllWithFilters(null, null, null, null))
                .thenReturn(List.of(order));
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(30L)))
                .thenReturn(null);

        // when
        GetAdminOrdersResult result = getAdminOrdersService.getAdminOrders(query);

        // then
        assertThat(result.orders()).hasSize(1);
        assertThat(result.orders().get(0).orderProducts().get(0).productName()).isNull();
    }

    @Test
    @DisplayName("주문 상품이 없는 주문도 정상 반환한다")
    void shouldHandleOrderWithNoProducts() {
        // given
        GetAdminOrdersQuery query = GetAdminOrdersQuery.of(null, null, null, null);
        Order order = Order.from(OrderSnapshotState.builder()
                .id(1L)
                .buyerId(100L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD1")
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(100000L, 95000L, 3000L, 2000L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(List.of())
                .createdAt(LocalDateTime.of(2026, 2, 20, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 2, 20, 10, 0))
                .build());

        when(findOrderPort.findAllWithFilters(null, null, null, null))
                .thenReturn(List.of(order));

        // when
        GetAdminOrdersResult result = getAdminOrdersService.getAdminOrders(query);

        // then
        assertThat(result.orders()).hasSize(1);
        assertThat(result.orders().get(0).orderProducts()).isEmpty();
        verifyNoInteractions(findProductByPricePolicyPort);
    }

    @Test
    @DisplayName("endDate가 startDate보다 이전이면 InvalidOrderDateRangeException이 발생한다")
    void shouldThrowWhenEndDateBeforeStartDate() {
        // given
        LocalDateTime startDate = LocalDateTime.of(2026, 2, 28, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 2, 1, 0, 0);

        // when & then
        assertThatThrownBy(() -> GetAdminOrdersQuery.of(null, startDate, endDate, null))
                .isInstanceOf(InvalidOrderDateRangeException.class)
                .hasMessageContaining("종료일은 시작일 이후여야 합니다");
    }

    @Test
    @DisplayName("여러 주문의 pricePolicyId가 중복되면 distinct하여 조회한다")
    void shouldDeduplicatePricePolicyIds() {
        // given
        GetAdminOrdersQuery query = GetAdminOrdersQuery.of(null, null, null, null);
        Order order1 = createOrder(1L, 100L, OrderStatus.PAID, 30L);
        Order order2 = createOrder(2L, 101L, OrderStatus.PAID, 30L);

        when(findOrderPort.findAllWithFilters(null, null, null, null))
                .thenReturn(List.of(order1, order2));
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(30L)))
                .thenReturn(Map.of(30L, createProductInfo(50L, "테스트 상품", "테스트 브랜드")));

        // when
        GetAdminOrdersResult result = getAdminOrdersService.getAdminOrders(query);

        // then
        assertThat(result.orders()).hasSize(2);
        verify(findProductByPricePolicyPort).findByPricePolicyIds(List.of(30L));
    }
}
