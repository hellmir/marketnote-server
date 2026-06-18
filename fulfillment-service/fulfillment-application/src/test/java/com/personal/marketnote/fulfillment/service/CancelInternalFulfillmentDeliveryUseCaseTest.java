package com.personal.marketnote.fulfillment.service;

import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistration;
import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistrationSnapshotState;
import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentWorkStatus;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryCancellationNotAllowedException;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryRegistrationNotFoundException;
import com.personal.marketnote.fulfillment.port.in.command.CancelInternalFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.CancelInternalFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CancelFulfillmentDeliveryItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CancelFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingStatus;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTrackerSnapshotState;
import com.personal.marketnote.fulfillment.port.out.delivery.FindFulfillmentDeliveryRegistrationPort;
import com.personal.marketnote.fulfillment.port.out.shipping.FindShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.shipping.UpdateShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.vendor.CancelFulfillmentDeliveryPort;
import com.personal.marketnote.fulfillment.port.out.vendor.DisconnectFulfillmentAuthPort;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentCustomerCodePort;
import com.personal.marketnote.fulfillment.port.out.vendor.RequestFulfillmentAuthPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelInternalFulfillmentDeliveryService 테스트")
class CancelInternalFulfillmentDeliveryUseCaseTest {
    @InjectMocks
    private CancelInternalFulfillmentDeliveryService cancelInternalFulfillmentDeliveryService;

    @Mock
    private FindFulfillmentDeliveryRegistrationPort findFulfillmentDeliveryRegistrationPort;

    @Mock
    private GetFulfillmentCustomerCodePort getFulfillmentCustomerCodePort;

    @Mock
    private RequestFulfillmentAuthPort requestFulfillmentAuthPort;

    @Mock
    private DisconnectFulfillmentAuthPort disconnectFulfillmentAuthPort;

    @Mock
    private CancelFulfillmentDeliveryPort cancelFulfillmentDeliveryPort;

    @Mock
    private FindShippingTrackerPort findShippingTrackerPort;

    @Mock
    private UpdateShippingTrackerPort updateShippingTrackerPort;

