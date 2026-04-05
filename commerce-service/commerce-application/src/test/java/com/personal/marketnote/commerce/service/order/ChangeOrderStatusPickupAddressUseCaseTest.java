package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.InvalidPickupRequestMessageException;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.result.user.ShippingAddressInfoResult;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.commerce.port.out.user.FindUserShippingAddressPort;
import com.personal.marketnote.common.domain.delivery.PickupRequestType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeOrderStatusPickupAddressUseCaseTest {
    @Mock
    private GetOrderUseCase getOrderUseCase;
    @Mock
    private UpdateOrderPort updateOrderPort;
    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;
    @Mock
    private ModifyUserPointPort modifyUserPointPort;
    @Mock
    private PublishOrderEventPort publishOrderEventPort;
    @Mock
    private FindUserShippingAddressPort findUserShippingAddressPort;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-04-03T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private ChangeOrderStatusService changeOrderStatusService;

    // ==================================================================================
    // 환불 요청 시 pickupAddressId 기반 회수지 조회 + 적용
    // ==================================================================================

    @Nested
    @DisplayName("환불 요청 시 pickupAddressId 기반 회수지 적용")
    class PickupAddressLookupTest {

        @Test
        @DisplayName("pickupAddressId가 있으면 회수지를 조회하여 적용한다")
        void refundRequested_withPickupAddressId_appliesLookedUpAddress() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pickupAddressId = 50L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            when(findUserShippingAddressPort.findByIdAndUserId(pickupAddressId, buyerId))
                    .thenReturn(new ShippingAddressInfoResult("회수 수령인", "01099998888", "회수지 주소", "회수지 상세주소"));

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.REFUND_REQUESTED)
                    .role("BUYER")
                    .buyerId(buyerId)
                    .pickupAddressId(pickupAddressId)
                    .pickupRequestType(PickupRequestType.LEAVE_AT_DOOR)
                    .pickupRequestMessage(null)
                    .build();

            changeOrderStatusService.changeOrderStatus(command);

            assertThat(order.getPickupAddress()).isNotNull();
            assertThat(order.getPickupAddress().getRecipientName()).isEqualTo("회수 수령인");
            assertThat(order.getPickupAddress().getRecipientPhoneNumber()).isEqualTo("01099998888");
            assertThat(order.getPickupAddress().getAddress()).isEqualTo("회수지 주소");
            assertThat(order.getPickupAddress().getAddressDetail()).isEqualTo("회수지 상세주소");
            assertThat(order.getPickupAddress().getPickupRequestType()).isEqualTo(PickupRequestType.LEAVE_AT_DOOR);
            verify(findUserShippingAddressPort).findByIdAndUserId(pickupAddressId, buyerId);
        }

        @Test
        @DisplayName("pickupAddressId가 없으면 배송지가 회수지 기본값으로 적용된다")
        void refundRequested_withoutPickupAddressId_appliesShippingAddressAsDefault() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.REFUND_REQUESTED)
                    .role("BUYER")
                    .buyerId(buyerId)
                    .build();

            changeOrderStatusService.changeOrderStatus(command);

            assertThat(order.getPickupAddress()).isNotNull();
            assertThat(order.getPickupAddress().getRecipientName()).isEqualTo("수령인");
            assertThat(order.getPickupAddress().getRecipientPhoneNumber()).isEqualTo("01012345678");
            assertThat(order.getPickupAddress().getAddress()).isEqualTo("서울시 강남구");
            assertThat(order.getPickupAddress().getAddressDetail()).isEqualTo("상세주소");
            verifyNoInteractions(findUserShippingAddressPort);
        }

        @Test
        @DisplayName("pickupAddressId가 있으면 FindUserShippingAddressPort를 호출한다")
        void refundRequested_withPickupAddressId_callsFindPort() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pickupAddressId = 50L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            when(findUserShippingAddressPort.findByIdAndUserId(pickupAddressId, buyerId))
                    .thenReturn(new ShippingAddressInfoResult("회수 수령인", "01099998888", "회수지 주소", "회수지 상세주소"));

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.REFUND_REQUESTED)
                    .role("BUYER")
                    .buyerId(buyerId)
                    .pickupAddressId(pickupAddressId)
                    .pickupRequestType(PickupRequestType.NONE)
                    .build();

            changeOrderStatusService.changeOrderStatus(command);

            verify(findUserShippingAddressPort).findByIdAndUserId(pickupAddressId, buyerId);
        }
    }

    // ==================================================================================
    // CUSTOM 타입 검증
    // ==================================================================================

    @Nested
    @DisplayName("CUSTOM 타입 검증")
    class CustomPickupRequestTypeValidationTest {

        @Test
        @DisplayName("CUSTOM 타입에 메시지가 있으면 정상 처리된다")
        void customType_withMessage_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pickupAddressId = 50L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            when(findUserShippingAddressPort.findByIdAndUserId(pickupAddressId, buyerId))
                    .thenReturn(new ShippingAddressInfoResult("회수 수령인", "01099998888", "회수지 주소", "회수지 상세주소"));

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.REFUND_REQUESTED)
                    .role("BUYER")
                    .buyerId(buyerId)
                    .pickupAddressId(pickupAddressId)
                    .pickupRequestType(PickupRequestType.CUSTOM)
                    .pickupRequestMessage("부재시 경비실에 맡겨주세요")
                    .build();

            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();

            assertThat(order.getPickupAddress().getDeliveryRequestMessage()).isEqualTo("부재시 경비실에 맡겨주세요");
        }

        @Test
        @DisplayName("CUSTOM 타입에 메시지가 없으면 InvalidPickupRequestMessageException이 발생한다")
        void customType_withoutMessage_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pickupAddressId = 50L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.REFUND_REQUESTED)
                    .role("BUYER")
                    .buyerId(buyerId)
                    .pickupAddressId(pickupAddressId)
                    .pickupRequestType(PickupRequestType.CUSTOM)
                    .pickupRequestMessage(null)
                    .build();

            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(InvalidPickupRequestMessageException.class);
        }

        @Test
        @DisplayName("CUSTOM 타입에 빈 문자열 메시지이면 InvalidPickupRequestMessageException이 발생한다")
        void customType_withEmptyMessage_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pickupAddressId = 50L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.REFUND_REQUESTED)
                    .role("BUYER")
                    .buyerId(buyerId)
                    .pickupAddressId(pickupAddressId)
                    .pickupRequestType(PickupRequestType.CUSTOM)
                    .pickupRequestMessage("")
                    .build();

            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(InvalidPickupRequestMessageException.class);
        }

        @Test
        @DisplayName("CUSTOM 타입 검증 실패 시 UpdateOrderPort를 호출하지 않는다")
        void customType_validationFailed_doesNotCallUpdatePort() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pickupAddressId = 50L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.REFUND_REQUESTED)
                    .role("BUYER")
                    .buyerId(buyerId)
                    .pickupAddressId(pickupAddressId)
                    .pickupRequestType(PickupRequestType.CUSTOM)
                    .pickupRequestMessage(null)
                    .build();

            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(InvalidPickupRequestMessageException.class);

            verifyNoInteractions(updateOrderPort);
        }
    }

    // ==================================================================================
    // 환불 요청이 아닌 경우 회수지 미조회
    // ==================================================================================

    @Nested
    @DisplayName("환불 요청이 아닌 경우")
    class NonRefundRequestTest {

        @Test
        @DisplayName("환불 요청이 아닌 상태 변경 시 FindUserShippingAddressPort를 호출하지 않는다")
        void nonRefundRequest_doesNotCallFindPort() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.CANCEL_REQUESTED)
                    .role("BUYER")
                    .buyerId(buyerId)
                    .build();

            changeOrderStatusService.changeOrderStatus(command);

            verifyNoInteractions(findUserShippingAddressPort);
        }

        @Test
        @DisplayName("환불 요청이 아닌 상태 변경 시 회수지가 적용되지 않는다")
        void nonRefundRequest_pickupAddressNotApplied() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.CANCEL_REQUESTED)
                    .role("BUYER")
                    .buyerId(buyerId)
                    .pickupAddressId(50L)
                    .pickupRequestType(PickupRequestType.LEAVE_AT_DOOR)
                    .build();

            changeOrderStatusService.changeOrderStatus(command);

            assertThat(order.getPickupAddress()).isNull();
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private Order createOrderWithBuyerId(Long orderId, Long buyerId, OrderStatus status) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(100L)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(status)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(status)
                .amount(OrderAmount.of(50000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
