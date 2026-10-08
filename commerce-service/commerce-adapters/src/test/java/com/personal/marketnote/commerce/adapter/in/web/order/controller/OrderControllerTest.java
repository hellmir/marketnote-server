package com.personal.marketnote.commerce.adapter.in.web.order.controller;

import com.personal.marketnote.commerce.adapter.in.web.order.request.CancelOrderRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.request.ChangeOrderStatusRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.request.RegisterOrderRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.request.RejectReturnRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.request.RequestReturnRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.response.GetAdminOrdersResponse;
import com.personal.marketnote.commerce.adapter.in.web.order.response.GetMyOrdersResponse;
import com.personal.marketnote.commerce.adapter.in.web.order.response.GetOrderCountResponse;
import com.personal.marketnote.commerce.adapter.in.web.order.response.GetOrderKeyResponse;
import com.personal.marketnote.commerce.adapter.in.web.order.response.GetOrderProductKeyResponse;
import com.personal.marketnote.commerce.adapter.in.web.order.response.GetOrderResponse;
import com.personal.marketnote.commerce.adapter.in.web.order.response.GetOrderStatusHistoryResponse;
import com.personal.marketnote.commerce.adapter.in.web.order.response.GetReturnRefundInfoResponse;
import com.personal.marketnote.commerce.adapter.in.web.order.response.RegisterOrderResponse;
import com.personal.marketnote.commerce.domain.order.OrderPeriod;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusFilter;
import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import com.personal.marketnote.commerce.port.in.command.order.CancelOrderCommand;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.command.order.ConfirmOrderCommand;
import com.personal.marketnote.commerce.port.in.command.order.GetBuyerOrderHistoryQuery;
import com.personal.marketnote.commerce.port.in.command.order.GetReturnRefundInfoCommand;
import com.personal.marketnote.commerce.port.in.command.order.RegisterOrderCommand;
import com.personal.marketnote.commerce.port.in.command.order.RejectReturnCommand;
import com.personal.marketnote.commerce.port.in.command.order.RequestReturnCommand;
import com.personal.marketnote.commerce.port.in.command.order.UpdateOrderProductReviewStatusCommand;
import com.personal.marketnote.commerce.port.in.result.order.GetAdminOrdersResult;
import com.personal.marketnote.commerce.port.in.result.order.GetBuyerOrdersResult;
import com.personal.marketnote.commerce.port.in.result.order.GetOrderCountResult;
import com.personal.marketnote.commerce.port.in.result.order.GetOrderKeyResult;
import com.personal.marketnote.commerce.port.in.result.order.GetOrderProductKeyResult;
import com.personal.marketnote.commerce.port.in.result.order.GetOrderResult;
import com.personal.marketnote.commerce.port.in.result.order.GetOrderStatusHistoryResult;
import com.personal.marketnote.commerce.port.in.result.order.GetReturnRefundInfoResult;
import com.personal.marketnote.commerce.port.in.result.order.RegisterOrderResult;
import com.personal.marketnote.commerce.port.in.usecase.order.CancelOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.ConfirmOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetAdminOrdersUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderStatusHistoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetReturnRefundInfoUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.RegisterOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.RejectReturnUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.RequestReturnUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.UpdateOrderProductUseCase;
import com.personal.marketnote.commerce.port.out.user.UpdateUserShippingAddressDeliveryRequestPort;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController 테스트")
class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private RegisterOrderUseCase registerOrderUseCase;

    @Mock
    private UpdateUserShippingAddressDeliveryRequestPort updateUserShippingAddressDeliveryRequestPort;

    @Mock
    private GetOrderUseCase getOrderUseCase;

    @Mock
    private CancelOrderUseCase cancelOrderUseCase;

    @Mock
    private RequestReturnUseCase requestReturnUseCase;

    @Mock
    private ConfirmOrderUseCase confirmOrderUseCase;

    @Mock
    private ChangeOrderStatusUseCase changeOrderStatusUseCase;

    @Mock
    private UpdateOrderProductUseCase updateOrderProductUseCase;

    @Mock
    private GetAdminOrdersUseCase getAdminOrdersUseCase;

    @Mock
    private GetOrderStatusHistoryUseCase getOrderStatusHistoryUseCase;

    @Mock
    private RejectReturnUseCase rejectReturnUseCase;

    @Mock
    private GetReturnRefundInfoUseCase getReturnRefundInfoUseCase;

    private OAuth2AuthenticatedPrincipal buildPrincipal(Long userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                String.valueOf(userId), Map.of("name", String.valueOf(userId)), List.of()
        );
    }

    @Nested
    @DisplayName("registerOrder")
    class RegisterOrder {

        @Test
        @DisplayName("주문 등록이 성공하면 201 CREATED와 주문 등록 응답을 반환한다")
        void shouldReturnCreatedWhenRegisterOrderSucceeds() {
            // given
            RegisterOrderRequest request = mock(RegisterOrderRequest.class);
            when(request.getTotalAmount()).thenReturn(10_000L);
            when(request.getCouponAmount()).thenReturn(0L);
            when(request.getPointAmount()).thenReturn(0L);
            when(request.getShippingFee()).thenReturn(3_000L);
            when(request.getShippingAddressId()).thenReturn(100L);
            when(request.getDeliveryRequestType()).thenReturn(null);
            when(request.getDeliveryRequestMessage()).thenReturn(null);
            when(request.getOrderProducts()).thenReturn(List.of());

            RegisterOrderResult result = new RegisterOrderResult(1L, "ORDER_KEY_1");
            when(registerOrderUseCase.registerOrder(any(RegisterOrderCommand.class))).thenReturn(result);

            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);

            // when
            ResponseEntity<BaseResponse<RegisterOrderResponse>> response =
                    orderController.registerOrder(request, principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().id()).isEqualTo(1L);
            assertThat(response.getBody().getContent().orderKey()).isEqualTo("ORDER_KEY_1");
            verify(registerOrderUseCase).registerOrder(any(RegisterOrderCommand.class));
            verify(updateUserShippingAddressDeliveryRequestPort, never())
                    .updateDeliveryRequest(any(), any(), any(), any());
        }

        @Test
        @DisplayName("배송 요청사항 타입이 있으면 배송 요청사항을 업데이트한다")
        void shouldUpdateDeliveryRequestWhenTypeProvided() {
            // given
            RegisterOrderRequest request = mock(RegisterOrderRequest.class);
            when(request.getTotalAmount()).thenReturn(10_000L);
            when(request.getCouponAmount()).thenReturn(0L);
            when(request.getPointAmount()).thenReturn(0L);
            when(request.getShippingFee()).thenReturn(3_000L);
            when(request.getShippingAddressId()).thenReturn(100L);
            when(request.getDeliveryRequestType()).thenReturn(DeliveryRequestType.LEAVE_AT_DOOR);
            when(request.getDeliveryRequestMessage()).thenReturn("문앞에 놓아주세요");
            when(request.getOrderProducts()).thenReturn(List.of());

            when(registerOrderUseCase.registerOrder(any(RegisterOrderCommand.class)))
                    .thenReturn(new RegisterOrderResult(1L, "ORDER_KEY_1"));

            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);

            // when
            orderController.registerOrder(request, principal);

            // then
            verify(updateUserShippingAddressDeliveryRequestPort)
                    .updateDeliveryRequest(
                            100L,
                            200L,
                            DeliveryRequestType.LEAVE_AT_DOOR,
                            "문앞에 놓아주세요"
                    );
        }
    }

    @Nested
    @DisplayName("getOrderKey")
    class GetOrderKey {

        @Test
        @DisplayName("주문 ID로 주문 키를 조회하면 200 OK와 주문 키를 반환한다")
        void shouldReturnOrderKeyWhenGetOrderKeySucceeds() {
            // given
            Long orderId = 1L;
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);
            when(getOrderUseCase.getOrderKey(orderId, 200L))
                    .thenReturn(new GetOrderKeyResult("ORDER_KEY_1"));

            // when
            ResponseEntity<BaseResponse<GetOrderKeyResponse>> response =
                    orderController.getOrderKey(orderId, principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().orderKey()).isEqualTo("ORDER_KEY_1");
            verify(getOrderUseCase).getOrderKey(orderId, 200L);
        }
    }

    @Nested
    @DisplayName("getOrderProductKey")
    class GetOrderProductKey {

        @Test
        @DisplayName("주문 ID와 가격 정책 ID로 주문 상품 키를 조회하면 200 OK를 반환한다")
        void shouldReturnOrderProductKey() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);
            when(getOrderUseCase.getOrderProductKey(1L, 10L, 200L))
                    .thenReturn(new GetOrderProductKeyResult("ORDER_PRODUCT_KEY_1"));

            // when
            ResponseEntity<BaseResponse<GetOrderProductKeyResponse>> response =
                    orderController.getOrderProductKey(1L, 10L, principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().orderProductKey()).isEqualTo("ORDER_PRODUCT_KEY_1");
            verify(getOrderUseCase).getOrderProductKey(1L, 10L, 200L);
        }
    }

    @Nested
    @DisplayName("getOrder")
    class GetOrder {

        @Test
        @DisplayName("주문 정보를 조회하면 200 OK와 주문 정보를 반환한다")
        void shouldReturnOrderInfo() {
            // given
            Long orderId = 1L;
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);
            GetOrderResult result = mock(GetOrderResult.class);
            when(getOrderUseCase.getOrderAndOrderProducts(orderId, 200L)).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetOrderResponse>> response =
                    orderController.getOrder(orderId, principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().orderInfo()).isEqualTo(result);
            verify(getOrderUseCase).getOrderAndOrderProducts(orderId, 200L);
        }
    }

    @Nested
    @DisplayName("getMyOrderHistory")
    class GetMyOrderHistory {

        @Test
        @DisplayName("나의 주문 내역을 조회하면 200 OK를 반환한다")
        void shouldReturnMyOrderHistory() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);
            when(getOrderUseCase.getBuyerOrderHistory(any(GetBuyerOrderHistoryQuery.class)))
                    .thenReturn(GetBuyerOrdersResult.of(List.of(), Map.of()));

            // when
            ResponseEntity<BaseResponse<GetMyOrdersResponse>> response = orderController.getMyOrderHistory(
                    principal,
                    OrderPeriod.ONE_MONTH,
                    OrderStatusFilter.ALL,
                    null
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().orderHistory()).isEmpty();
            ArgumentCaptor<GetBuyerOrderHistoryQuery> captor = ArgumentCaptor.forClass(GetBuyerOrderHistoryQuery.class);
            verify(getOrderUseCase).getBuyerOrderHistory(captor.capture());
            assertThat(captor.getValue().buyerId()).isEqualTo(200L);
            assertThat(captor.getValue().period()).isEqualTo(OrderPeriod.ONE_MONTH);
            assertThat(captor.getValue().status()).isEqualTo(OrderStatusFilter.ALL);
        }
    }

    @Nested
    @DisplayName("getMyOrderCount")
    class GetMyOrderCount {

        @Test
        @DisplayName("나의 주문 내역 개수를 조회하면 200 OK와 개수를 반환한다")
        void shouldReturnMyOrderCount() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);
            when(getOrderUseCase.getBuyerOrderCount(any(GetBuyerOrderHistoryQuery.class)))
                    .thenReturn(GetOrderCountResult.of(5L));

            // when
            ResponseEntity<BaseResponse<GetOrderCountResponse>> response = orderController.getMyOrderCount(
                    principal,
                    OrderPeriod.ALL,
                    OrderStatusFilter.ALL,
                    "상품명"
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().totalCount()).isEqualTo(5L);
            verify(getOrderUseCase).getBuyerOrderCount(any(GetBuyerOrderHistoryQuery.class));
        }
    }

    @Nested
    @DisplayName("cancelOrder")
    class CancelOrder {

        @Test
        @DisplayName("주문 취소 요청이 성공하면 200 OK를 반환한다")
        void shouldReturnOkWhenCancelOrderSucceeds() {
            // given
            Long orderId = 1L;
            CancelOrderRequest request = mock(CancelOrderRequest.class);
            when(request.getReasonCategory()).thenReturn(OrderStatusReasonCategory.CANCEL_ORDER);
            when(request.getReason()).thenReturn("단순 변심");
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);

            // when
            ResponseEntity<BaseResponse<Void>> response =
                    orderController.cancelOrder(orderId, request, principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(cancelOrderUseCase).cancelOrder(any(CancelOrderCommand.class));
        }
    }

    @Nested
    @DisplayName("requestReturn")
    class RequestReturn {

        @Test
        @DisplayName("반품 요청이 성공하면 200 OK를 반환한다")
        void shouldReturnOkWhenRequestReturnSucceeds() {
            // given
            Long orderId = 1L;
            RequestReturnRequest request = mock(RequestReturnRequest.class);
            when(request.getReasonCategory()).thenReturn(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND);
            when(request.getReason()).thenReturn("사이즈 교환");
            when(request.getPickupRecipientName()).thenReturn("홍길동");
            when(request.getPickupRecipientPhoneNumber()).thenReturn("010-1234-5678");
            when(request.getPickupZipCode()).thenReturn("12345");
            when(request.getPickupAddress()).thenReturn("서울시 강남구");
            when(request.getPickupAddressDetail()).thenReturn("101호");
            when(request.getPickupRequestMessage()).thenReturn("문앞에 놓아주세요");
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);

            // when
            ResponseEntity<BaseResponse<Void>> response =
                    orderController.requestReturn(orderId, request, principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(requestReturnUseCase).requestReturn(any(RequestReturnCommand.class));
        }
    }

    @Nested
    @DisplayName("rejectReturn")
    class RejectReturn {

        @Test
        @DisplayName("반품 불가 판정이 성공하면 200 OK를 반환한다")
        void shouldReturnOkWhenRejectReturnSucceeds() {
            // given
            Long orderId = 1L;
            RejectReturnRequest request = mock(RejectReturnRequest.class);
            when(request.getReasonCategory()).thenReturn(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND);
            when(request.getReason()).thenReturn("반품 기한 경과");

            // when
            ResponseEntity<BaseResponse<Void>> response =
                    orderController.rejectReturn(orderId, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(rejectReturnUseCase).rejectReturn(any(RejectReturnCommand.class));
        }
    }

    @Nested
    @DisplayName("getReturnRefundInfo")
    class GetReturnRefundInfo {

        @Test
        @DisplayName("반품 환불 예정 정보 조회가 성공하면 200 OK와 환불 예정 정보를 반환한다")
        void shouldReturnRefundInfo() {
            // given
            Long orderId = 1L;
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);
            GetReturnRefundInfoResult result = GetReturnRefundInfoResult.builder()
                    .totalProductAmount(10_000L)
                    .returnShippingFee(3_000L)
                    .refundMethod("CARD")
                    .estimatedRefundAmount(7_000L)
                    .estimatedRefundCash(7_000L)
                    .build();
            when(getReturnRefundInfoUseCase.getReturnRefundInfo(any(GetReturnRefundInfoCommand.class)))
                    .thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetReturnRefundInfoResponse>> response =
                    orderController.getReturnRefundInfo(
                            orderId,
                            OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND,
                            List.of(1L, 2L),
                            principal
                    );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().totalProductAmount()).isEqualTo(10_000L);
            assertThat(response.getBody().getContent().returnShippingFee()).isEqualTo(3_000L);
            assertThat(response.getBody().getContent().refundMethod()).isEqualTo("CARD");
            assertThat(response.getBody().getContent().estimatedRefundAmount()).isEqualTo(7_000L);
        }
    }

    @Nested
    @DisplayName("confirmOrder")
    class ConfirmOrder {

        @Test
        @DisplayName("구매 확정 요청이 성공하면 200 OK를 반환한다")
        void shouldReturnOkWhenConfirmOrderSucceeds() {
            // given
            Long orderId = 1L;
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);

            // when
            ResponseEntity<BaseResponse<Void>> response =
                    orderController.confirmOrder(orderId, principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<ConfirmOrderCommand> captor = ArgumentCaptor.forClass(ConfirmOrderCommand.class);
            verify(confirmOrderUseCase).confirmOrder(captor.capture());
            assertThat(captor.getValue().id()).isEqualTo(1L);
            assertThat(captor.getValue().buyerId()).isEqualTo(200L);
        }
    }

    @Nested
    @DisplayName("changeOrderStatus")
    class ChangeOrderStatus {

        @Test
        @DisplayName("주문 상태 변경이 성공하면 200 OK를 반환한다")
        void shouldReturnOkWhenChangeOrderStatusSucceeds() {
            // given
            Long orderId = 1L;
            ChangeOrderStatusRequest request = mock(ChangeOrderStatusRequest.class);
            when(request.getOrderStatus()).thenReturn(OrderStatus.SHIPPING);
            when(request.getPricePolicyIds()).thenReturn(List.of(10L));
            when(request.getReasonCategory()).thenReturn(null);
            when(request.getReason()).thenReturn(null);

            // when
            ResponseEntity<BaseResponse<Void>> response =
                    orderController.changeOrderStatus(orderId, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));
        }
    }

    @Nested
    @DisplayName("updateOrderProductReviewStatus")
    class UpdateOrderProductReviewStatus {

        @Test
        @DisplayName("리뷰 작성 여부 업데이트가 성공하면 200 OK를 반환한다")
        void shouldReturnOkWhenUpdateReviewStatusSucceeds() {
            // when
            ResponseEntity<BaseResponse<Void>> response =
                    orderController.updateOrderProductReviewStatus(1L, 10L, true);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<UpdateOrderProductReviewStatusCommand> captor =
                    ArgumentCaptor.forClass(UpdateOrderProductReviewStatusCommand.class);
            verify(updateOrderProductUseCase).updateReviewStatus(captor.capture());
            assertThat(captor.getValue().orderId()).isEqualTo(1L);
            assertThat(captor.getValue().pricePolicyId()).isEqualTo(10L);
            assertThat(captor.getValue().isReviewed()).isTrue();
        }
    }

    @Nested
    @DisplayName("getAdminOrders")
    class GetAdminOrders {

        @Test
        @DisplayName("관리자 주문 내역을 조회하면 200 OK를 반환한다")
        void shouldReturnAdminOrders() {
            // given
            when(getAdminOrdersUseCase.getAdminOrders(any())).thenReturn(GetAdminOrdersResult.empty());

            // when
            ResponseEntity<BaseResponse<GetAdminOrdersResponse>> response = orderController.getAdminOrders(
                    null,
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 31, 23, 59),
                    OrderStatus.PAID
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().orders()).isEmpty();
            verify(getAdminOrdersUseCase).getAdminOrders(any());
        }
    }

    @Nested
    @DisplayName("getOrderStatusHistory")
    class GetOrderStatusHistoryTest {

        @Test
        @DisplayName("주문 상태 이력을 조회하면 200 OK와 이력 목록을 반환한다")
        void shouldReturnOrderStatusHistory() {
            // given
            Long orderId = 1L;
            when(getOrderStatusHistoryUseCase.getOrderStatusHistory(orderId))
                    .thenReturn(new GetOrderStatusHistoryResult(orderId, List.of()));

            // when
            ResponseEntity<BaseResponse<GetOrderStatusHistoryResponse>> response =
                    orderController.getOrderStatusHistory(orderId);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().orderId()).isEqualTo(orderId);
            assertThat(response.getBody().getContent().statusHistory()).isEmpty();
            verify(getOrderStatusHistoryUseCase).getOrderStatusHistory(orderId);
            verifyNoInteractions(getAdminOrdersUseCase);
        }
    }
}