    @ParameterizedTest
    @EnumSource(value = FulfillmentWorkStatus.class, names = {"REGISTERED", "NOT_REGISTERED", "PICKING"})
    @DisplayName("취소 가능한 상태에서 출고 취소를 요청하면 Fassto 출고 취소를 호출하고 성공 결과를 반환한다")
    void shouldCancelDeliveryWhenStatusIsCancellable(FulfillmentWorkStatus cancellableStatus) {
        // given
        Long orderId = 100L;
        CancelInternalFulfillmentDeliveryCommand command = new CancelInternalFulfillmentDeliveryCommand(orderId);

        FulfillmentDeliveryRegistration registration = createRegistration(orderId, cancellableStatus);
        when(findFulfillmentDeliveryRegistrationPort.findByOrderId(orderId)).thenReturn(Optional.of(registration));

        FulfillmentAccessToken accessToken = FulfillmentAccessToken.of("test-token", "20261231235959");
        when(requestFulfillmentAuthPort.requestAccessToken()).thenReturn(accessToken);
        when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn("CUST001");

        CancelFulfillmentDeliveryResult fasstoResult = CancelFulfillmentDeliveryResult.of(1, List.of(
                CancelFulfillmentDeliveryItemResult.of("SLIP001", String.valueOf(orderId), "성공", "00", null)
        ));
        when(cancelFulfillmentDeliveryPort.cancelDelivery(any())).thenReturn(fasstoResult);

        // when
        CancelInternalFulfillmentDeliveryResult result = cancelInternalFulfillmentDeliveryService.cancelDelivery(command);

        // then
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.cancelled()).isTrue();
        verify(cancelFulfillmentDeliveryPort).cancelDelivery(any());
        verify(disconnectFulfillmentAuthPort).disconnectAccessToken("test-token");
    }

    @ParameterizedTest
    @EnumSource(value = FulfillmentWorkStatus.class, names = {"PICKED", "PACKING", "RELEASED"})
    @DisplayName("취소 불가능한 상태에서 출고 취소를 요청하면 FulfillmentDeliveryCancellationNotAllowedException이 발생한다")
    void shouldThrowExceptionWhenStatusIsNotCancellable(FulfillmentWorkStatus nonCancellableStatus) {
        // given
        Long orderId = 100L;
        CancelInternalFulfillmentDeliveryCommand command = new CancelInternalFulfillmentDeliveryCommand(orderId);

        FulfillmentDeliveryRegistration registration = createRegistration(orderId, nonCancellableStatus);
        when(findFulfillmentDeliveryRegistrationPort.findByOrderId(orderId)).thenReturn(Optional.of(registration));

        // when & then
        assertThatThrownBy(() -> cancelInternalFulfillmentDeliveryService.cancelDelivery(command))
                .isInstanceOf(FulfillmentDeliveryCancellationNotAllowedException.class);

        verifyNoInteractions(requestFulfillmentAuthPort);
        verifyNoInteractions(cancelFulfillmentDeliveryPort);
        verifyNoInteractions(disconnectFulfillmentAuthPort);
    }

    @Test
    @DisplayName("orderId에 해당하는 출고 등록이 없으면 FulfillmentDeliveryRegistrationNotFoundException이 발생한다")
    void shouldThrowExceptionWhenRegistrationNotFound() {
        // given
        Long orderId = 999L;
        CancelInternalFulfillmentDeliveryCommand command = new CancelInternalFulfillmentDeliveryCommand(orderId);
        when(findFulfillmentDeliveryRegistrationPort.findByOrderId(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cancelInternalFulfillmentDeliveryService.cancelDelivery(command))
                .isInstanceOf(FulfillmentDeliveryRegistrationNotFoundException.class);

        verifyNoInteractions(requestFulfillmentAuthPort);
        verifyNoInteractions(cancelFulfillmentDeliveryPort);
        verifyNoInteractions(disconnectFulfillmentAuthPort);
    }

    @Test
    @DisplayName("Fassto 출고 취소가 실패하면 예외가 전파되고 인증 해제는 호출된다")
    void shouldPropagateExceptionAndDisconnectWhenCancelFails() {
        // given
        Long orderId = 100L;
        CancelInternalFulfillmentDeliveryCommand command = new CancelInternalFulfillmentDeliveryCommand(orderId);

        FulfillmentDeliveryRegistration registration = createRegistration(orderId, FulfillmentWorkStatus.REGISTERED);
        when(findFulfillmentDeliveryRegistrationPort.findByOrderId(orderId)).thenReturn(Optional.of(registration));

        FulfillmentAccessToken accessToken = FulfillmentAccessToken.of("test-token", "20261231235959");
        when(requestFulfillmentAuthPort.requestAccessToken()).thenReturn(accessToken);
        when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn("CUST001");
        when(cancelFulfillmentDeliveryPort.cancelDelivery(any())).thenThrow(new RuntimeException("Fassto 호출 실패"));

        // when & then
        assertThatThrownBy(() -> cancelInternalFulfillmentDeliveryService.cancelDelivery(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Fassto 호출 실패");

        verify(disconnectFulfillmentAuthPort).disconnectAccessToken("test-token");
    }

    @Test
    @DisplayName("출고 취소 성공 시 인증 발급 → 취소 호출 → 인증 해제 순서로 실행된다")
    void shouldExecuteInCorrectOrder() {
        // given
        Long orderId = 100L;
        CancelInternalFulfillmentDeliveryCommand command = new CancelInternalFulfillmentDeliveryCommand(orderId);

        FulfillmentDeliveryRegistration registration = createRegistration(orderId, FulfillmentWorkStatus.REGISTERED);
        when(findFulfillmentDeliveryRegistrationPort.findByOrderId(orderId)).thenReturn(Optional.of(registration));

        FulfillmentAccessToken accessToken = FulfillmentAccessToken.of("test-token", "20261231235959");
        when(requestFulfillmentAuthPort.requestAccessToken()).thenReturn(accessToken);
        when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn("CUST001");

        CancelFulfillmentDeliveryResult fasstoResult = CancelFulfillmentDeliveryResult.of(1, List.of(
                CancelFulfillmentDeliveryItemResult.of("SLIP001", String.valueOf(orderId), "성공", "00", null)
        ));
        when(cancelFulfillmentDeliveryPort.cancelDelivery(any())).thenReturn(fasstoResult);

        // when
        cancelInternalFulfillmentDeliveryService.cancelDelivery(command);

        // then
        InOrder inOrder = inOrder(requestFulfillmentAuthPort, cancelFulfillmentDeliveryPort, disconnectFulfillmentAuthPort);
        inOrder.verify(requestFulfillmentAuthPort).requestAccessToken();
        inOrder.verify(cancelFulfillmentDeliveryPort).cancelDelivery(any());
        inOrder.verify(disconnectFulfillmentAuthPort).disconnectAccessToken("test-token");
    }

    @ParameterizedTest
    @EnumSource(value = ShippingStatus.class, names = {"PREPARING", "SHIPPING"})
    @DisplayName("ShippingTracker가 PREPARING/SHIPPING 상태이면 CANCELLED로 전이하고 update를 호출한다")
    void shouldCancelShippingTrackerWhenInPreparingOrShipping(ShippingStatus status) {
        // given
        Long orderId = 100L;
        CancelInternalFulfillmentDeliveryCommand command = new CancelInternalFulfillmentDeliveryCommand(orderId);

        FulfillmentDeliveryRegistration registration = createRegistration(orderId, FulfillmentWorkStatus.REGISTERED);
        when(findFulfillmentDeliveryRegistrationPort.findByOrderId(orderId)).thenReturn(Optional.of(registration));

        FulfillmentAccessToken accessToken = FulfillmentAccessToken.of("test-token", "20261231235959");
        when(requestFulfillmentAuthPort.requestAccessToken()).thenReturn(accessToken);
        when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn("CUST001");

        CancelFulfillmentDeliveryResult fasstoResult = CancelFulfillmentDeliveryResult.of(1, List.of(
                CancelFulfillmentDeliveryItemResult.of("SLIP001", String.valueOf(orderId), "성공", "00", null)
        ));
        when(cancelFulfillmentDeliveryPort.cancelDelivery(any())).thenReturn(fasstoResult);

        ShippingTracker tracker = createShippingTracker(orderId, status);
        when(findShippingTrackerPort.findByOrderId(orderId)).thenReturn(Optional.of(tracker));

        // when
        cancelInternalFulfillmentDeliveryService.cancelDelivery(command);

        // then
        org.mockito.ArgumentCaptor<ShippingTracker> captor = org.mockito.ArgumentCaptor.forClass(ShippingTracker.class);
        verify(updateShippingTrackerPort).update(captor.capture());
        ShippingTracker updated = captor.getValue();
        assertThat(updated.isCancelled()).isTrue();
        assertThat(updated.isPollingActive()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ShippingStatus.class, names = {"DELIVERED", "CANCELLED", "RETURN_DELIVERED", "DELIVERY_FAILED"})
    @DisplayName("ShippingTracker가 종료 상태이면 cancel을 호출하지 않는다 (멱등성)")
    void shouldSkipShippingTrackerWhenTerminal(ShippingStatus terminalStatus) {
        // given
        Long orderId = 100L;
        CancelInternalFulfillmentDeliveryCommand command = new CancelInternalFulfillmentDeliveryCommand(orderId);

        FulfillmentDeliveryRegistration registration = createRegistration(orderId, FulfillmentWorkStatus.REGISTERED);
        when(findFulfillmentDeliveryRegistrationPort.findByOrderId(orderId)).thenReturn(Optional.of(registration));

        FulfillmentAccessToken accessToken = FulfillmentAccessToken.of("test-token", "20261231235959");
        when(requestFulfillmentAuthPort.requestAccessToken()).thenReturn(accessToken);
        when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn("CUST001");

        CancelFulfillmentDeliveryResult fasstoResult = CancelFulfillmentDeliveryResult.of(1, List.of(
                CancelFulfillmentDeliveryItemResult.of("SLIP001", String.valueOf(orderId), "성공", "00", null)
        ));
        when(cancelFulfillmentDeliveryPort.cancelDelivery(any())).thenReturn(fasstoResult);

        ShippingTracker tracker = createShippingTracker(orderId, terminalStatus);
        when(findShippingTrackerPort.findByOrderId(orderId)).thenReturn(Optional.of(tracker));

        // when
        cancelInternalFulfillmentDeliveryService.cancelDelivery(command);

        // then
        verify(updateShippingTrackerPort, never()).update(any());
    }

    @Test
    @DisplayName("ShippingTracker가 존재하지 않으면 정상 처리한다 (예외 없음)")
    void shouldSkipWhenShippingTrackerNotFound() {
        // given
        Long orderId = 100L;
        CancelInternalFulfillmentDeliveryCommand command = new CancelInternalFulfillmentDeliveryCommand(orderId);

        FulfillmentDeliveryRegistration registration = createRegistration(orderId, FulfillmentWorkStatus.REGISTERED);
        when(findFulfillmentDeliveryRegistrationPort.findByOrderId(orderId)).thenReturn(Optional.of(registration));

        FulfillmentAccessToken accessToken = FulfillmentAccessToken.of("test-token", "20261231235959");
        when(requestFulfillmentAuthPort.requestAccessToken()).thenReturn(accessToken);
        when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn("CUST001");

        CancelFulfillmentDeliveryResult fasstoResult = CancelFulfillmentDeliveryResult.of(1, List.of(
                CancelFulfillmentDeliveryItemResult.of("SLIP001", String.valueOf(orderId), "성공", "00", null)
        ));
        when(cancelFulfillmentDeliveryPort.cancelDelivery(any())).thenReturn(fasstoResult);

        when(findShippingTrackerPort.findByOrderId(orderId)).thenReturn(Optional.empty());

        // when
        cancelInternalFulfillmentDeliveryService.cancelDelivery(command);

        // then
        verify(updateShippingTrackerPort, never()).update(any());
    }

    private ShippingTracker createShippingTracker(Long orderId, ShippingStatus status) {
        return ShippingTracker.from(ShippingTrackerSnapshotState.builder()
                .id(1L)
                .orderId(orderId)
                .shippingStatus(status)
                .pollingActive(!status.isTerminal())
                .createdAt(LocalDateTime.of(2026, 4, 7, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 7, 10, 0))
                .build());
    }

    private FulfillmentDeliveryRegistration createRegistration(Long orderId, FulfillmentWorkStatus workStatus) {
        return FulfillmentDeliveryRegistration.from(
                FulfillmentDeliveryRegistrationSnapshotState.builder()
                        .id(1L)
                        .orderId(orderId)
                        .workStatus(workStatus)
                        .createdAt(LocalDateTime.of(2026, 4, 7, 10, 0))
                        .build()
        );
    }
}
