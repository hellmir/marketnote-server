package com.personal.marketnote.fulfillment.service.shipping;

import com.personal.marketnote.common.kafka.event.ShippingStatusChangedEvent;
import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingStatus;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTrackerCreateState;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTrackerSnapshotState;
import com.personal.marketnote.fulfillment.port.in.command.PollShippingStatusCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentDeliveryStatusInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryStatusesResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFulfillmentAuthUseCase;
import com.personal.marketnote.fulfillment.port.out.event.PublishShippingStatusChangedEventPort;
import com.personal.marketnote.fulfillment.port.out.shipping.FindShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.shipping.UpdateShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentDeliveryStatusesPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PollShippingStatusUseCase 테스트")
class PollShippingStatusUseCaseTest {

    private PollShippingStatusService pollShippingStatusService;

    @Mock
    private FindShippingTrackerPort findShippingTrackerPort;

    @Mock
    private UpdateShippingTrackerPort updateShippingTrackerPort;

    @Mock
    private RequestFulfillmentAuthUseCase requestFulfillmentAuthUseCase;

    @Mock
    private GetFulfillmentDeliveryStatusesPort getDeliveryStatusesPort;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private PublishShippingStatusChangedEventPort publishShippingStatusChangedEventPort;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-06-03T10:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        pollShippingStatusService = new PollShippingStatusService(
                findShippingTrackerPort,
                updateShippingTrackerPort,
                requestFulfillmentAuthUseCase,
                getDeliveryStatusesPort,
                publishShippingStatusChangedEventPort,
                clock,
                transactionManager
        );
    }

    @Test
    @DisplayName("폴링 대상이 없으면 파스토 API를 호출하지 않는다")
    void skipWhenNoActiveTrackers() {
        // given
        when(findShippingTrackerPort.findAllPollingActive()).thenReturn(Collections.emptyList());
        PollShippingStatusCommand command = new PollShippingStatusCommand("CUST001");

        // when
        pollShippingStatusService.pollShippingStatuses(command);

        // then
        verifyNoInteractions(requestFulfillmentAuthUseCase);
        verifyNoInteractions(getDeliveryStatusesPort);
        verifyNoInteractions(updateShippingTrackerPort);
    }

    @Test
    @DisplayName("파스토 상태가 SHIPPING으로 변경되면 송장번호와 함께 배송중으로 전이한다")
    void transitionToShippingWithTrackingInfo() {
        // given
        ShippingTracker tracker = createPreparingTrackerWithId(1L, 100L);
        when(findShippingTrackerPort.findAllPollingActive()).thenReturn(List.of(tracker));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));
        when(requestFulfillmentAuthUseCase.requestAccessToken())
                .thenReturn(FulfillmentAccessToken.of("token123", "20260410100000"));
        when(getDeliveryStatusesPort.getDeliveryStatuses(any()))
                .thenReturn(createDeliveryStatusResult("100", "집하완료", "INV001", "CJ"));

        PollShippingStatusCommand command = new PollShippingStatusCommand("CUST001");

        // when
        pollShippingStatusService.pollShippingStatuses(command);

        // then
        ArgumentCaptor<ShippingTracker> captor = ArgumentCaptor.forClass(ShippingTracker.class);
        verify(updateShippingTrackerPort).update(captor.capture());

        ShippingTracker updated = captor.getValue();
        assertThat(updated.isShipping()).isTrue();
        assertThat(updated.getTrackingNumber()).isEqualTo("INV001");
        assertThat(updated.getCarrierCode()).isEqualTo("CJ");
        assertThat(updated.getLastPolledAt()).isNotNull();

        ArgumentCaptor<ShippingStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(ShippingStatusChangedEvent.class);
        verify(publishShippingStatusChangedEventPort).publish(eventCaptor.capture());
        ShippingStatusChangedEvent event = eventCaptor.getValue();
        assertThat(event.orderId()).isEqualTo(100L);
        assertThat(event.shippingStatus()).isEqualTo("SHIPPING");
        assertThat(event.trackingNumber()).isEqualTo("INV001");
        assertThat(event.carrierCode()).isEqualTo("CJ");
    }

    @Test
    @DisplayName("파스토 상태가 SHIPPING이지만 송장번호가 없으면 송장번호 없이 배송중으로 전이한다")
    void transitionToShippingWithoutTrackingInfo() {
        // given
        ShippingTracker tracker = createPreparingTrackerWithId(1L, 100L);
        when(findShippingTrackerPort.findAllPollingActive()).thenReturn(List.of(tracker));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));
        when(requestFulfillmentAuthUseCase.requestAccessToken())
                .thenReturn(FulfillmentAccessToken.of("token123", "20260410100000"));
        when(getDeliveryStatusesPort.getDeliveryStatuses(any()))
                .thenReturn(createDeliveryStatusResult("100", "피킹완료", null, null));

        PollShippingStatusCommand command = new PollShippingStatusCommand("CUST001");

        // when
        pollShippingStatusService.pollShippingStatuses(command);

        // then
        ArgumentCaptor<ShippingTracker> captor = ArgumentCaptor.forClass(ShippingTracker.class);
        verify(updateShippingTrackerPort).update(captor.capture());

        ShippingTracker updated = captor.getValue();
        assertThat(updated.isShipping()).isTrue();
        assertThat(updated.getTrackingNumber()).isNull();

        verify(publishShippingStatusChangedEventPort).publish(any(ShippingStatusChangedEvent.class));
    }

    @Test
    @DisplayName("파스토 상태가 배송완료이면 DELIVERED로 전이한다")
    void transitionToDelivered() {
        // given
        ShippingTracker tracker = createShippingTrackerWithId(1L, 100L);
        when(findShippingTrackerPort.findAllPollingActive()).thenReturn(List.of(tracker));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));
        when(requestFulfillmentAuthUseCase.requestAccessToken())
                .thenReturn(FulfillmentAccessToken.of("token123", "20260410100000"));
        when(getDeliveryStatusesPort.getDeliveryStatuses(any()))
                .thenReturn(createDeliveryStatusResult("100", "배송완료", "INV001", "CJ"));

        PollShippingStatusCommand command = new PollShippingStatusCommand("CUST001");

        // when
        pollShippingStatusService.pollShippingStatuses(command);

        // then
        ArgumentCaptor<ShippingTracker> captor = ArgumentCaptor.forClass(ShippingTracker.class);
        verify(updateShippingTrackerPort).update(captor.capture());

        ShippingTracker updated = captor.getValue();
        assertThat(updated.isDelivered()).isTrue();
        assertThat(updated.isPollingActive()).isFalse();

        ArgumentCaptor<ShippingStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(ShippingStatusChangedEvent.class);
        verify(publishShippingStatusChangedEventPort).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().shippingStatus()).isEqualTo("DELIVERED");
    }

    @Test
    @DisplayName("파스토 상태가 배송불가이면 DELIVERY_FAILED로 전이한다")
    void transitionToDeliveryFailed() {
        // given
        ShippingTracker tracker = createShippingTrackerWithId(1L, 100L);
        when(findShippingTrackerPort.findAllPollingActive()).thenReturn(List.of(tracker));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));
        when(requestFulfillmentAuthUseCase.requestAccessToken())
                .thenReturn(FulfillmentAccessToken.of("token123", "20260410100000"));
        when(getDeliveryStatusesPort.getDeliveryStatuses(any()))
                .thenReturn(createDeliveryStatusResult("100", "배송불가", "INV001", "CJ"));

        PollShippingStatusCommand command = new PollShippingStatusCommand("CUST001");

        // when
        pollShippingStatusService.pollShippingStatuses(command);

        // then
        ArgumentCaptor<ShippingTracker> captor = ArgumentCaptor.forClass(ShippingTracker.class);
        verify(updateShippingTrackerPort).update(captor.capture());

        ShippingTracker updated = captor.getValue();
        assertThat(updated.isDeliveryFailed()).isTrue();
        assertThat(updated.isPollingActive()).isFalse();

        ArgumentCaptor<ShippingStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(ShippingStatusChangedEvent.class);
        verify(publishShippingStatusChangedEventPort).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().shippingStatus()).isEqualTo("DELIVERY_FAILED");
    }

    @Test
    @DisplayName("상태가 동일하면 lastPolledAt만 갱신하고 상태는 변경하지 않는다")
    void noTransitionWhenSameStatus() {
        // given
        ShippingTracker tracker = createPreparingTrackerWithId(1L, 100L);
        when(findShippingTrackerPort.findAllPollingActive()).thenReturn(List.of(tracker));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));
        when(requestFulfillmentAuthUseCase.requestAccessToken())
                .thenReturn(FulfillmentAccessToken.of("token123", "20260410100000"));
        when(getDeliveryStatusesPort.getDeliveryStatuses(any()))
                .thenReturn(createDeliveryStatusResult("100", "피킹중", null, null));

        PollShippingStatusCommand command = new PollShippingStatusCommand("CUST001");

        // when
        pollShippingStatusService.pollShippingStatuses(command);

        // then
        ArgumentCaptor<ShippingTracker> captor = ArgumentCaptor.forClass(ShippingTracker.class);
        verify(updateShippingTrackerPort).update(captor.capture());

        ShippingTracker updated = captor.getValue();
        assertThat(updated.isPreparing()).isTrue();
        assertThat(updated.getLastPolledAt()).isNotNull();

        verifyNoInteractions(publishShippingStatusChangedEventPort);
    }

    @Test
    @DisplayName("파스토 응답에 해당 주문이 없으면 lastPolledAt만 갱신한다")
    void noMatchingOrderInFasstoResponse() {
        // given
        ShippingTracker tracker = createPreparingTrackerWithId(1L, 100L);
        when(findShippingTrackerPort.findAllPollingActive()).thenReturn(List.of(tracker));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));
        when(requestFulfillmentAuthUseCase.requestAccessToken())
                .thenReturn(FulfillmentAccessToken.of("token123", "20260410100000"));
        when(getDeliveryStatusesPort.getDeliveryStatuses(any()))
                .thenReturn(GetFulfillmentDeliveryStatusesResult.of(0, Collections.emptyList()));

        PollShippingStatusCommand command = new PollShippingStatusCommand("CUST001");

        // when
        pollShippingStatusService.pollShippingStatuses(command);

        // then
        ArgumentCaptor<ShippingTracker> captor = ArgumentCaptor.forClass(ShippingTracker.class);
        verify(updateShippingTrackerPort).update(captor.capture());

        ShippingTracker updated = captor.getValue();
        assertThat(updated.isPreparing()).isTrue();
        assertThat(updated.getLastPolledAt()).isNotNull();
    }

    @Test
    @DisplayName("개별 건 처리 중 예외가 발생해도 다른 건은 정상 처리된다")
    void individualFailureDoesNotBlockOthers() {
        // given
        ShippingTracker tracker1 = createPreparingTrackerWithId(1L, 100L);
        ShippingTracker tracker2 = createPreparingTrackerWithId(2L, 200L);
        when(findShippingTrackerPort.findAllPollingActive()).thenReturn(List.of(tracker1, tracker2));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker1));
        when(findShippingTrackerPort.findByOrderId(200L)).thenReturn(Optional.of(tracker2));
        when(requestFulfillmentAuthUseCase.requestAccessToken())
                .thenReturn(FulfillmentAccessToken.of("token123", "20260410100000"));

        // tracker1은 배송완료 (PREPARING→DELIVERED는 전이 불가 → 예외)
        // tracker2는 집하완료 (PREPARING→SHIPPING은 정상 전이)
        GetFulfillmentDeliveryStatusesResult result = GetFulfillmentDeliveryStatusesResult.of(2, List.of(
                createDeliveryStatusInfo("100", "배송완료", "INV001", "CJ"),
                createDeliveryStatusInfo("200", "집하완료", "INV002", "HANJIN")
        ));
        when(getDeliveryStatusesPort.getDeliveryStatuses(any())).thenReturn(result);

        PollShippingStatusCommand command = new PollShippingStatusCommand("CUST001");

        // when
        pollShippingStatusService.pollShippingStatuses(command);

        // then - tracker2는 정상 처리됨 (최소 1회 update 호출)
        verify(updateShippingTrackerPort, atLeastOnce()).update(any(ShippingTracker.class));
    }

    @Test
    @DisplayName("SHIPPING 상태에서 송장번호가 없다가 파스토 응답에 송장번호가 생기면 갱신한다")
    void updateTrackingInfoWhenBecomeAvailable() {
        // given - SHIPPING이지만 송장번호 없는 상태 (advanceToShipping으로 전이된 경우)
        ShippingTracker tracker = ShippingTracker.from(ShippingTrackerSnapshotState.builder()
                .id(1L)
                .orderId(100L)
                .shippingStatus(ShippingStatus.SHIPPING)
                .pollingActive(true)
                .createdAt(LocalDateTime.of(2026, 4, 8, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 8, 10, 0))
                .build());
        when(findShippingTrackerPort.findAllPollingActive()).thenReturn(List.of(tracker));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));
        when(requestFulfillmentAuthUseCase.requestAccessToken())
                .thenReturn(FulfillmentAccessToken.of("token123", "20260410100000"));
        when(getDeliveryStatusesPort.getDeliveryStatuses(any()))
                .thenReturn(createDeliveryStatusResult("100", "집하완료", "INV001", "CJ"));

        PollShippingStatusCommand command = new PollShippingStatusCommand("CUST001");

        // when
        pollShippingStatusService.pollShippingStatuses(command);

        // then
        ArgumentCaptor<ShippingTracker> captor = ArgumentCaptor.forClass(ShippingTracker.class);
        verify(updateShippingTrackerPort).update(captor.capture());

        ShippingTracker updated = captor.getValue();
        assertThat(updated.isShipping()).isTrue();
        assertThat(updated.getTrackingNumber()).isEqualTo("INV001");
        assertThat(updated.getCarrierCode()).isEqualTo("CJ");

        verifyNoInteractions(publishShippingStatusChangedEventPort);
    }

    // --- 테스트 헬퍼 ---

    private ShippingTracker createPreparingTrackerWithId(Long id, Long orderId) {
        return ShippingTracker.from(ShippingTrackerSnapshotState.builder()
                .id(id)
                .orderId(orderId)
                .shippingStatus(ShippingStatus.PREPARING)
                .pollingActive(true)
                .createdAt(LocalDateTime.of(2026, 4, 8, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 8, 10, 0))
                .build());
    }

    private ShippingTracker createShippingTrackerWithId(Long id, Long orderId) {
        return ShippingTracker.from(ShippingTrackerSnapshotState.builder()
                .id(id)
                .orderId(orderId)
                .trackingNumber("INV001")
                .carrierCode("CJ")
                .shippingStatus(ShippingStatus.SHIPPING)
                .pollingActive(true)
                .createdAt(LocalDateTime.of(2026, 4, 8, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 8, 10, 0))
                .build());
    }

    private GetFulfillmentDeliveryStatusesResult createDeliveryStatusResult(
            String ordNo, String crgStNm, String invoiceNo, String courierCode) {
        return GetFulfillmentDeliveryStatusesResult.of(1,
                List.of(createDeliveryStatusInfo(ordNo, crgStNm, invoiceNo, courierCode)));
    }

    private FulfillmentDeliveryStatusInfoResult createDeliveryStatusInfo(
            String ordNo, String crgStNm, String invoiceNo, String courierCode) {
        return FulfillmentDeliveryStatusInfoResult.of(
                null, null, null, null, null,
                null, crgStNm, null, null, null, null, null, null, null, null,
                invoiceNo, ordNo, null, null, null, null, null, null, null,
                courierCode, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null
        );
    }
}
