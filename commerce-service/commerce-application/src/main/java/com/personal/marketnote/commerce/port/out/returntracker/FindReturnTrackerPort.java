package com.personal.marketnote.commerce.port.out.returntracker;

import com.personal.marketnote.commerce.domain.returntracker.ReturnInspectionStatus;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;

import java.util.List;
import java.util.Optional;

public interface FindReturnTrackerPort {
    Optional<ReturnTracker> findByOrderId(Long orderId);

    List<ReturnTracker> findByInspectionStatus(ReturnInspectionStatus inspectionStatus);
}
