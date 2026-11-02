package com.personal.marketnote.fulfillment.service;

import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistration;
import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistrationSnapshotState;
import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentWorkStatus;
import com.personal.marketnote.fulfillment.port.in.command.GetFulfillmentWorkStatusCommand;
import com.personal.marketnote.fulfillment.port.in.result.GetFulfillmentWorkStatusResult;
import com.personal.marketnote.fulfillment.port.out.delivery.FindFulfillmentDeliveryRegistrationPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetFulfillmentWorkStatusUseCase 테스트")
class GetFulfillmentWorkStatusUseCaseTest {
    @InjectMocks
    private GetFulfillmentWorkStatusService getFulfillmentWorkStatusService;

    @Mock
    private FindFulfillmentDeliveryRegistrationPort findFulfillmentDeliveryRegistrationPort;

    private FulfillmentDeliveryRegistration buildRegistration(Long orderId, FulfillmentWorkStatus workStatus) {
        return FulfillmentDeliveryRegistration.from(
                FulfillmentDeliveryRegistrationSnapshotState.builder()
                        .id(1L)
                        .orderId(orderId)
                        .workStatus(workStatus)
                        .createdAt(LocalDateTime.of(2026, 4, 6, 10, 0))
                        .build()
        );
    }

    @Nested
    @DisplayName("getWorkStatus")
    class GetWorkStatus {
        @Test
        @DisplayName("출고 등록이 존재하면 해당 작업 상태를 반환한다")
        void shouldReturnWorkStatusWhenDeliveryRegistrationExists() {
            // given
            Long orderId = 100L;
            GetFulfillmentWorkStatusCommand command = new GetFulfillmentWorkStatusCommand(orderId);
            FulfillmentDeliveryRegistration registration = buildRegistration(orderId, FulfillmentWorkStatus.REGISTERED);
            when(findFulfillmentDeliveryRegistrationPort.findByOrderId(orderId))
                    .thenReturn(Optional.of(registration));

            // when
            GetFulfillmentWorkStatusResult result = getFulfillmentWorkStatusService.getWorkStatus(command);

            // then
            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.workStatus()).isEqualTo(FulfillmentWorkStatus.REGISTERED.name());
            verify(findFulfillmentDeliveryRegistrationPort).findByOrderId(orderId);
        }

        @Test
        @DisplayName("출고 등록이 존재하지 않으면 NOT_REGISTERED 상태를 반환한다")
        void shouldReturnNotRegisteredWhenDeliveryRegistrationNotFound() {
            // given
            Long orderId = 200L;
            GetFulfillmentWorkStatusCommand command = new GetFulfillmentWorkStatusCommand(orderId);
            when(findFulfillmentDeliveryRegistrationPort.findByOrderId(orderId))
                    .thenReturn(Optional.empty());

            // when
            GetFulfillmentWorkStatusResult result = getFulfillmentWorkStatusService.getWorkStatus(command);

            // then
            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.workStatus()).isEqualTo(FulfillmentWorkStatus.NOT_REGISTERED.name());
            verify(findFulfillmentDeliveryRegistrationPort).findByOrderId(orderId);
        }

        @Test
        @DisplayName("피킹 완료 상태의 출고 등록이 존재하면 PICKED 상태를 반환한다")
        void shouldReturnPickedStatusWhenRegistrationHasPickedStatus() {
            // given
            Long orderId = 300L;
            GetFulfillmentWorkStatusCommand command = new GetFulfillmentWorkStatusCommand(orderId);
            FulfillmentDeliveryRegistration registration = buildRegistration(orderId, FulfillmentWorkStatus.PICKED);
            when(findFulfillmentDeliveryRegistrationPort.findByOrderId(orderId))
                    .thenReturn(Optional.of(registration));

            // when
            GetFulfillmentWorkStatusResult result = getFulfillmentWorkStatusService.getWorkStatus(command);

            // then
            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.workStatus()).isEqualTo(FulfillmentWorkStatus.PICKED.name());
            verify(findFulfillmentDeliveryRegistrationPort).findByOrderId(orderId);
        }

        @Test
        @DisplayName("출고 완료 상태의 출고 등록이 존재하면 RELEASED 상태를 반환한다")
        void shouldReturnReleasedStatusWhenRegistrationHasReleasedStatus() {
            // given
            Long orderId = 400L;
            GetFulfillmentWorkStatusCommand command = new GetFulfillmentWorkStatusCommand(orderId);
            FulfillmentDeliveryRegistration registration = buildRegistration(orderId, FulfillmentWorkStatus.RELEASED);
            when(findFulfillmentDeliveryRegistrationPort.findByOrderId(orderId))
                    .thenReturn(Optional.of(registration));

            // when
            GetFulfillmentWorkStatusResult result = getFulfillmentWorkStatusService.getWorkStatus(command);

            // then
            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.workStatus()).isEqualTo(FulfillmentWorkStatus.RELEASED.name());
            verify(findFulfillmentDeliveryRegistrationPort).findByOrderId(orderId);
        }
    }
}
