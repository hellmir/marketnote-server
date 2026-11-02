package com.personal.marketnote.product.adapter.in.web.product.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.product.adapter.in.web.cart.request.GetMyOrderingProductsRequest;
import com.personal.marketnote.product.adapter.in.web.cart.request.OrderingItemRequest;
import com.personal.marketnote.product.adapter.in.web.cart.response.GetMyOrderingProductsResponse;
import com.personal.marketnote.product.port.in.command.GetMyOrderingProductsQuery;
import com.personal.marketnote.product.port.in.result.product.GetMyOrderProductsResult;
import com.personal.marketnote.product.port.in.usecase.product.GetMyOrderingProductsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductOrderController 테스트")
class ProductOrderControllerTest {

    @InjectMocks
    private ProductOrderController productOrderController;

    @Mock
    private GetMyOrderingProductsUseCase getMyOrderingProductsUseCase;

    @Nested
    @DisplayName("POST /api/v1/products/ordering")
    class GetMyOrderingProducts {

        @Test
        @DisplayName("주문 대기 상품 목록 조회 시 200 OK를 반환한다")
        void returnsOkWithOrderingProducts() {
            GetMyOrderingProductsRequest request = new GetMyOrderingProductsRequest(
                    List.of(new OrderingItemRequest(1L, null, (short) 2, "http://img"))
            );
            GetMyOrderProductsResult result = GetMyOrderProductsResult.from(List.of());
            when(getMyOrderingProductsUseCase.getMyOrderingProducts(any(GetMyOrderingProductsQuery.class)))
                    .thenReturn(result);

            ResponseEntity<BaseResponse<GetMyOrderingProductsResponse>> response =
                    productOrderController.getMyOrderingProducts(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).isNotNull();
            verify(getMyOrderingProductsUseCase).getMyOrderingProducts(any(GetMyOrderingProductsQuery.class));
        }

        @Test
        @DisplayName("빈 주문 대기 상품 목록 조회 시 200 OK와 빈 결과를 반환한다")
        void returnsOkWithEmptyList() {
            GetMyOrderingProductsRequest request = new GetMyOrderingProductsRequest(List.of());
            GetMyOrderProductsResult result = GetMyOrderProductsResult.from(List.of());
            when(getMyOrderingProductsUseCase.getMyOrderingProducts(any(GetMyOrderingProductsQuery.class)))
                    .thenReturn(result);

            ResponseEntity<BaseResponse<GetMyOrderingProductsResponse>> response =
                    productOrderController.getMyOrderingProducts(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
