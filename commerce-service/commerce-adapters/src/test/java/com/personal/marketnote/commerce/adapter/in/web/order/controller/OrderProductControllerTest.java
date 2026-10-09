package com.personal.marketnote.commerce.adapter.in.web.order.controller;

import com.personal.marketnote.commerce.adapter.in.web.order.response.GetMyOrderProductsResponse;
import com.personal.marketnote.commerce.port.in.command.order.GetBuyerOrderProductsQuery;
import com.personal.marketnote.commerce.port.in.result.order.GetBuyerOrderProductsResult;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderProductController 테스트")
class OrderProductControllerTest {

    @InjectMocks
    private OrderProductController orderProductController;

    @Mock
    private GetOrderUseCase getOrderUseCase;

    private OAuth2AuthenticatedPrincipal buildPrincipal(Long userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                String.valueOf(userId), Map.of("name", String.valueOf(userId)), List.of()
        );
    }

    @Nested
    @DisplayName("getMyOrderProducts")
    class GetMyOrderProducts {

        @Test
        @DisplayName("나의 주문 상품 목록을 조회하면 200 OK와 목록을 반환한다")
        void shouldReturnOrderProducts() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);
            GetBuyerOrderProductsResult result = GetBuyerOrderProductsResult.of(List.of());
            when(getOrderUseCase.getBuyerOrderProducts(GetBuyerOrderProductsQuery.of(200L, null)))
                    .thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetMyOrderProductsResponse>> response =
                    orderProductController.getMyOrderProducts(principal, null);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().orderProducts()).isEmpty();
            verify(getOrderUseCase).getBuyerOrderProducts(GetBuyerOrderProductsQuery.of(200L, null));
        }

        @Test
        @DisplayName("isReviewed 파라미터로 필터링하여 조회하면 200 OK를 반환한다")
        void shouldFilterByIsReviewed() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);
            GetBuyerOrderProductsResult result = GetBuyerOrderProductsResult.of(List.of());
            when(getOrderUseCase.getBuyerOrderProducts(GetBuyerOrderProductsQuery.of(200L, Boolean.TRUE)))
                    .thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetMyOrderProductsResponse>> response =
                    orderProductController.getMyOrderProducts(principal, Boolean.TRUE);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<GetBuyerOrderProductsQuery> captor =
                    ArgumentCaptor.forClass(GetBuyerOrderProductsQuery.class);
            verify(getOrderUseCase).getBuyerOrderProducts(captor.capture());
            assertThat(captor.getValue().isReviewed()).isTrue();
            assertThat(captor.getValue().buyerId()).isEqualTo(200L);
        }
    }
}
