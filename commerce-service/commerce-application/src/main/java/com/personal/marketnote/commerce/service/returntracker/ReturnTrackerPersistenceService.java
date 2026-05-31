package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTrackerCreateState;
import com.personal.marketnote.commerce.port.out.returntracker.SaveReturnTrackerPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnTrackerPersistenceService {
    private final SaveReturnTrackerPort saveReturnTrackerPort;

    @Transactional(isolation = READ_COMMITTED)
    public void saveReturnTracker(Long orderId, String returnSlipNumber) {
        ReturnTracker returnTracker = ReturnTracker.from(
                ReturnTrackerCreateState.builder()
                        .orderId(orderId)
                        .returnSlipNumber(returnSlipNumber)
                        .build()
        );

        saveReturnTrackerPort.save(returnTracker);

        log.info("ReturnTracker 생성 완료 - orderId: {}, returnSlipNumber: {}",
                orderId, returnSlipNumber);
    }
}
