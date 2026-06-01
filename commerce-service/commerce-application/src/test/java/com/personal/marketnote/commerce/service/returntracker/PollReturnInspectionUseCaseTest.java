package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.returntracker.ReturnInspectionStatus;
import com.personal.marketnote.commerce.domain.returntracker.ReturnRefundStatus;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTrackerSnapshotState;
import com.personal.marketnote.commerce.port.out.fulfillment.GetReturnInspectionResultPort;
import com.personal.marketnote.commerce.port.out.fulfillment.ReturnInspectionResult;
import com.personal.marketnote.commerce.port.out.fulfillment.ReturnInspectionResult.ReturnInspectionGoodsItem;
import com.personal.marketnote.commerce.port.out.fulfillment.ReturnInspectionResult.ReturnInspectionResultItem;
import com.personal.marketnote.commerce.port.out.returntracker.FindReturnTrackerPort;
import com.personal.marketnote.commerce.port.out.returntracker.UpdateReturnTrackerPort;
import com.personal.marketnote.common.exception.FulfillmentServiceRequestFailedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PollReturnInspectionUseCaseTest {
    @Mock
    private FindReturnTrackerPort findReturnTrackerPort;
    @Mock
    private UpdateReturnTrackerPort updateReturnTrackerPort;
    @Mock
    private GetReturnInspectionResultPort getReturnInspectionResultPort;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-04-09T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private PollReturnInspectionService pollReturnInspectionService;

    @Nested
    @DisplayName("PENDING 트래커 없음")
    class NoPendingTrackersTest {

        @Test
        @DisplayName("PENDING 상태의 ReturnTracker가 없으면 외부 API를 호출하지 않는다")
        void pollPendingInspections_noPendingTrackers_doesNotCallExternalApi() {
            // given
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of());

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verifyNoInteractions(getReturnInspectionResultPort);
            verifyNoInteractions(updateReturnTrackerPort);
        }
    }

    @Nested
    @DisplayName("검수 상태 업데이트")
    class InspectionStatusUpdateTest {

        @Test
        @DisplayName("모든 상품이 검수 정상(01)이면 ReturnTracker를 PASSED로 업데이트한다")
        void pollPendingInspections_allPassed_updatesStatusToPassed() {
            // given
            ReturnTracker tracker = createPendingTracker(1L, "SLIP001");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker));

            ReturnInspectionResult result = new ReturnInspectionResult(1, List.of(
                    new ReturnInspectionResultItem("1", "SLIP001", List.of(
                            new ReturnInspectionGoodsItem("PROD001", "상품A", "01", "정상"),
                            new ReturnInspectionGoodsItem("PROD002", "상품B", "01", "정상")
                    ))
            ));
            when(getReturnInspectionResultPort.getReturnGodDetail("SLIP001")).thenReturn(result);

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verify(updateReturnTrackerPort).update(argThat(ReturnTracker::isInspectionPassed));
        }

        @Test
        @DisplayName("상품 중 하나라도 검수 불량(02)이면 ReturnTracker를 FAILED로 업데이트한다")
        void pollPendingInspections_anyFailed_updatesStatusToFailed() {
            // given
            ReturnTracker tracker = createPendingTracker(2L, "SLIP002");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker));

            ReturnInspectionResult result = new ReturnInspectionResult(1, List.of(
                    new ReturnInspectionResultItem("2", "SLIP002", List.of(
                            new ReturnInspectionGoodsItem("PROD001", "상품A", "01", "정상"),
                            new ReturnInspectionGoodsItem("PROD002", "상품B", "02", "불량")
                    ))
            ));
            when(getReturnInspectionResultPort.getReturnGodDetail("SLIP002")).thenReturn(result);

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verify(updateReturnTrackerPort).update(argThat(ReturnTracker::isInspectionFailed));
        }

        @Test
        @DisplayName("상품 중 하나라도 검수 보류(03)이면 ReturnTracker를 ON_HOLD로 업데이트한다")
        void pollPendingInspections_anyOnHold_updatesStatusToOnHold() {
            // given
            ReturnTracker tracker = createPendingTracker(3L, "SLIP003");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker));

            ReturnInspectionResult result = new ReturnInspectionResult(1, List.of(
                    new ReturnInspectionResultItem("3", "SLIP003", List.of(
                            new ReturnInspectionGoodsItem("PROD001", "상품A", "01", "정상"),
                            new ReturnInspectionGoodsItem("PROD002", "상품B", "03", "보류")
                    ))
            ));
            when(getReturnInspectionResultPort.getReturnGodDetail("SLIP003")).thenReturn(result);

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verify(updateReturnTrackerPort).update(argThat(ReturnTracker::isInspectionOnHold));
        }

        @Test
        @DisplayName("불량(02)과 보류(03)가 동시에 있으면 FAILED가 우선한다")
        void pollPendingInspections_failedAndOnHold_failedTakesPriority() {
            // given
            ReturnTracker tracker = createPendingTracker(4L, "SLIP004");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker));

            ReturnInspectionResult result = new ReturnInspectionResult(1, List.of(
                    new ReturnInspectionResultItem("4", "SLIP004", List.of(
                            new ReturnInspectionGoodsItem("PROD001", "상품A", "02", "불량"),
                            new ReturnInspectionGoodsItem("PROD002", "상품B", "03", "보류")
                    ))
            ));
            when(getReturnInspectionResultPort.getReturnGodDetail("SLIP004")).thenReturn(result);

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verify(updateReturnTrackerPort).update(argThat(ReturnTracker::isInspectionFailed));
        }
    }

    @Nested
    @DisplayName("외부 API 실패 처리")
    class ExternalApiFailureTest {

        @Test
        @DisplayName("풀필먼트 API 호출 실패 시 예외를 삼키고 ReturnTracker를 업데이트하지 않는다")
        void pollPendingInspections_apiFails_doesNotUpdateTrackers() {
            // given
            ReturnTracker tracker = createPendingTracker(1L, "SLIP001");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker));
            when(getReturnInspectionResultPort.getReturnGodDetail("SLIP001"))
                    .thenThrow(new FulfillmentServiceRequestFailedException(new IOException("연결 실패")));

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verifyNoInteractions(updateReturnTrackerPort);
        }
    }

    @Nested
    @DisplayName("returnSlipNumber 필터링")
    class SlipNumberFilteringTest {

        @Test
        @DisplayName("returnSlipNumber가 없는 ReturnTracker는 스킵하고 나머지만 폴링한다")
        void pollPendingInspections_someWithoutSlipNumber_onlyPollsWithSlipNumber() {
            // given
            ReturnTracker withSlip = createPendingTracker(1L, "SLIP001");
            ReturnTracker withoutSlip = createPendingTracker(2L, null);
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(withSlip, withoutSlip));

            ReturnInspectionResult result = new ReturnInspectionResult(1, List.of(
                    new ReturnInspectionResultItem("1", "SLIP001", List.of(
                            new ReturnInspectionGoodsItem("PROD001", "상품A", "01", "정상")
                    ))
            ));
            when(getReturnInspectionResultPort.getReturnGodDetail("SLIP001")).thenReturn(result);

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verify(getReturnInspectionResultPort).getReturnGodDetail("SLIP001");
            verify(updateReturnTrackerPort).update(argThat(ReturnTracker::isInspectionPassed));
        }

        @Test
        @DisplayName("모든 ReturnTracker에 returnSlipNumber가 없으면 외부 API를 호출하지 않는다")
        void pollPendingInspections_allWithoutSlipNumber_doesNotCallExternalApi() {
            // given
            ReturnTracker withoutSlip = createPendingTracker(1L, null);
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(withoutSlip));

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verifyNoInteractions(getReturnInspectionResultPort);
            verifyNoInteractions(updateReturnTrackerPort);
        }
    }

    @Nested
    @DisplayName("배치 조회")
    class BatchQueryTest {

        @Test
        @DisplayName("여러 PENDING ReturnTracker의 returnSlipNumber를 콤마로 연결하여 단일 API 호출한다")
        void pollPendingInspections_multipleTrackers_batchQuery() {
            // given
            ReturnTracker tracker1 = createPendingTracker(1L, "SLIP001");
            ReturnTracker tracker2 = createPendingTracker(2L, "SLIP002");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker1, tracker2));

            ReturnInspectionResult result = new ReturnInspectionResult(2, List.of(
                    new ReturnInspectionResultItem("1", "SLIP001", List.of(
                            new ReturnInspectionGoodsItem("PROD001", "상품A", "01", "정상")
                    )),
                    new ReturnInspectionResultItem("2", "SLIP002", List.of(
                            new ReturnInspectionGoodsItem("PROD002", "상품B", "02", "불량")
                    ))
            ));
            when(getReturnInspectionResultPort.getReturnGodDetail("SLIP001,SLIP002")).thenReturn(result);

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verify(getReturnInspectionResultPort).getReturnGodDetail("SLIP001,SLIP002");
            verify(updateReturnTrackerPort, times(2)).update(any(ReturnTracker.class));
        }
    }

    private ReturnTracker createPendingTracker(Long orderId, String returnSlipNumber) {
        return ReturnTracker.from(ReturnTrackerSnapshotState.builder()
                .id(orderId)
                .orderId(orderId)
                .returnSlipNumber(returnSlipNumber)
                .inspectionStatus(ReturnInspectionStatus.PENDING)
                .refundStatus(ReturnRefundStatus.PENDING)
                .build());
    }
}
