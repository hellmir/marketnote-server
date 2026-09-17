package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;
import com.personal.marketnote.commerce.port.out.returntracker.SaveReturnTrackerPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReturnTrackerPersistenceUseCaseTest {
    @Mock
    private SaveReturnTrackerPort saveReturnTrackerPort;

    @InjectMocks
    private ReturnTrackerPersistenceService returnTrackerPersistenceService;

    @Nested
    @DisplayName("ReturnTracker 생성 및 저장")
    class SaveReturnTrackerTest {

        @Test
        @DisplayName("orderId와 returnSlipNumber로 ReturnTracker를 생성하여 저장한다")
        void shouldCreateAndSaveReturnTracker() {
            // given
            Long orderId = 1L;
            String returnSlipNumber = "RS-2026041100001";
            when(saveReturnTrackerPort.save(any(ReturnTracker.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            returnTrackerPersistenceService.saveReturnTracker(orderId, returnSlipNumber);

            // then
            ArgumentCaptor<ReturnTracker> captor = ArgumentCaptor.forClass(ReturnTracker.class);
            verify(saveReturnTrackerPort).save(captor.capture());

            ReturnTracker saved = captor.getValue();
            assertThat(saved.getOrderId()).isEqualTo(orderId);
            assertThat(saved.getReturnSlipNumber()).isEqualTo(returnSlipNumber);
        }

        @Test
        @DisplayName("생성된 ReturnTracker의 검수 상태는 PENDING이다")
        void shouldCreateReturnTrackerWithPendingInspectionStatus() {
            // given
            Long orderId = 1L;
            String returnSlipNumber = "RS-2026041100001";
            when(saveReturnTrackerPort.save(any(ReturnTracker.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            returnTrackerPersistenceService.saveReturnTracker(orderId, returnSlipNumber);

            // then
            ArgumentCaptor<ReturnTracker> captor = ArgumentCaptor.forClass(ReturnTracker.class);
            verify(saveReturnTrackerPort).save(captor.capture());

            ReturnTracker saved = captor.getValue();
            assertThat(saved.isInspectionPending()).isTrue();
        }

        @Test
        @DisplayName("생성된 ReturnTracker의 환불 상태는 PENDING이다")
        void shouldCreateReturnTrackerWithPendingRefundStatus() {
            // given
            Long orderId = 1L;
            String returnSlipNumber = "RS-2026041100001";
            when(saveReturnTrackerPort.save(any(ReturnTracker.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            returnTrackerPersistenceService.saveReturnTracker(orderId, returnSlipNumber);

            // then
            ArgumentCaptor<ReturnTracker> captor = ArgumentCaptor.forClass(ReturnTracker.class);
            verify(saveReturnTrackerPort).save(captor.capture());

            ReturnTracker saved = captor.getValue();
            assertThat(saved.isRefundPending()).isTrue();
        }

        @Test
        @DisplayName("saveReturnTrackerPort.save가 정확히 1회 호출된다")
        void shouldCallSavePortExactlyOnce() {
            // given
            Long orderId = 1L;
            String returnSlipNumber = "RS-2026041100001";
            when(saveReturnTrackerPort.save(any(ReturnTracker.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            returnTrackerPersistenceService.saveReturnTracker(orderId, returnSlipNumber);

            // then
            verify(saveReturnTrackerPort).save(any(ReturnTracker.class));
        }
    }
}
