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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PollReturnInspectionUseCaseTest {
    @Mock
    private FindReturnTrackerPort findReturnTrackerPort;
    @Mock
    private UpdateReturnTrackerPort updateReturnTrackerPort;
    @Mock
    private GetReturnInspectionResultPort getReturnInspectionResultPort;
    @Mock
    private CompleteReturnInspectionService completeReturnInspectionService;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-17T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private PollReturnInspectionService pollReturnInspectionService;

    @BeforeEach
    void setUp() {
        pollReturnInspectionService = new PollReturnInspectionService(
                findReturnTrackerPort, updateReturnTrackerPort,
                getReturnInspectionResultPort, completeReturnInspectionService, clock);
    }

    @Nested
    @DisplayName("PENDING ReturnTracker 조회")
    class PendingTrackerLookupTest {

        @Test
        @DisplayName("PENDING 상태 ReturnTracker가 없으면 조회만 하고 종료한다")
        void shouldReturnEarlyWhenNoPendingTrackers() {
            // given
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of());

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verifyNoInteractions(getReturnInspectionResultPort);
            verifyNoInteractions(updateReturnTrackerPort);
            verifyNoInteractions(completeReturnInspectionService);
        }

        @Test
        @DisplayName("PENDING ReturnTracker 중 returnSlipNumber가 없는 건은 스킵한다")
        void shouldSkipTrackersWithoutSlipNumber() {
            // given
            ReturnTracker trackerWithoutSlip = createTrackerWithoutSlipNumber(1L);
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(trackerWithoutSlip));

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verifyNoInteractions(getReturnInspectionResultPort);
        }
    }

    @Nested
    @DisplayName("풀필먼트 검수 결과 조회 실패")
    class InspectionResultFetchFailureTest {

        @Test
        @DisplayName("풀필먼트 검수 결과 조회 시 예외가 발생하면 로깅 후 종료한다")
        void shouldLogAndReturnWhenFetchFails() {
            // given
            ReturnTracker tracker = createTracker(1L, "RS-001");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker));
            when(getReturnInspectionResultPort.getReturnGodDetail("RS-001"))
                    .thenThrow(new RuntimeException("API 호출 실패"));

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verifyNoInteractions(updateReturnTrackerPort);
            verifyNoInteractions(completeReturnInspectionService);
        }

        @Test
        @DisplayName("풀필먼트 검수 결과가 null이면 종료한다")
        void shouldReturnWhenResultIsNull() {
            // given
            ReturnTracker tracker = createTracker(1L, "RS-001");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker));
            when(getReturnInspectionResultPort.getReturnGodDetail("RS-001"))
                    .thenReturn(null);

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verifyNoInteractions(updateReturnTrackerPort);
            verifyNoInteractions(completeReturnInspectionService);
        }

        @Test
        @DisplayName("풀필먼트 검수 결과의 returnGodInfos가 null이면 종료한다")
        void shouldReturnWhenReturnGodInfosIsNull() {
            // given
            ReturnTracker tracker = createTracker(1L, "RS-001");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker));
            when(getReturnInspectionResultPort.getReturnGodDetail("RS-001"))
                    .thenReturn(new ReturnInspectionResult(0, null));

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verifyNoInteractions(updateReturnTrackerPort);
            verifyNoInteractions(completeReturnInspectionService);
        }
    }

    @Nested
    @DisplayName("검수 결과 처리 - 검수 통과")
    class InspectionPassedTest {

        @Test
        @DisplayName("상품 전체가 검수 통과(01)이면 completeReturnInspectionService를 호출한다")
        void shouldCallCompleteInspectionWhenAllProductsPassed() {
            // given
            ReturnTracker tracker = createTracker(1L, "RS-001");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker));

            ReturnInspectionResult result = new ReturnInspectionResult(1, List.of(
                    createResultItem("1", "01", "01")
            ));
            when(getReturnInspectionResultPort.getReturnGodDetail("RS-001")).thenReturn(result);

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verify(completeReturnInspectionService).completeInspection(eq(tracker), any());
            verifyNoInteractions(updateReturnTrackerPort);
        }
    }

    @Nested
    @DisplayName("검수 결과 처리 - 검수 실패/보류")
    class InspectionFailedOrOnHoldTest {

        @Test
        @DisplayName("상품 중 검수 실패(02)가 있으면 tracker를 FAILED로 업데이트한다")
        void shouldUpdateTrackerToFailedWhenProductInspectionFailed() {
            // given
            ReturnTracker tracker = createTracker(1L, "RS-001");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker));

            ReturnInspectionResult result = new ReturnInspectionResult(1, List.of(
                    createResultItem("1", "01", "02")
            ));
            when(getReturnInspectionResultPort.getReturnGodDetail("RS-001")).thenReturn(result);

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            assertThat(tracker.isInspectionFailed()).isTrue();
            verify(updateReturnTrackerPort).update(tracker);
            verifyNoInteractions(completeReturnInspectionService);
        }

        @Test
        @DisplayName("상품 중 검수 보류(03)가 있으면 tracker를 ON_HOLD로 업데이트한다")
        void shouldUpdateTrackerToOnHoldWhenProductInspectionOnHold() {
            // given
            ReturnTracker tracker = createTracker(1L, "RS-001");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker));

            ReturnInspectionResult result = new ReturnInspectionResult(1, List.of(
                    createResultItem("1", "01", "03")
            ));
            when(getReturnInspectionResultPort.getReturnGodDetail("RS-001")).thenReturn(result);

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            assertThat(tracker.isInspectionOnHold()).isTrue();
            verify(updateReturnTrackerPort).update(tracker);
            verifyNoInteractions(completeReturnInspectionService);
        }

        @Test
        @DisplayName("검수 실패(02)와 보류(03)가 혼재하면 FAILED가 우선한다")
        void shouldPrioritizeFailedOverOnHold() {
            // given
            ReturnTracker tracker = createTracker(1L, "RS-001");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker));

            ReturnInspectionResult result = new ReturnInspectionResult(1, List.of(
                    createResultItem("1", "03", "02")
            ));
            when(getReturnInspectionResultPort.getReturnGodDetail("RS-001")).thenReturn(result);

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            assertThat(tracker.isInspectionFailed()).isTrue();
            verify(updateReturnTrackerPort).update(tracker);
        }
    }

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCaseTest {

        @Test
        @DisplayName("검수 결과 orderNumber와 매칭되는 ReturnTracker가 없으면 스킵한다")
        void shouldSkipWhenNoMatchingTracker() {
            // given
            ReturnTracker tracker = createTracker(1L, "RS-001");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker));

            ReturnInspectionResult result = new ReturnInspectionResult(1, List.of(
                    createResultItem("999", "01")
            ));
            when(getReturnInspectionResultPort.getReturnGodDetail("RS-001")).thenReturn(result);

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verifyNoInteractions(updateReturnTrackerPort);
            verifyNoInteractions(completeReturnInspectionService);
        }

        @Test
        @DisplayName("상품 목록이 비어있으면 상태 업데이트 없이 스킵한다")
        void shouldSkipWhenProductsEmpty() {
            // given
            ReturnTracker tracker = createTracker(1L, "RS-001");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker));

            ReturnInspectionResult result = new ReturnInspectionResult(1, List.of(
                    new ReturnInspectionResultItem("1", "SLIP-001", List.of())
            ));
            when(getReturnInspectionResultPort.getReturnGodDetail("RS-001")).thenReturn(result);

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verifyNoInteractions(updateReturnTrackerPort);
            verifyNoInteractions(completeReturnInspectionService);
        }

        @Test
        @DisplayName("분류 불가능한 상태 조합이면 상태 업데이트 없이 스킵한다")
        void shouldSkipWhenUnclassifiableStatus() {
            // given
            ReturnTracker tracker = createTracker(1L, "RS-001");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker));

            ReturnInspectionResult result = new ReturnInspectionResult(1, List.of(
                    createResultItem("1", "99")
            ));
            when(getReturnInspectionResultPort.getReturnGodDetail("RS-001")).thenReturn(result);

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            verifyNoInteractions(updateReturnTrackerPort);
            verifyNoInteractions(completeReturnInspectionService);
        }

        @Test
        @DisplayName("여러 검수 결과 중 하나에서 예외가 발생해도 나머지는 계속 처리한다")
        void shouldContinueProcessingWhenOneResultFails() {
            // given
            ReturnTracker tracker1 = createTracker(1L, "RS-001");
            ReturnTracker tracker2 = createTracker(2L, "RS-002");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker1, tracker2));

            ReturnInspectionResultItem failingItem = createResultItem("1", "01");
            ReturnInspectionResultItem successItem = createResultItem("2", "02");
            ReturnInspectionResult result = new ReturnInspectionResult(2, List.of(failingItem, successItem));
            when(getReturnInspectionResultPort.getReturnGodDetail("RS-001,RS-002")).thenReturn(result);

            doThrow(new RuntimeException("완료 처리 실패"))
                    .when(completeReturnInspectionService).completeInspection(eq(tracker1), any());

            // when
            assertThatCode(() -> pollReturnInspectionService.pollPendingInspections())
                    .doesNotThrowAnyException();

            // then
            verify(updateReturnTrackerPort).update(tracker2);
        }

        @Test
        @DisplayName("중복 orderId가 있으면 먼저 등록된 tracker를 사용한다")
        void shouldUseFirstTrackerWhenDuplicateOrderIdExists() {
            // given
            ReturnTracker tracker1 = createTracker(1L, "RS-001");
            ReturnTracker tracker2 = createTracker(1L, "RS-002");
            when(findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING))
                    .thenReturn(List.of(tracker1, tracker2));

            ReturnInspectionResult result = new ReturnInspectionResult(1, List.of(
                    createResultItem("1", "02")
            ));
            when(getReturnInspectionResultPort.getReturnGodDetail("RS-001,RS-002")).thenReturn(result);

            // when
            pollReturnInspectionService.pollPendingInspections();

            // then
            assertThat(tracker1.isInspectionFailed()).isTrue();
            verify(updateReturnTrackerPort).update(tracker1);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private ReturnTracker createTracker(Long orderId, String returnSlipNumber) {
        return ReturnTracker.from(ReturnTrackerSnapshotState.builder()
                .id(orderId)
                .orderId(orderId)
                .returnSlipNumber(returnSlipNumber)
                .inspectionStatus(ReturnInspectionStatus.PENDING)
                .refundStatus(ReturnRefundStatus.PENDING)
                .build());
    }

    private ReturnTracker createTrackerWithoutSlipNumber(Long orderId) {
        return ReturnTracker.from(ReturnTrackerSnapshotState.builder()
                .id(orderId)
                .orderId(orderId)
                .inspectionStatus(ReturnInspectionStatus.PENDING)
                .refundStatus(ReturnRefundStatus.PENDING)
                .build());
    }

    private ReturnInspectionResultItem createResultItem(String orderNumber, String... statuses) {
        List<ReturnInspectionGoodsItem> goods = java.util.Arrays.stream(statuses)
                .map(status -> new ReturnInspectionGoodsItem("PROD-001", "테스트 상품", status, "상태명"))
                .toList();
        return new ReturnInspectionResultItem(orderNumber, "SLIP-001", goods);
    }
}
