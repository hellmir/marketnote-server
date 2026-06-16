package com.personal.marketnote.fulfillment.service.shipping;

import com.personal.marketnote.fulfillment.domain.exception.ShippingTrackerNotFoundException;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingStatus;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTrackerSnapshotState;
import com.personal.marketnote.fulfillment.port.in.command.GetShippingStatusCommand;
import com.personal.marketnote.fulfillment.port.in.result.GetShippingStatusResult;
import com.personal.marketnote.fulfillment.port.out.shipping.FindShippingTrackerPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetShippingStatusUseCase 테스트")
class GetShippingStatusUseCaseTest {

    @InjectMocks
    private GetShippingStatusService getShippingStatusService;

    @Mock
    private FindShippingTrackerPort findShippingTrackerPort;

    @Test
    @DisplayName("PREPARING 상태이면 isCancellable=true이고 모든 필드를 반환한다")
    void preparingIsCancellable() {
        // given
        LocalDateTime polledAt = LocalDateTime.of(2026, 4, 9, 10, 0);
        ShippingTracker tracker = createTracker(100L, ShippingStatus.PREPARING, null, null, polledAt);
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));

        // when
        GetShippingStatusResult result = getShippingStatusService.getShippingStatus(
                new GetShippingStatusCommand(100L)
        );

        // then
        assertThat(result.orderId()).isEqualTo(100L);
        assertThat(result.shippingStatus()).isEqualTo("PREPARING");
        assertThat(result.cancellable()).isTrue();
        assertThat(result.trackingNumber()).isNull();
        assertThat(result.carrierCode()).isNull();
        assertThat(result.lastPolledAt()).isEqualTo(polledAt);
    }

    @ParameterizedTest
    @EnumSource(value = ShippingStatus.class, names = {"SHIPPING", "DELIVERED", "CANCELLED", "RETURN_SHIPPING", "RETURN_DELIVERED", "DELIVERY_FAILED"})
    @DisplayName("PREPARING이 아니면 isCancellable=false이다")
    void nonPreparingIsNotCancellable(ShippingStatus status) {
        // given
        ShippingTracker tracker = createTracker(100L, status, "INV001", "CJ", null);
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));

        // when
        GetShippingStatusResult result = getShippingStatusService.getShippingStatus(
                new GetShippingStatusCommand(100L)
        );

        // then
        assertThat(result.cancellable()).isFalse();
        assertThat(result.shippingStatus()).isEqualTo(status.name());
    }

    @Test
    @DisplayName("SHIPPING 상태이면 송장번호와 택배사 코드를 반환한다")
    void shippingReturnsTrackingInfo() {
        // given
        ShippingTracker tracker = createTracker(100L, ShippingStatus.SHIPPING, "INV001", "CJ", null);
        when(findShippingTrackerPort.findByOrderId(100L)).thenReturn(Optional.of(tracker));

        // when
        GetShippingStatusResult result = getShippingStatusService.getShippingStatus(
                new GetShippingStatusCommand(100L)
        );

        // then
        assertThat(result.trackingNumber()).isEqualTo("INV001");
        assertThat(result.carrierCode()).isEqualTo("CJ");
    }

    @Test
    @DisplayName("ShippingTracker가 존재하지 않으면 ShippingTrackerNotFoundException이 발생한다")
    void notFoundThrowsException() {
        // given
        when(findShippingTrackerPort.findByOrderId(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getShippingStatusService.getShippingStatus(
                new GetShippingStatusCommand(999L)
        )).isInstanceOf(ShippingTrackerNotFoundException.class);
    }

    private ShippingTracker createTracker(
            Long orderId,
            ShippingStatus status,
            String trackingNumber,
            String carrierCode,
            LocalDateTime lastPolledAt
    ) {
        return ShippingTracker.from(ShippingTrackerSnapshotState.builder()
                .id(1L)
                .orderId(orderId)
                .trackingNumber(trackingNumber)
                .carrierCode(carrierCode)
                .shippingStatus(status)
                .pollingActive(!status.isTerminal())
                .lastPolledAt(lastPolledAt)
                .createdAt(LocalDateTime.of(2026, 4, 8, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 9, 10, 0))
                .build());
    }
}
