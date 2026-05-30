package com.personal.marketnote.commerce.domain.returntracker;

public class InvalidReturnInspectionStatusTransitionException extends IllegalStateException {
    public InvalidReturnInspectionStatusTransitionException(ReturnInspectionStatus currentStatus, ReturnInspectionStatus targetStatus) {
        super("검수 상태 전이 불가: " + currentStatus + " → " + targetStatus);
    }
}
