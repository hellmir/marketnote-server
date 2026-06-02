package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;
import com.personal.marketnote.commerce.port.out.event.PublishReturnTrackerEventPort;
import com.personal.marketnote.commerce.port.out.returntracker.UpdateReturnTrackerPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompleteReturnInspectionService {

    private final UpdateReturnTrackerPort updateReturnTrackerPort;
    private final PublishReturnTrackerEventPort publishReturnTrackerEventPort;

    @Transactional(isolation = READ_COMMITTED)
    public void completeInspection(ReturnTracker tracker, LocalDateTime now) {
        tracker.passInspection(now);
        updateReturnTrackerPort.update(tracker);
        publishReturnTrackerEventPort.publishReturnInspectionCompletedEvent(tracker.getOrderId());

        log.info("반품 검수 완료 이벤트 발행 - orderId: {}", tracker.getOrderId());
    }
}
