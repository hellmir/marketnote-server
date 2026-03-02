package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.OrderNotFoundException;
import com.personal.marketnote.commerce.port.in.command.order.RegisterTrackingInfoCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterTrackingInfoUseCase 테스트")
class RegisterTrackingInfoUseCaseTest {

    @InjectMocks
    private RegisterTrackingInfoService registerTrackingInfoService;

    @Mock
    private GetOrderUseCase getOrderUseCase;

    @Mock
    private UpdateOrderPort updateOrderPort;

    private Order createOrder(Long id, OrderStatus status) {
        return Order.from(OrderSnapshotState.builder()
                .id(id)
                .buyerId(100L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD" + id)
                .orderStatus(status)
                .totalAmount(100000L)
                .paidAmount(95000L)
                .couponAmount(3000L)
                .pointAmount(2000L)
                .orderProductStates(List.of())
                .createdAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                .build());
    }

    private Order createOrderWithTrackingInfo(Long id) {
        return Order.from(OrderSnapshotState.builder()
                .id(id)
                .buyerId(100L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD" + id)
                .orderStatus(OrderStatus.PREPARED)
                .totalAmount(100000L)
                .paidAmount(95000L)
                .couponAmount(3000L)
                .pointAmount(2000L)
                .courierCompany(CourierCompany.CJ_LOGISTICS)
                .trackingNumber("9876543210")
                .orderProductStates(List.of())
                .createdAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                .build());
    }

    private RegisterTrackingInfoCommand createCommand(Long orderId) {
        return RegisterTrackingInfoCommand.builder()
                .orderId(orderId)
                .courierCompany(CourierCompany.CJ_LOGISTICS)
                .trackingNumber("1234567890")
                .build();
    }

    @Nested
    @DisplayName("송장 정보 등록 성공 테스트")
    class RegisterTrackingInfoSuccessTest {

        @Test
        @DisplayName("송장 정보를 정상적으로 등록한다")
        void shouldRegisterTrackingInfoSuccessfully() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PREPARED);
            RegisterTrackingInfoCommand command = createCommand(orderId);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            // when
            registerTrackingInfoService.registerTrackingInfo(command);

            // then
            assertThat(order.getCourierCompany()).isEqualTo(CourierCompany.CJ_LOGISTICS);
            assertThat(order.getTrackingNumber()).isEqualTo("1234567890");
            verify(getOrderUseCase).getOrder(orderId);
            verify(updateOrderPort).updateTrackingInfo(order);
            verifyNoMoreInteractions(getOrderUseCase, updateOrderPort);
        }

        @Test
        @DisplayName("기존 송장 정보가 있는 주문에 새 송장 정보를 덮어쓴다")
        void shouldOverwriteExistingTrackingInfo() {
            // given
            Long orderId = 1L;
            Order order = createOrderWithTrackingInfo(orderId);
            RegisterTrackingInfoCommand command = RegisterTrackingInfoCommand.builder()
                    .orderId(orderId)
                    .courierCompany(CourierCompany.HANJIN)
                    .trackingNumber("5555555555")
                    .build();
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            // when
            registerTrackingInfoService.registerTrackingInfo(command);

            // then
            assertThat(order.getCourierCompany()).isEqualTo(CourierCompany.HANJIN);
            assertThat(order.getTrackingNumber()).isEqualTo("5555555555");
            verify(updateOrderPort).updateTrackingInfo(order);
        }

        @Test
        @DisplayName("다양한 택배사로 송장 정보를 등록할 수 있다")
        void shouldRegisterWithVariousCourierCompanies() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PREPARED);
            RegisterTrackingInfoCommand command = RegisterTrackingInfoCommand.builder()
                    .orderId(orderId)
                    .courierCompany(CourierCompany.LOTTE)
                    .trackingNumber("LOTTE123")
                    .build();
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            // when
            registerTrackingInfoService.registerTrackingInfo(command);

            // then
            assertThat(order.getCourierCompany()).isEqualTo(CourierCompany.LOTTE);
            assertThat(order.getTrackingNumber()).isEqualTo("LOTTE123");
        }
    }

    @Nested
    @DisplayName("송장 정보 등록 실패 테스트")
    class RegisterTrackingInfoFailureTest {

        @Test
        @DisplayName("존재하지 않는 주문에 송장 정보를 등록하면 OrderNotFoundException을 던진다")
        void shouldThrowWhenOrderNotFound() {
            // given
            Long orderId = 999L;
            RegisterTrackingInfoCommand command = createCommand(orderId);
            when(getOrderUseCase.getOrder(orderId)).thenThrow(new OrderNotFoundException(orderId));

            // when & then
            assertThatThrownBy(() -> registerTrackingInfoService.registerTrackingInfo(command))
                    .isInstanceOf(OrderNotFoundException.class);
            verify(getOrderUseCase).getOrder(orderId);
            verifyNoInteractions(updateOrderPort);
        }
    }

    @Nested
    @DisplayName("도메인 행위 메서드 테스트")
    class DomainBehaviorTest {

        @Test
        @DisplayName("송장 정보가 없는 주문의 hasTrackingInfo는 false를 반환한다")
        void shouldReturnFalseWhenNoTrackingInfo() {
            // given
            Order order = createOrder(1L, OrderStatus.PREPARED);

            // then
            assertThat(order.hasTrackingInfo()).isFalse();
        }

        @Test
        @DisplayName("송장 정보를 등록한 주문의 hasTrackingInfo는 true를 반환한다")
        void shouldReturnTrueAfterRegisteringTrackingInfo() {
            // given
            Order order = createOrder(1L, OrderStatus.PREPARED);

            // when
            order.registerTrackingInfo(CourierCompany.CJ_LOGISTICS, "1234567890");

            // then
            assertThat(order.hasTrackingInfo()).isTrue();
        }

        @Test
        @DisplayName("스냅샷에서 복원된 송장 정보가 있는 주문의 hasTrackingInfo는 true를 반환한다")
        void shouldReturnTrueWhenRestoredWithTrackingInfo() {
            // given
            Order order = createOrderWithTrackingInfo(1L);

            // then
            assertThat(order.hasTrackingInfo()).isTrue();
            assertThat(order.getCourierCompany()).isEqualTo(CourierCompany.CJ_LOGISTICS);
            assertThat(order.getTrackingNumber()).isEqualTo("9876543210");
        }
    }

    @Nested
    @DisplayName("Port 호출 검증 테스트")
    class PortInvocationTest {

        @Test
        @DisplayName("getOrderUseCase.getOrder를 정확히 한 번 호출한다")
        void shouldCallGetOrderExactlyOnce() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PREPARED);
            RegisterTrackingInfoCommand command = createCommand(orderId);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            // when
            registerTrackingInfoService.registerTrackingInfo(command);

            // then
            verify(getOrderUseCase, times(1)).getOrder(orderId);
        }

        @Test
        @DisplayName("updateOrderPort.updateTrackingInfo를 정확히 한 번 호출한다")
        void shouldCallUpdateTrackingInfoExactlyOnce() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PREPARED);
            RegisterTrackingInfoCommand command = createCommand(orderId);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            // when
            registerTrackingInfoService.registerTrackingInfo(command);

            // then
            verify(updateOrderPort, times(1)).updateTrackingInfo(order);
        }
    }
}
