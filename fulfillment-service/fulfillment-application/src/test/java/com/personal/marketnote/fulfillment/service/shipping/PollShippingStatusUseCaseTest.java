package com.personal.marketnote.fulfillment.service.shipping;

import com.personal.marketnote.common.kafka.event.ShippingStatusChangedEvent;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingStatus;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTrackerSnapshotState;
import com.personal.marketnote.fulfillment.domain.shipping.CarrierCode;
import com.personal.marketnote.fulfillment.domain.shipping.TrackingNumber;
import com.personal.marketnote.fulfillment.port.in.command.PollShippingStatusCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentDeliveryStatusInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryStatusesResult;
import com.personal.marketnote.fulfillment.port.out.event.PublishShippingStatusChangedEventPort;
import com.personal.marketnote.fulfillment.port.out.notification.SendDeliveryFailureSlackAlertPort;
import com.personal.marketnote.fulfillment.port.out.shipping.FindShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.shipping.UpdateShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentDeliveryStatusesPort;
import com.personal.marketnote.fulfillment.utility.FulfillmentAuthenticatedCaller;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
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
    private FulfillmentAuthenticatedCaller fulfillmentAuthenticatedCaller;

    @Mock
    private GetFulfillmentDeliveryStatusesPort getDeliveryStatusesPort;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private PublishShippingStatusChangedEventPort publishShippingStatusChangedEventPort;

    @Mock
    private SendDeliveryFailureSlackAlertPort sendDeliveryFailureSlackAlertPort;

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-03T10:00:00Z"), ZoneId.of("Asia/Seoul"));

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        pollShippingStatusService = new PollShippingStatusService(
                findShippingTrackerPort,
                updateShippingTrackerPort,
                fulfillmentAuthenticatedCaller,
                getDeliveryStatusesPort,
                publishShippingStatusChangedEventPort,
                sendDeliveryFailureSlackAlertPort,
                clock,
                transactionManager
        );
    }

    @SuppressWarnings("unchecked")
    private void stubAuthenticatedCaller() {
        lenient().when(fulfillmentAuthenticatedCaller.call(any())).thenAnswer(invocation -> {
            java.util.function.Function<String, Object> operation = invocation.getArgument(0);
            return operation.apply("token123");
        });
    }

    private void stubMinCreatedAt() {
        when(findShippingTrackerPort.findMinCreatedAtOfPollingActive())
                .thenReturn(Optional.of(LocalDateTime.of(2026, 4, 8, 10, 0)));
    }

    @Test
    @DisplayName("폴링 대상이 없으면 파스토 API를 호출하지 않는다")
    void skipWhenNoActiveTrackers() {
        // given
        when(findShippingTrackerPort.findMinCreatedAtOfPollingActive()).thenReturn(Optional.empty());
        PollShippingStatusCommand command = new PollShippingStatusCommand("CUST001");

        // when
        pollShippingStatusService.pollShippingStatuses(command);

        // then
        verifyNoInteractions(fulfillmentAuthenticatedCaller);
        verifyNoInteractions(getDeliveryStatusesPort);
        verifyNoInteractions(updateShippingTrackerPort);
        verify(findShippingTrackerPort, never()).findPollingActiveChunk(anyLong(), anyInt());
    }

    @Test
    @DisplayName("파스토 상태가 SHIPPING으로 변경되면 송장번호와 함께 배송중으로 전이한다")
    void transitionToShippingWithTrackingInfo() {
        // given
        ShippingTracker tracker = createPreparingTrackerWithId(1L, 100L);
        stubMinCreatedAt();
        when(findShippingTrackerPort.findPollingActiveChunk(0L, 100)).thenReturn(List.of(tracker));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));
        stubAuthenticatedCaller();
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
        assertThat(updated.getTrackingNumberValue()).isEqualTo("INV001");
        assertThat(updated.getCarrierCodeValue()).isEqualTo("CJ");
        assertThat(updated.getLastPolledAt()).isNotNull();

        ArgumentCaptor<ShippingStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(ShippingStatusChangedEvent.class);
        verify(publishShippingStatusChangedEventPort).publish(eventCaptor.capture());
        ShippingStatusChangedEvent event = eventCaptor.getValue();
        assertThat(event.orderId()).isEqualTo(100L);
        assertThat(event.buyerId()).isEqualTo(1L);
        assertThat(event.shippingStatus()).isEqualTo("SHIPPING");
        assertThat(event.trackingNumber()).isEqualTo("INV001");
        assertThat(event.carrierCode()).isEqualTo("CJ");
    }

    @Test
    @DisplayName("파스토 상태가 SHIPPING이지만 송장번호가 없으면 송장번호 없이 배송중으로 전이한다")
    void transitionToShippingWithoutTrackingInfo() {
        // given
        ShippingTracker tracker = createPreparingTrackerWithId(1L, 100L);
        stubMinCreatedAt();
        when(findShippingTrackerPort.findPollingActiveChunk(0L, 100)).thenReturn(List.of(tracker));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));
        stubAuthenticatedCaller();
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
        stubMinCreatedAt();
        when(findShippingTrackerPort.findPollingActiveChunk(0L, 100)).thenReturn(List.of(tracker));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));
        stubAuthenticatedCaller();
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
        stubMinCreatedAt();
        when(findShippingTrackerPort.findPollingActiveChunk(0L, 100)).thenReturn(List.of(tracker));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));
        stubAuthenticatedCaller();
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
    @DisplayName("배송불가 상태 감지 시 Slack 알림을 발송한다")
    void sendSlackAlertWhenDeliveryFailed() {
        // given
        ShippingTracker tracker = createShippingTrackerWithId(1L, 100L);
        stubMinCreatedAt();
        when(findShippingTrackerPort.findPollingActiveChunk(0L, 100)).thenReturn(List.of(tracker));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));
        stubAuthenticatedCaller();
        when(getDeliveryStatusesPort.getDeliveryStatuses(any()))
                .thenReturn(createDeliveryStatusResult("100", "배송불가", "INV001", "CJ"));

        PollShippingStatusCommand command = new PollShippingStatusCommand("CUST001");

        // when
        pollShippingStatusService.pollShippingStatuses(command);

        // then
        LocalDateTime expectedOccurredAt = LocalDateTime.now(clock);
        verify(sendDeliveryFailureSlackAlertPort).sendDeliveryFailureAlert(100L, "INV001", "CJ", expectedOccurredAt);
    }

    @Test
    @DisplayName("배송불가가 아닌 상태 전이에서는 Slack 알림을 발송하지 않는다")
    void noSlackAlertWhenNotDeliveryFailed() {
        // given
        ShippingTracker tracker = createPreparingTrackerWithId(1L, 100L);
        stubMinCreatedAt();
        when(findShippingTrackerPort.findPollingActiveChunk(0L, 100)).thenReturn(List.of(tracker));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));
        stubAuthenticatedCaller();
        when(getDeliveryStatusesPort.getDeliveryStatuses(any()))
                .thenReturn(createDeliveryStatusResult("100", "집하완료", "INV001", "CJ"));

        PollShippingStatusCommand command = new PollShippingStatusCommand("CUST001");

        // when
        pollShippingStatusService.pollShippingStatuses(command);

        // then
        verifyNoInteractions(sendDeliveryFailureSlackAlertPort);
    }

    @Test
    @DisplayName("Slack 알림 발송 실패 시 다른 건 처리에 영향을 주지 않는다")
    void slackAlertFailureDoesNotBlockOtherTrackers() {
        // given
        ShippingTracker tracker1 = createShippingTrackerWithId(1L, 100L);
        ShippingTracker tracker2 = createPreparingTrackerWithId(2L, 200L);
        stubMinCreatedAt();
        when(findShippingTrackerPort.findPollingActiveChunk(0L, 100)).thenReturn(List.of(tracker1, tracker2));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker1));
        when(findShippingTrackerPort.findByOrderId(200L)).thenReturn(Optional.of(tracker2));
        stubAuthenticatedCaller();

        GetFulfillmentDeliveryStatusesResult result = GetFulfillmentDeliveryStatusesResult.of(2, List.of(
                createDeliveryStatusInfo("100", "배송불가", "INV001", "CJ"),
                createDeliveryStatusInfo("200", "집하완료", "INV002", "HANJIN")
        ));
        when(getDeliveryStatusesPort.getDeliveryStatuses(any())).thenReturn(result);

        LocalDateTime expectedOccurredAt = LocalDateTime.now(clock);
        doThrow(new RuntimeException("Slack webhook failed"))
                .when(sendDeliveryFailureSlackAlertPort)
                .sendDeliveryFailureAlert(100L, "INV001", "CJ", expectedOccurredAt);

        PollShippingStatusCommand command = new PollShippingStatusCommand("CUST001");

        // when
        pollShippingStatusService.pollShippingStatuses(command);

        // then
        verify(sendDeliveryFailureSlackAlertPort).sendDeliveryFailureAlert(100L, "INV001", "CJ", expectedOccurredAt);

        ArgumentCaptor<ShippingTracker> captor = ArgumentCaptor.forClass(ShippingTracker.class);
        verify(updateShippingTrackerPort).update(captor.capture());

        ShippingTracker updatedTracker2 = captor.getValue();
        assertThat(updatedTracker2.getOrderId()).isEqualTo(200L);
        assertThat(updatedTracker2.isShipping()).isTrue();
        assertThat(updatedTracker2.getTrackingNumberValue()).isEqualTo("INV002");
    }

    @Test
    @DisplayName("상태가 동일하면 lastPolledAt만 갱신하고 상태는 변경하지 않는다")
    void noTransitionWhenSameStatus() {
        // given
        ShippingTracker tracker = createPreparingTrackerWithId(1L, 100L);
        stubMinCreatedAt();
        when(findShippingTrackerPort.findPollingActiveChunk(0L, 100)).thenReturn(List.of(tracker));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));
        stubAuthenticatedCaller();
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
        stubMinCreatedAt();
        when(findShippingTrackerPort.findPollingActiveChunk(0L, 100)).thenReturn(List.of(tracker));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));
        stubAuthenticatedCaller();
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
        stubMinCreatedAt();
        when(findShippingTrackerPort.findPollingActiveChunk(0L, 100)).thenReturn(List.of(tracker1, tracker2));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker1));
        when(findShippingTrackerPort.findByOrderId(200L)).thenReturn(Optional.of(tracker2));
        stubAuthenticatedCaller();

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
                .buyerId(1L)
                .shippingStatus(ShippingStatus.SHIPPING)
                .pollingActive(true)
                .createdAt(LocalDateTime.of(2026, 4, 8, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 8, 10, 0))
                .build());
        stubMinCreatedAt();
        when(findShippingTrackerPort.findPollingActiveChunk(0L, 100)).thenReturn(List.of(tracker));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));
        stubAuthenticatedCaller();
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
        assertThat(updated.getTrackingNumberValue()).isEqualTo("INV001");
        assertThat(updated.getCarrierCodeValue()).isEqualTo("CJ");

        verifyNoInteractions(publishShippingStatusChangedEventPort);
    }

    @Test
    @DisplayName("여러 청크가 존재해도 파스토 API는 1회만 호출한다")
    void fasstoApiCalledOnceAcrossMultipleChunks() {
        // given - 청크 크기(100건) 이상이므로 두 번째 청크 조회 발생
        List<ShippingTracker> firstChunk = new java.util.ArrayList<>();
        for (long i = 1L; i <= 100L; i++) {
            firstChunk.add(createPreparingTrackerWithId(i, 100L + i));
        }
        List<ShippingTracker> secondChunk = List.of(
                createPreparingTrackerWithId(101L, 201L),
                createPreparingTrackerWithId(102L, 202L)
        );
        stubMinCreatedAt();
        when(findShippingTrackerPort.findPollingActiveChunk(0L, 100)).thenReturn(firstChunk);
        when(findShippingTrackerPort.findPollingActiveChunk(100L, 100)).thenReturn(secondChunk);
        lenient().when(findShippingTrackerPort.findByOrderId(anyLong())).thenAnswer(invocation -> {
            Long orderId = invocation.getArgument(0);
            return firstChunk.stream()
                    .filter(t -> t.getOrderId().equals(orderId))
                    .findFirst()
                    .or(() -> secondChunk.stream()
                            .filter(t -> t.getOrderId().equals(orderId))
                            .findFirst());
        });
        stubAuthenticatedCaller();
        when(getDeliveryStatusesPort.getDeliveryStatuses(any()))
                .thenReturn(GetFulfillmentDeliveryStatusesResult.of(0, Collections.emptyList()));

        PollShippingStatusCommand command = new PollShippingStatusCommand("CUST001");

        // when
        pollShippingStatusService.pollShippingStatuses(command);

        // then - Fassto API는 1회만 호출
        verify(fulfillmentAuthenticatedCaller, times(1)).call(any());
        verify(getDeliveryStatusesPort, times(1)).getDeliveryStatuses(any());

        // 두 청크 조회 모두 수행
        verify(findShippingTrackerPort).findPollingActiveChunk(0L, 100);
        verify(findShippingTrackerPort).findPollingActiveChunk(100L, 100);

        // 총 102건 업데이트
        verify(updateShippingTrackerPort, times(102)).update(any(ShippingTracker.class));
    }

    @Test
    @DisplayName("단일 청크 처리 시에도 파스토 API는 1회만 호출한다")
    void fasstoApiCalledOnceInSingleChunk() {
        // given
        ShippingTracker tracker = createPreparingTrackerWithId(1L, 100L);
        stubMinCreatedAt();
        when(findShippingTrackerPort.findPollingActiveChunk(0L, 100)).thenReturn(List.of(tracker));
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));
        stubAuthenticatedCaller();
        when(getDeliveryStatusesPort.getDeliveryStatuses(any()))
                .thenReturn(createDeliveryStatusResult("100", "집하완료", "INV001", "CJ"));

        PollShippingStatusCommand command = new PollShippingStatusCommand("CUST001");

        // when
        pollShippingStatusService.pollShippingStatuses(command);

        // then
        verify(fulfillmentAuthenticatedCaller, times(1)).call(any());
        verify(getDeliveryStatusesPort, times(1)).getDeliveryStatuses(any());
    }

    // --- 테스트 헬퍼 ---

    private ShippingTracker createPreparingTrackerWithId(Long id, Long orderId) {
        return ShippingTracker.from(ShippingTrackerSnapshotState.builder()
                .id(id)
                .orderId(orderId)
                .buyerId(1L)
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
                .buyerId(1L)
                .trackingNumber(TrackingNumber.of("INV001"))
                .carrierCode(CarrierCode.of("CJ"))
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
