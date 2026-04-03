package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicySnapshotState;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.port.in.command.GetMyOrderingProductsQuery;
import com.personal.marketnote.product.port.in.command.OrderingItemQuery;
import com.personal.marketnote.product.port.in.result.cart.GetCartProductResult;
import com.personal.marketnote.product.port.in.result.product.GetMyOrderProductsResult;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.GetPricePoliciesUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductInventoryUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMyOrderingProductsUseCaseTest {
    @Mock
    private GetPricePoliciesUseCase getPricePoliciesUseCase;
    @Mock
    private GetProductInventoryUseCase getProductInventoryUseCase;

    @InjectMocks
    private GetMyOrderingProductsService getMyOrderingProductsService;

    @Test
    @DisplayName("주문 대기 상품 목록 조회 시 요청한 상품 정보를 반환한다")
    void getMyOrderingProducts_returnsOrderingProducts() {
        Product product1 = buildProduct(1L);
        Product product2 = buildProduct(2L);
        PricePolicy policy1 = buildPricePolicy(10L, product1);
        PricePolicy policy2 = buildPricePolicy(20L, product2);
        UUID sharerKey1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID sharerKey2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        OrderingItemQuery item1 = OrderingItemQuery.of(10L, sharerKey1, (short) 2, "url-1");
        OrderingItemQuery item2 = OrderingItemQuery.of(20L, sharerKey2, (short) 1, "url-2");
        GetMyOrderingProductsQuery query = GetMyOrderingProductsQuery.from(List.of(item1, item2));

        when(getPricePoliciesUseCase.getPricePoliciesAndOptions(List.of(10L, 20L)))
                .thenReturn(List.of(policy1, policy2));
        when(getProductInventoryUseCase.getProductStocks(List.of(10L, 20L)))
                .thenReturn(Map.of(10L, 5, 20L, 0));

        GetMyOrderProductsResult result = getMyOrderingProductsService.getMyOrderingProducts(query);

        assertThat(result.orderingProducts()).hasSize(2);
        GetCartProductResult first = result.orderingProducts().getFirst();
        assertThat(first.pricePolicy().id()).isEqualTo(10L);
        assertThat(first.quantity()).isEqualTo((short) 2);
        assertThat(first.stock()).isEqualTo(5);
        assertThat(first.sharerKey()).isEqualTo(sharerKey1);
        assertThat(first.product().id()).isEqualTo(1L);
        assertThat(first.product().imageUrl()).isEqualTo("url-1");

        GetCartProductResult second = result.orderingProducts().get(1);
        assertThat(second.pricePolicy().id()).isEqualTo(20L);
        assertThat(second.quantity()).isEqualTo((short) 1);
        assertThat(second.stock()).isEqualTo(0);
        assertThat(second.sharerKey()).isEqualTo(sharerKey2);
        assertThat(second.product().id()).isEqualTo(2L);
        assertThat(second.product().imageUrl()).isEqualTo("url-2");

        ArgumentCaptor<List<Long>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(getPricePoliciesUseCase).getPricePoliciesAndOptions(idsCaptor.capture());
        verify(getProductInventoryUseCase).getProductStocks(idsCaptor.capture());
        assertThat(idsCaptor.getAllValues()).contains(List.of(10L, 20L));
    }

    @Test
    @DisplayName("주문 대기 상품 목록 조회 시 가격 정책 정보가 없으면 빈 목록을 반환한다")
    void getMyOrderingProducts_emptyPricePolicies_returnsEmpty() {
        OrderingItemQuery item = OrderingItemQuery.of(10L, UUID.fromString("550e8400-e29b-41d4-a716-446655440001"), (short) 1, "url");
        GetMyOrderingProductsQuery query = GetMyOrderingProductsQuery.from(List.of(item));

        when(getPricePoliciesUseCase.getPricePoliciesAndOptions(List.of(10L)))
                .thenReturn(List.of());
        when(getProductInventoryUseCase.getProductStocks(List.of(10L)))
                .thenReturn(Map.of());

        GetMyOrderProductsResult result = getMyOrderingProductsService.getMyOrderingProducts(query);

        assertThat(result.orderingProducts()).isEmpty();
        verify(getPricePoliciesUseCase).getPricePoliciesAndOptions(List.of(10L));
        verify(getProductInventoryUseCase).getProductStocks(List.of(10L));
    }

    @Test
    @DisplayName("주문 대기 상품 목록 조회 시 일치하는 가격 정책이 없으면 해당 항목을 제외한다")
    void getMyOrderingProducts_missingPricePolicy_skipsItem() {
        Product product1 = buildProduct(1L);
        PricePolicy policy1 = buildPricePolicy(10L, product1);
        OrderingItemQuery item1 = OrderingItemQuery.of(10L, UUID.fromString("550e8400-e29b-41d4-a716-446655440001"), (short) 1, "url-1");
        OrderingItemQuery item2 = OrderingItemQuery.of(20L, UUID.fromString("550e8400-e29b-41d4-a716-446655440002"), (short) 2, "url-2");
        GetMyOrderingProductsQuery query = GetMyOrderingProductsQuery.from(List.of(item1, item2));

        when(getPricePoliciesUseCase.getPricePoliciesAndOptions(List.of(10L, 20L)))
                .thenReturn(List.of(policy1));
        when(getProductInventoryUseCase.getProductStocks(List.of(10L, 20L)))
                .thenReturn(Map.of(10L, 9));

        GetMyOrderProductsResult result = getMyOrderingProductsService.getMyOrderingProducts(query);

        assertThat(result.orderingProducts()).hasSize(1);
        assertThat(result.orderingProducts().getFirst().pricePolicy().id()).isEqualTo(10L);
    }

    @Test
    @DisplayName("주문 대기 상품 목록 조회 시 재고 정보가 없으면 재고 정보에 null을 반환한다")
    void getMyOrderingProducts_missingStock_returnsNullStock() {
        Product product = buildProduct(1L);
        PricePolicy policy = buildPricePolicy(10L, product);
        OrderingItemQuery item = OrderingItemQuery.of(10L, UUID.fromString("550e8400-e29b-41d4-a716-446655440001"), (short) 1, "url");
        GetMyOrderingProductsQuery query = GetMyOrderingProductsQuery.from(List.of(item));

        when(getPricePoliciesUseCase.getPricePoliciesAndOptions(List.of(10L)))
                .thenReturn(List.of(policy));
        when(getProductInventoryUseCase.getProductStocks(List.of(10L)))
                .thenReturn(Map.of());

        GetMyOrderProductsResult result = getMyOrderingProductsService.getMyOrderingProducts(query);

        assertThat(result.orderingProducts()).hasSize(1);
        assertThat(result.orderingProducts().getFirst().stock()).isNull();
    }

    @Test
    @DisplayName("주문 대기 상품 목록 조회 요청이 비어 있으면 빈 목록을 반환한다")
    void getMyOrderingProducts_emptyRequest_returnsEmpty() {
        GetMyOrderingProductsQuery query = GetMyOrderingProductsQuery.from(List.of());

        when(getPricePoliciesUseCase.getPricePoliciesAndOptions(List.of()))
                .thenReturn(List.of());
        when(getProductInventoryUseCase.getProductStocks(List.of()))
                .thenReturn(Map.of());

        GetMyOrderProductsResult result = getMyOrderingProductsService.getMyOrderingProducts(query);

        assertThat(result.orderingProducts()).isEmpty();
        verify(getPricePoliciesUseCase).getPricePoliciesAndOptions(List.of());
        verify(getProductInventoryUseCase).getProductStocks(List.of());
    }

    @Test
    @DisplayName("주문 대기 상품 목록 조회 중 가격 정책 조회가 실패하면 예외를 전파한다")
    void getMyOrderingProducts_pricePoliciesFail_propagates() {
        OrderingItemQuery item = OrderingItemQuery.of(10L, UUID.fromString("550e8400-e29b-41d4-a716-446655440001"), (short) 1, "url");
        GetMyOrderingProductsQuery query = GetMyOrderingProductsQuery.from(List.of(item));
        RuntimeException exception = new RuntimeException("price policy fail");

        when(getPricePoliciesUseCase.getPricePoliciesAndOptions(List.of(10L))).thenThrow(exception);

        assertThatThrownBy(() -> getMyOrderingProductsService.getMyOrderingProducts(query))
                .isSameAs(exception);

        verifyNoInteractions(getProductInventoryUseCase);
    }

    @Test
    @DisplayName("주문 대기 상품 목록 조회 중 재고 조회가 실패하면 예외를 전파한다")
    void getMyOrderingProducts_inventoryFail_propagates() {
        Product product = buildProduct(1L);
        PricePolicy policy = buildPricePolicy(10L, product);
        OrderingItemQuery item = OrderingItemQuery.of(10L, UUID.fromString("550e8400-e29b-41d4-a716-446655440001"), (short) 1, "url");
        GetMyOrderingProductsQuery query = GetMyOrderingProductsQuery.from(List.of(item));
        RuntimeException exception = new RuntimeException("inventory fail");

        when(getPricePoliciesUseCase.getPricePoliciesAndOptions(List.of(10L)))
                .thenReturn(List.of(policy));
        when(getProductInventoryUseCase.getProductStocks(List.of(10L)))
                .thenThrow(exception);

        assertThatThrownBy(() -> getMyOrderingProductsService.getMyOrderingProducts(query))
                .isSameAs(exception);
    }

    private Product buildProduct(Long id) {
        return Product.from(
                ProductSnapshotState.builder()
                        .id(id)
                        .sellerId(1L)
                        .name("상품-" + id)
                        .brandName("브랜드-" + id)
                        .detail("설명-" + id)
                        .findAllOptionsYn(false)
                        .productTags(List.of())
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private PricePolicy buildPricePolicy(Long id, Product product) {
        return PricePolicy.from(
                PricePolicySnapshotState.builder()
                        .id(id)
                        .product(product)
                        .price(10000L)
                        .discountPrice(9000L)
                        .discountRate(BigDecimal.valueOf(10))
                        .accumulatedPoint(100L)
                        .accumulationRate(BigDecimal.valueOf(1))
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
