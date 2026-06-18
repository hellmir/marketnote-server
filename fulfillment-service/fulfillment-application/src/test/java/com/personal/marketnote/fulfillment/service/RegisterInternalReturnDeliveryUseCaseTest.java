package com.personal.marketnote.fulfillment.service;

import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingStatus;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTrackerSnapshotState;
import com.personal.marketnote.fulfillment.port.in.command.RegisterInternalReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.RegisterInternalReturnDeliveryProductCommand;
import com.personal.marketnote.fulfillment.port.in.result.RegisterInternalReturnDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.out.shipping.FindShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.shipping.UpdateShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.vendor.DisconnectFulfillmentAuthPort;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentCustomerCodePort;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentReturnDeliveryPort;
import com.personal.marketnote.fulfillment.port.out.vendor.RequestFulfillmentAuthPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterInternalReturnDeliveryService 테스트")
class RegisterInternalReturnDeliveryUseCaseTest {

    @InjectMocks
    private RegisterInternalReturnDeliveryService registerInternalReturnDeliveryService;

    @Mock
    private GetFulfillmentCustomerCodePort getFulfillmentCustomerCodePort;

    @Mock
    private RequestFulfillmentAuthPort requestFulfillmentAuthPort;

    @Mock
    private DisconnectFulfillmentAuthPort disconnectFulfillmentAuthPort;

    @Mock
    private RegisterFulfillmentReturnDeliveryPort registerFulfillmentReturnDeliveryPort;

    @Mock
    private FindShippingTrackerPort findShippingTrackerPort;

    @Mock
    private UpdateShippingTrackerPort updateShippingTrackerPort;

    @Test
    @DisplayName("DELIVERED ShippingTracker가 존재하면 RETURN_SHIPPING으로 전이하고 폴링이 재활성화된다")
    void shouldStartReturnShippingWhenTrackerDelivered() {
        // given
        Long orderId = 100L;
        RegisterInternalReturnDeliveryCommand command = createCommand(orderId);

        stubFasstoSuccess(orderId);
        ShippingTracker tracker = createTracker(orderId, ShippingStatus.DELIVERED);
        when(findShippingTrackerPort.findByOrderId(orderId)).thenReturn(Optional.of(tracker));

        // when
        RegisterInternalReturnDeliveryResult result = registerInternalReturnDeliveryService.registerReturnDelivery(command);

        // then
        assertThat(result.registered()).isTrue();
        ArgumentCaptor<ShippingTracker> captor = ArgumentCaptor.forClass(ShippingTracker.class);
        verify(updateShippingTrackerPort).update(captor.capture());
        ShippingTracker updated = captor.getValue();
        assertThat(updated.isReturnShipping()).isTrue();
        assertThat(updated.isPollingActive()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ShippingStatus.class, names = {"PREPARING", "SHIPPING", "CANCELLED", "RETURN_SHIPPING", "RETURN_DELIVERED", "DELIVERY_FAILED"})
    @DisplayName("ShippingTracker가 DELIVERED가 아니면 회수 폴링을 재개하지 않는다 (멱등성)")
    void shouldSkipWhenTrackerNotDelivered(ShippingStatus nonDeliveredStatus) {
        // given
        Long orderId = 100L;
        RegisterInternalReturnDeliveryCommand command = createCommand(orderId);

        stubFasstoSuccess(orderId);
        ShippingTracker tracker = createTracker(orderId, nonDeliveredStatus);
        when(findShippingTrackerPort.findByOrderId(orderId)).thenReturn(Optional.of(tracker));

        // when
        registerInternalReturnDeliveryService.registerReturnDelivery(command);

        // then
        verify(updateShippingTrackerPort, never()).update(any());
    }

    @Test
    @DisplayName("ShippingTracker가 존재하지 않으면 정상 처리한다 (예외 없음)")
    void shouldSkipWhenTrackerNotFound() {
        // given
        Long orderId = 100L;
        RegisterInternalReturnDeliveryCommand command = createCommand(orderId);

        stubFasstoSuccess(orderId);
        when(findShippingTrackerPort.findByOrderId(orderId)).thenReturn(Optional.empty());

        // when
        registerInternalReturnDeliveryService.registerReturnDelivery(command);

        // then
        verify(updateShippingTrackerPort, never()).update(any());
    }

    @Test
    @DisplayName("파스토 응답이 비어있으면 ShippingTracker를 변경하지 않는다")
    void shouldSkipWhenFasstoReturnsEmpty() {
        // given
        Long orderId = 100L;
        RegisterInternalReturnDeliveryCommand command = createCommand(orderId);

        FulfillmentAccessToken accessToken = FulfillmentAccessToken.of("test-token", "20261231235959");
        when(requestFulfillmentAuthPort.requestAccessToken()).thenReturn(accessToken);
        when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn("CUST001");
        when(registerFulfillmentReturnDeliveryPort.registerReturnDelivery(any()))
                .thenReturn(RegisterFulfillmentDeliveryResult.of(0, Collections.emptyList()));

        // when
        RegisterInternalReturnDeliveryResult result = registerInternalReturnDeliveryService.registerReturnDelivery(command);

        // then
        assertThat(result.registered()).isFalse();
        verifyNoInteractions(findShippingTrackerPort);
        verify(updateShippingTrackerPort, never()).update(any());
    }

    private void stubFasstoSuccess(Long orderId) {
        FulfillmentAccessToken accessToken = FulfillmentAccessToken.of("test-token", "20261231235959");
        when(requestFulfillmentAuthPort.requestAccessToken()).thenReturn(accessToken);
        when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn("CUST001");

        RegisterFulfillmentDeliveryResult fasstoResult = RegisterFulfillmentDeliveryResult.of(1, List.of(
                RegisterFulfillmentDeliveryItemResult.of("RETURN001", String.valueOf(orderId), "성공", "00", null)
        ));
        when(registerFulfillmentReturnDeliveryPort.registerReturnDelivery(any())).thenReturn(fasstoResult);
    }

    private RegisterInternalReturnDeliveryCommand createCommand(Long orderId) {
        return RegisterInternalReturnDeliveryCommand.builder()
                .orderId(orderId)
                .orderDate("20260401")
                .recipientName("홍길동")
                .recipientPhoneNumber("01012345678")
                .recipientAddress("서울시 강남구")
                .pickupRecipientName("홍길동")
                .pickupRecipientPhoneNumber("01012345678")
                .pickupZipCode("06000")
                .pickupAddress("서울시 강남구")
                .pickupAddressDetail("123")
                .returnReason("단순 변심")
                .returnDetailReason("색상 마음에 안 듦")
                .returnShippingRequest("문 앞")
                .products(List.of(
                        RegisterInternalReturnDeliveryProductCommand.of("PROD001", 1)
                ))
                .build();
    }

    private ShippingTracker createTracker(Long orderId, ShippingStatus status) {
        return ShippingTracker.from(ShippingTrackerSnapshotState.builder()
                .id(1L)
                .orderId(orderId)
                .trackingNumber("INV001")
                .carrierCode("CJ")
                .shippingStatus(status)
                .pollingActive(!status.isTerminal())
                .createdAt(LocalDateTime.of(2026, 4, 7, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 7, 10, 0))
                .build());
    }
}
