package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.TrackingInfoRequiredException;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ReduceProductInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.order.DeleteOrderedCartProductsPort;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChangeOrderStatus 송장 정보 검증 테스트")
class ChangeOrderStatusTrackingValidationUseCaseTest {

    @Mock
    private ReduceProductInventoryUseCase reduceProductInventoryUseCase;
    @Mock
    private GetOrderUseCase getOrderUseCase;
    @Mock
    private UpdateOrderPort updateOrderPort;
    @Mock
    private DeleteOrderedCartProductsPort deleteOrderedCartProductsPort;
    @Mock
    private ModifyUserPointPort modifyUserPointPort;

    @InjectMocks
    private ChangeOrderStatusService changeOrderStatusService;

    @Nested
    @DisplayName("SHIPPING 상태 전이 시 송장 정보 검증")
    class ShippingTrackingValidationTest {

        @Test
        @DisplayName("송장 정보가 없는 주문을 SHIPPING으로 변경하면 TrackingInfoRequiredException을 던진다")
        void shouldThrowWhenNoTrackingInfoForShipping() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PREPARED, null, null);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.SHIPPING)
                    .build();

            // when & then
            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(TrackingInfoRequiredException.class);
            verifyNoInteractions(updateOrderPort);
        }

        @Test
        @DisplayName("송장 정보가 있는 주문을 SHIPPING으로 변경하면 정상 처리된다")
        void shouldSucceedWhenTrackingInfoExistsForShipping() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PREPARED, CourierCompany.CJ_LOGISTICS, "1234567890");
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.SHIPPING)
                    .build();

            // when & then
            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("택배사만 있고 송장번호가 없으면 TrackingInfoRequiredException을 던진다")
        void shouldThrowWhenOnlyCourierCompanyExists() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PREPARED, CourierCompany.CJ_LOGISTICS, null);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.SHIPPING)
                    .build();

            // when & then
            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(TrackingInfoRequiredException.class);
        }

        @Test
        @DisplayName("SHIPPING이 아닌 상태 전이 시에는 송장 정보 검증을 수행하지 않는다")
        void shouldNotValidateTrackingInfoForNonShippingTransition() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PAID, null, null);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PREPARING)
                    .build();

            // when & then
            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("DELIVERED 상태 전이 시에는 송장 정보 검증을 수행하지 않는다")
        void shouldNotValidateTrackingInfoForDeliveredTransition() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.SHIPPING, CourierCompany.CJ_LOGISTICS, "1234567890");
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.DELIVERED)
                    .build();

            // when & then
            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }
    }

    private Order createOrder(Long orderId, OrderStatus status, CourierCompany courierCompany, String trackingNumber) {
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
                .buyerId(1L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(status)
                .totalAmount(50000L)
                .couponAmount(0L)
                .pointAmount(0L)
                .courierCompany(courierCompany)
                .trackingNumber(trackingNumber)
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
