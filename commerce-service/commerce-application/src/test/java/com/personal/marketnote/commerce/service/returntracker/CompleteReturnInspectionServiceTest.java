package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.returntracker.ReturnInspectionStatus;
import com.personal.marketnote.commerce.domain.returntracker.ReturnRefundStatus;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTrackerSnapshotState;
import com.personal.marketnote.commerce.port.out.event.PublishReturnTrackerEventPort;
import com.personal.marketnote.commerce.port.out.returntracker.UpdateReturnTrackerPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompleteReturnInspectionService 테스트")
class CompleteReturnInspectionServiceTest {

    @InjectMocks
    private CompleteReturnInspectionService service;

    @Mock
    private UpdateReturnTrackerPort updateReturnTrackerPort;

    @Mock
    private PublishReturnTrackerEventPort publishReturnTrackerEventPort;

    @Test
    @DisplayName("검수 PASSED 시 ReturnTracker를 업데이트하고 이벤트를 발행한다")
    void shouldPassInspectionAndPublishEvent() {
        ReturnTracker tracker = ReturnTracker.from(ReturnTrackerSnapshotState.builder()
                .id(1L)
                .orderId(100L)
                .returnSlipNumber("RS-001")
                .inspectionStatus(ReturnInspectionStatus.PENDING)
                .refundStatus(ReturnRefundStatus.PENDING)
                .build());

        LocalDateTime now = LocalDateTime.of(2026, 4, 9, 19, 0);

        service.completeInspection(tracker, now);

        assertThat(tracker.isInspectionPassed()).isTrue();
        assertThat(tracker.getInspectedAt()).isEqualTo(now);
        verify(updateReturnTrackerPort).update(tracker);
        verify(publishReturnTrackerEventPort).publishReturnInspectionCompletedEvent(100L);
    }
}
