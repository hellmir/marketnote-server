package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.returntracker.ReturnInspectionStatus;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;
import com.personal.marketnote.commerce.port.in.usecase.returntracker.PollReturnInspectionUseCase;
import com.personal.marketnote.commerce.port.out.fulfillment.GetReturnInspectionResultPort;
import com.personal.marketnote.commerce.port.out.fulfillment.ReturnInspectionResult;
import com.personal.marketnote.commerce.port.out.fulfillment.ReturnInspectionResult.ReturnInspectionGoodsItem;
import com.personal.marketnote.commerce.port.out.fulfillment.ReturnInspectionResult.ReturnInspectionResultItem;
import com.personal.marketnote.commerce.port.out.returntracker.FindReturnTrackerPort;
import com.personal.marketnote.commerce.port.out.returntracker.UpdateReturnTrackerPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class PollReturnInspectionService implements PollReturnInspectionUseCase {
    private static final String INSPECTION_PASSED = "01";
    private static final String INSPECTION_FAILED = "02";
    private static final String INSPECTION_ON_HOLD = "03";

    private final FindReturnTrackerPort findReturnTrackerPort;
    private final UpdateReturnTrackerPort updateReturnTrackerPort;
    private final GetReturnInspectionResultPort getReturnInspectionResultPort;
    private final CompleteReturnInspectionService completeReturnInspectionService;
    private final Clock clock;

    @Override
    public void pollPendingInspections() {
        List<ReturnTracker> pendingTrackers = findReturnTrackerPort.findByInspectionStatus(ReturnInspectionStatus.PENDING);
        if (pendingTrackers.isEmpty()) {
            return;
        }

        List<ReturnTracker> trackersWithSlipNumber = filterTrackersWithSlipNumber(pendingTrackers);
        if (trackersWithSlipNumber.isEmpty()) {
            return;
        }

        String returnSlipNumbers = buildReturnSlipNumbers(trackersWithSlipNumber);
        ReturnInspectionResult response = fetchInspectionResults(returnSlipNumbers);
        if (FormatValidator.hasNoValue(response) || FormatValidator.hasNoValue(response.returnGodInfos())) {
            return;
        }

        Map<String, ReturnTracker> trackerByOrderId = buildTrackerByOrderIdMap(trackersWithSlipNumber);
        processInspectionResults(response.returnGodInfos(), trackerByOrderId);
    }

    private List<ReturnTracker> filterTrackersWithSlipNumber(List<ReturnTracker> trackers) {
        List<ReturnTracker> withSlipNumber = trackers.stream()
                .filter(tracker -> FormatValidator.hasValue(tracker.getReturnSlipNumber()))
                .toList();

        int skippedCount = trackers.size() - withSlipNumber.size();
        if (skippedCount > 0) {
            log.warn("반품 검수 폴링: returnSlipNumber 없는 ReturnTracker {} 건 스킵", skippedCount);
        }
        return withSlipNumber;
    }

    private String buildReturnSlipNumbers(List<ReturnTracker> trackers) {
        return trackers.stream()
                .map(ReturnTracker::getReturnSlipNumber)
                .collect(Collectors.joining(","));
    }

    private ReturnInspectionResult fetchInspectionResults(String returnSlipNumbers) {
        try {
            return getReturnInspectionResultPort.getReturnGodDetail(returnSlipNumbers);
        } catch (Exception e) {
            log.error("반품 검수 상태 조회 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    private Map<String, ReturnTracker> buildTrackerByOrderIdMap(List<ReturnTracker> trackers) {
        return trackers.stream()
                .collect(Collectors.toMap(
                        tracker -> String.valueOf(tracker.getOrderId()),
                        tracker -> tracker,
                        (existing, duplicate) -> {
                            log.warn("반품 검수 폴링: 중복 orderId 발견 - orderId: {}", existing.getOrderId());
                            return existing;
                        }
                ));
    }

    private void processInspectionResults(
            List<ReturnInspectionResultItem> resultItems,
            Map<String, ReturnTracker> trackerByOrderId
    ) {
        for (ReturnInspectionResultItem resultItem : resultItems) {
            try {
                processInspectionResult(resultItem, trackerByOrderId);
            } catch (Exception e) {
                log.error("반품 검수 상태 업데이트 실패 - orderNumber: {}, error: {}",
                        resultItem.orderNumber(), e.getMessage(), e);
            }
        }
    }

    private void processInspectionResult(
            ReturnInspectionResultItem resultItem,
            Map<String, ReturnTracker> trackerByOrderId
    ) {
        ReturnTracker tracker = trackerByOrderId.get(resultItem.orderNumber());
        if (FormatValidator.hasNoValue(tracker)) {
            return;
        }

        String overallStatus = resolveOverallInspectionStatus(resultItem.products());
        if (FormatValidator.hasNoValue(overallStatus)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now(clock);

        if (INSPECTION_PASSED.equals(overallStatus)) {
            completeReturnInspectionService.completeInspection(tracker, now);
            log.info("반품 검수 완료 이벤트 발행 - orderId: {}", tracker.getOrderId());
            return;
        }

        applyInspectionStatus(tracker, overallStatus, now);
        updateReturnTrackerPort.update(tracker);
        log.info("반품 검수 상태 업데이트 - orderId: {}, status: {}", tracker.getOrderId(), overallStatus);
    }

    private String resolveOverallInspectionStatus(List<ReturnInspectionGoodsItem> products) {
        if (FormatValidator.hasNoValue(products) || products.isEmpty()) {
            return null;
        }

        boolean hasFailed = products.stream()
                .anyMatch(goods -> INSPECTION_FAILED.equals(goods.returnProductCheckStatus()));
        if (hasFailed) {
            return INSPECTION_FAILED;
        }

        boolean hasOnHold = products.stream()
                .anyMatch(goods -> INSPECTION_ON_HOLD.equals(goods.returnProductCheckStatus()));
        if (hasOnHold) {
            return INSPECTION_ON_HOLD;
        }

        boolean allPassed = products.stream()
                .allMatch(goods -> INSPECTION_PASSED.equals(goods.returnProductCheckStatus()));
        if (allPassed) {
            return INSPECTION_PASSED;
        }

        log.warn("반품 검수 폴링: 분류 불가능한 상태 조합 - statuses: {}",
                products.stream().map(ReturnInspectionGoodsItem::returnProductCheckStatus).toList());
        return null;
    }

    private void applyInspectionStatus(ReturnTracker tracker, String status, LocalDateTime now) {
        if (INSPECTION_FAILED.equals(status)) {
            tracker.failInspection(now);
            return;
        }
        if (INSPECTION_ON_HOLD.equals(status)) {
            tracker.holdInspection();
        }
    }
}
