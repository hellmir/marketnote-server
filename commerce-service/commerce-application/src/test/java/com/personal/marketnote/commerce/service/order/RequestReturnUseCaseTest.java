package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.exception.InvalidReasonCategoryException;
import com.personal.marketnote.commerce.exception.InvalidReturnTargetPricePolicyException;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.command.order.RequestReturnCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.service.returntracker.CreateReturnTrackerAfterReturnRequestService;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestReturnUseCaseTest {
    @Mock
    private GetOrderUseCase getOrderUseCase;
    @Mock
    private UpdateOrderPort updateOrderPort;
    @Mock
    private PublishOrderEventPort publishOrderEventPort;
    @Mock
    private CreateReturnTrackerAfterReturnRequestService createReturnTrackerAfterReturnRequestService;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-04-05T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private RequestReturnService requestReturnService;

    // ==================================================================================
    // 정상 반품 요청
    // ==================================================================================

    @Nested
    @DisplayName("정상 반품 요청")
    class SuccessfulReturnRequestTest {

        @Test
        @DisplayName("배송 완료 상태의 주문을 반품 요청하면 정상 처리된다")
        void requestReturn_fromDelivered_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .reason("상품 불량")
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> requestReturnService.requestReturn(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("배송중 상태의 주문을 반품 요청하면 정상 처리된다")
        void requestReturn_fromShipping_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.SHIPPING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .reason("상품 불량")
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> requestReturnService.requestReturn(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("부분 구매 확정 상태의 주문을 반품 요청하면 정상 처리된다")
        void requestReturn_fromPartiallyConfirmed_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PARTIALLY_CONFIRMED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> requestReturnService.requestReturn(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("부분 반품됨 상태의 주문을 반품 요청하면 정상 처리된다")
        void requestReturn_fromPartiallyReturned_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PARTIALLY_RETURNED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> requestReturnService.requestReturn(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("반품 요청 성공 시 UpdateOrderPort를 호출한다")
        void requestReturn_callsUpdateOrderPort() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .reason("상품 불량")
                    .buyerId(buyerId)
                    .build();

            requestReturnService.requestReturn(command);

            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
        }

        @Test
        @DisplayName("반품 요청 성공 시 ReturnRequestedEvent를 발행한다")
        void requestReturn_publishesReturnRequestedEvent() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .reason("상품 불량")
                    .buyerId(buyerId)
                    .build();

            requestReturnService.requestReturn(command);

            verify(publishOrderEventPort).publishReturnRequestedEvent(orderId, buyerId);
        }

        @Test
        @DisplayName("회수지 주소를 입력하면 입력된 회수지가 적용된다")
        void requestReturn_withPickupAddress_appliesProvidedAddress() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .reason("상품 불량")
                    .buyerId(buyerId)
                    .pickupRecipientName("회수 수령인")
                    .pickupRecipientPhoneNumber("010-9999-8888")
                    .pickupZipCode("54321")
                    .pickupAddress("회수지 주소")
                    .pickupAddressDetail("회수지 상세주소")
                    .pickupRequestMessage("부재시 경비실에 맡겨주세요")
                    .build();

            requestReturnService.requestReturn(command);

            assertThat(order.getPickupAddress().getRecipientName().getValue()).isEqualTo("회수 수령인");
            assertThat(order.getPickupAddress().getRecipientPhoneNumber().getValue()).isEqualTo("010-9999-8888");
            assertThat(order.getPickupAddress().getZipCode()).isEqualTo("54321");
            assertThat(order.getPickupAddress().getAddress()).isEqualTo("회수지 주소");
            assertThat(order.getPickupAddress().getAddressDetail()).isEqualTo("회수지 상세주소");
            assertThat(order.getPickupAddress().getDeliveryRequestMessage().getValue()).isEqualTo("부재시 경비실에 맡겨주세요");
        }

        @Test
        @DisplayName("회수지 주소를 미입력하면 배송지가 회수지 기본값으로 적용된다")
        void requestReturn_withoutPickupAddress_appliesShippingAddressAsDefault() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .buyerId(buyerId)
                    .build();

            requestReturnService.requestReturn(command);

            assertThat(order.getPickupAddress().getRecipientName().getValue()).isEqualTo("수령인");
            assertThat(order.getPickupAddress().getRecipientPhoneNumber().getValue()).isEqualTo("010-1234-5678");
            assertThat(order.getPickupAddress().getZipCode()).isEqualTo("12345");
            assertThat(order.getPickupAddress().getAddress()).isEqualTo("서울시 강남구");
            assertThat(order.getPickupAddress().getAddressDetail()).isEqualTo("상세주소");
        }
    }

    // ==================================================================================
    // 반품 사유 카테고리 검증
    // ==================================================================================

    @Nested
    @DisplayName("반품 사유 카테고리 검증")
    class ReturnReasonCategoryValidationTest {

        @Test
        @DisplayName("반품 전용 사유로 반품 요청하면 정상 처리된다")
        void requestReturn_withReturnReason_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND)
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> requestReturnService.requestReturn(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("공용 사유(ETC)로 반품 요청하면 정상 처리된다")
        void requestReturn_withBothReason_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .reason("기타 사유")
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> requestReturnService.requestReturn(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("취소 전용 사유로 반품 요청하면 InvalidReasonCategoryException이 발생한다")
        void requestReturn_withCancelOnlyReason_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(InvalidReasonCategoryException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(publishOrderEventPort);
        }

        @Test
        @DisplayName("사유 카테고리가 null이면 검증을 건너뛴다")
        void requestReturn_withNullReasonCategory_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> requestReturnService.requestReturn(command))
                    .doesNotThrowAnyException();
        }
    }

    // ==================================================================================
    // 구매자 소유권 검증
    // ==================================================================================

    @Nested
    @DisplayName("구매자 소유권 검증")
    class BuyerOwnershipValidationTest {

        @Test
        @DisplayName("타인의 주문을 반품 요청하면 UnauthorizedOrderAccessException이 발생한다")
        void requestReturn_otherBuyerOrder_throwsException() {
            Long orderId = 1L;
            Long ownerBuyerId = 100L;
            Long attackerBuyerId = 999L;
            Order order = createOrderWithBuyerId(orderId, ownerBuyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(attackerBuyerId)
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);
        }

        @Test
        @DisplayName("타인의 주문 반품 요청 시 UpdateOrderPort를 호출하지 않는다")
        void requestReturn_otherBuyerOrder_doesNotCallUpdatePort() {
            Long orderId = 1L;
            Long ownerBuyerId = 100L;
            Long attackerBuyerId = 999L;
            Order order = createOrderWithBuyerId(orderId, ownerBuyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(attackerBuyerId)
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(publishOrderEventPort);
        }
    }

    // ==================================================================================
    // 상태 전이 검증
    // ==================================================================================

    @Nested
    @DisplayName("상태 전이 검증")
    class StatusTransitionValidationTest {

        @Test
        @DisplayName("이미 반품 요청된 주문을 다시 반품 요청하면 OrderStatusAlreadyChangedException이 발생한다")
        void requestReturn_alreadyReturnRequested_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.RETURN_REQUESTED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(OrderStatusAlreadyChangedException.class);
        }

        @Test
        @DisplayName("결제 대기 상태의 주문을 반품 요청하면 InvalidOrderStatusTransitionException이 발생한다")
        void requestReturn_fromPaymentPending_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("상품 준비중 상태의 주문을 반품 요청하면 InvalidOrderStatusTransitionException이 발생한다")
        void requestReturn_fromPreparing_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PREPARING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("구매 확정 상태의 주문을 반품 요청하면 InvalidOrderStatusTransitionException이 발생한다")
        void requestReturn_fromConfirmed_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.CONFIRMED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("상태 전이 실패 시 UpdateOrderPort를 호출하지 않는다")
        void requestReturn_invalidTransition_doesNotCallUpdatePort() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PREPARING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(publishOrderEventPort);
        }
    }

    // ==================================================================================
    // 부분 반품 요청
    // ==================================================================================

    @Nested
    @DisplayName("부분 반품 요청")
    class PartialReturnRequestTest {

        @Test
        @DisplayName("pricePolicyIds가 null이면 전체 반품(changeAllProductsStatus)으로 처리된다")
        void requestReturn_withNullPricePolicyIds_callsChangeAllProductsStatus() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithTwoProducts(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .pricePolicyIds(null)
                    .build();

            requestReturnService.requestReturn(command);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.RETURN_REQUESTED);
            order.getOrderProducts().forEach(product ->
                    assertThat(product.getOrderStatus()).isEqualTo(OrderStatus.RETURN_REQUESTED)
            );
        }

        @Test
        @DisplayName("pricePolicyIds가 빈 리스트면 전체 반품으로 처리된다")
        void requestReturn_withEmptyPricePolicyIds_callsChangeAllProductsStatus() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithTwoProducts(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .pricePolicyIds(List.of())
                    .build();

            requestReturnService.requestReturn(command);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.RETURN_REQUESTED);
        }

        @Test
        @DisplayName("pricePolicyIds에 일부만 전달하면 Order 상태가 PARTIALLY_RETURN_REQUESTED로 전이된다")
        void requestReturn_withPartialPricePolicyIds_transitionsToPartiallyReturnRequested() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithTwoProducts(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .pricePolicyIds(List.of(100L))
                    .build();

            requestReturnService.requestReturn(command);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PARTIALLY_RETURN_REQUESTED);
            assertThat(order.getOrderProducts().get(0).getOrderStatus()).isEqualTo(OrderStatus.RETURN_REQUESTED);
            assertThat(order.getOrderProducts().get(1).getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
        }

        @Test
        @DisplayName("pricePolicyIds에 주문에 없는 ID가 포함되면 InvalidReturnTargetPricePolicyException이 발생한다")
        void requestReturn_withInvalidPricePolicyId_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithTwoProducts(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .pricePolicyIds(List.of(100L, 999L))
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(InvalidReturnTargetPricePolicyException.class);
        }

        @Test
        @DisplayName("부분 반품 요청된 품목만 Fassto products로 전달된다")
        void requestReturn_partialReturn_fassto_productsFiltered() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithTwoProducts(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .pricePolicyIds(List.of(200L))
                    .build();

            requestReturnService.requestReturn(command);

            ArgumentCaptor<RegisterFulfillmentReturnDeliveryCommand> captor =
                    ArgumentCaptor.forClass(RegisterFulfillmentReturnDeliveryCommand.class);
            verify(createReturnTrackerAfterReturnRequestService).createReturnTracker(captor.capture());

            RegisterFulfillmentReturnDeliveryCommand deliveryCommand = captor.getValue();
            assertThat(deliveryCommand.products()).hasSize(1);
            assertThat(deliveryCommand.products().get(0).quantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("Fassto productCode는 OrderProduct.productKey(UUID 문자열)로 매핑된다")
        void requestReturn_fassto_productCodeIsProductKey() {
            Long orderId = 1L;
            Long buyerId = 100L;
            UUID productKey100 = UUID.fromString("00000000-0000-0000-0000-000000000100");
            UUID productKey200 = UUID.fromString("00000000-0000-0000-0000-000000000200");
            Order order = createOrderWithProductKeys(orderId, buyerId, productKey100, productKey200);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            requestReturnService.requestReturn(command);

            ArgumentCaptor<RegisterFulfillmentReturnDeliveryCommand> captor =
                    ArgumentCaptor.forClass(RegisterFulfillmentReturnDeliveryCommand.class);
            verify(createReturnTrackerAfterReturnRequestService).createReturnTracker(captor.capture());

            RegisterFulfillmentReturnDeliveryCommand deliveryCommand = captor.getValue();
            assertThat(deliveryCommand.products()).hasSize(2);
            assertThat(deliveryCommand.products().get(0).productCode()).isEqualTo(productKey100.toString());
            assertThat(deliveryCommand.products().get(1).productCode()).isEqualTo(productKey200.toString());
        }

        @Test
        @DisplayName("이미 모든 요청 품목이 RETURN_REQUESTED 상태면 OrderStatusAlreadyChangedException이 발생한다")
        void requestReturn_partialReturn_alreadyRequested_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMixedProductStatuses(orderId, buyerId);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .pricePolicyIds(List.of(100L))
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(OrderStatusAlreadyChangedException.class);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private Order createOrderWithTwoProducts(Long orderId, Long buyerId, OrderStatus status) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .productKey(UUID.randomUUID())
                        .sellerId(10L)
                        .pricePolicyId(100L)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(status)
                        .accumulatedPoint(0L)
                        .build(),
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .productKey(UUID.randomUUID())
                        .sellerId(10L)
                        .pricePolicyId(200L)
                        .quantity(2)
                        .unitAmount(30000L)
                        .orderStatus(status)
                        .accumulatedPoint(0L)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber(OrderNumber.of("ORD-" + orderId))
                .orderStatus(status)
                .amount(OrderAmount.of(110000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithProductKeys(Long orderId, Long buyerId, UUID productKey1, UUID productKey2) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId).productKey(productKey1).sellerId(10L).pricePolicyId(100L)
                        .quantity(1).unitAmount(50000L).accumulatedPoint(0L)
                        .orderStatus(OrderStatus.DELIVERED).build(),
                OrderProductSnapshotState.builder()
                        .orderId(orderId).productKey(productKey2).sellerId(10L).pricePolicyId(200L)
                        .quantity(2).unitAmount(30000L).accumulatedPoint(0L)
                        .orderStatus(OrderStatus.DELIVERED).build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber(OrderNumber.of("ORD-" + orderId))
                .orderStatus(OrderStatus.DELIVERED)
                .amount(OrderAmount.of(110000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithMixedProductStatuses(Long orderId, Long buyerId) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId).productKey(UUID.randomUUID()).sellerId(10L).pricePolicyId(100L)
                        .quantity(1).unitAmount(50000L).accumulatedPoint(0L)
                        .orderStatus(OrderStatus.RETURN_REQUESTED).build(),
                OrderProductSnapshotState.builder()
                        .orderId(orderId).productKey(UUID.randomUUID()).sellerId(10L).pricePolicyId(200L)
                        .quantity(2).unitAmount(30000L).accumulatedPoint(0L)
                        .orderStatus(OrderStatus.DELIVERED).build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber(OrderNumber.of("ORD-" + orderId))
                .orderStatus(OrderStatus.PARTIALLY_RETURN_REQUESTED)
                .amount(OrderAmount.of(110000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithBuyerId(Long orderId, Long buyerId, OrderStatus status) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(100L)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(status)
                        .accumulatedPoint(0L)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber(OrderNumber.of("ORD-" + orderId))
                .orderStatus(status)
                .amount(OrderAmount.of(50000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
