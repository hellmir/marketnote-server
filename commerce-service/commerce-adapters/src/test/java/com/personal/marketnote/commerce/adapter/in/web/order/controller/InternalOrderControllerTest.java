package com.personal.marketnote.commerce.adapter.in.web.order.controller;

import com.personal.marketnote.commerce.adapter.in.web.order.response.InternalOrderProductResponse;
import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.domain.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalOrderController 테스트")
class InternalOrderControllerTest {

    @InjectMocks
    private InternalOrderController internalOrderController;

    @Mock
    private GetOrderUseCase getOrderUseCase;

    @Nested
    @DisplayName("verifyOrderOwnership")
    class VerifyOrderOwnership {

        @Test
        @DisplayName("주문 소유자와 요청자가 일치하면 200 OK를 반환한다")
        void shouldReturnOkWhenOwnerMatches() {
            // given
            Long orderId = 1L;
            Long buyerId = 200L;
            Order order = mock(Order.class);
            when(order.isBuyer(buyerId)).thenReturn(true);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            // when
            ResponseEntity<BaseResponse<Void>> response =
                    internalOrderController.verifyOrderOwnership(orderId, buyerId);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getOrderUseCase).getOrder(orderId);
            verify(order).isBuyer(buyerId);
        }

        @Test
        @DisplayName("주문 소유자와 요청자가 다르면 UnauthorizedOrderAccessException을 던진다")
        void shouldThrowExceptionWhenOwnerMismatch() {
            // given
            Long orderId = 1L;
            Long buyerId = 999L;
            Order order = mock(Order.class);
            when(order.isBuyer(buyerId)).thenReturn(false);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            // when & then
            assertThatThrownBy(() -> internalOrderController.verifyOrderOwnership(orderId, buyerId))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);
        }
    }

    @Nested
    @DisplayName("getOrderProduct")
    class GetOrderProduct {

        @Test
        @DisplayName("주문 상품을 조회하면 200 OK와 주문 상품 응답을 반환한다")
        void shouldReturnOrderProduct() {
            // given
            Long orderId = 1L;
            Long pricePolicyId = 10L;
            OrderProduct orderProduct = mock(OrderProduct.class);
            when(orderProduct.getUnitAmount()).thenReturn(Money.of(10_000L));
            when(getOrderUseCase.getOrderProduct(orderId, pricePolicyId)).thenReturn(orderProduct);

            // when
            ResponseEntity<BaseResponse<InternalOrderProductResponse>> response =
                    internalOrderController.getOrderProduct(orderId, pricePolicyId);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().unitAmount()).isEqualTo(10_000L);
            verify(getOrderUseCase).getOrderProduct(orderId, pricePolicyId);
        }
    }
}
