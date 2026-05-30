package com.personal.marketnote.commerce.domain.returntracker;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ReturnTracker {
    private Long id;
    private Long orderId;
    private String returnSlipNumber;
    private ReturnInspectionStatus inspectionStatus;
    private ReturnRefundStatus refundStatus;
    private LocalDateTime inspectedAt;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static ReturnTracker from(ReturnTrackerCreateState state) {
        if (FormatValidator.hasNoValue(state.getOrderId())) {
            throw new ReturnTrackerOrderIdNoValueException();
        }
        return ReturnTracker.builder()
                .orderId(state.getOrderId())
                .returnSlipNumber(state.getReturnSlipNumber())
                .inspectionStatus(ReturnInspectionStatus.PENDING)
                .refundStatus(ReturnRefundStatus.PENDING)
                .build();
    }

    public static ReturnTracker from(ReturnTrackerSnapshotState state) {
        return ReturnTracker.builder()
                .id(state.getId())
                .orderId(state.getOrderId())
                .returnSlipNumber(state.getReturnSlipNumber())
                .inspectionStatus(state.getInspectionStatus())
                .refundStatus(state.getRefundStatus())
                .inspectedAt(state.getInspectedAt())
                .refundedAt(state.getRefundedAt())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }

    public boolean isInspectionPending() {
        return inspectionStatus.isPending();
    }

    public boolean isInspectionPassed() {
        return inspectionStatus.isPassed();
    }

    public boolean isInspectionFailed() {
        return inspectionStatus.isFailed();
    }

    public boolean isInspectionOnHold() {
        return inspectionStatus.isOnHold();
    }

    public boolean isRefundPending() {
        return refundStatus.isPending();
    }

    public boolean isRefundCompleted() {
        return refundStatus.isCompleted();
    }

    public boolean isRefundFailed() {
        return refundStatus.isFailed();
    }

    public void passInspection(LocalDateTime now) {
        if (!inspectionStatus.canTransitionTo(ReturnInspectionStatus.PASSED)) {
            throw new InvalidReturnInspectionStatusTransitionException(inspectionStatus, ReturnInspectionStatus.PASSED);
        }
        this.inspectionStatus = ReturnInspectionStatus.PASSED;
        this.inspectedAt = now;
    }

    public void failInspection(LocalDateTime now) {
        if (!inspectionStatus.canTransitionTo(ReturnInspectionStatus.FAILED)) {
            throw new InvalidReturnInspectionStatusTransitionException(inspectionStatus, ReturnInspectionStatus.FAILED);
        }
        this.inspectionStatus = ReturnInspectionStatus.FAILED;
        this.inspectedAt = now;
    }

    public void holdInspection() {
        if (!inspectionStatus.canTransitionTo(ReturnInspectionStatus.ON_HOLD)) {
            throw new InvalidReturnInspectionStatusTransitionException(inspectionStatus, ReturnInspectionStatus.ON_HOLD);
        }
        this.inspectionStatus = ReturnInspectionStatus.ON_HOLD;
    }

    public void completeRefund(LocalDateTime now) {
        if (!refundStatus.canTransitionTo(ReturnRefundStatus.COMPLETED)) {
            throw new InvalidReturnRefundStatusTransitionException(refundStatus, ReturnRefundStatus.COMPLETED);
        }
        this.refundStatus = ReturnRefundStatus.COMPLETED;
        this.refundedAt = now;
    }

    public void failRefund() {
        if (!refundStatus.canTransitionTo(ReturnRefundStatus.FAILED)) {
            throw new InvalidReturnRefundStatusTransitionException(refundStatus, ReturnRefundStatus.FAILED);
        }
        this.refundStatus = ReturnRefundStatus.FAILED;
    }

    public void retryRefund() {
        if (!refundStatus.canTransitionTo(ReturnRefundStatus.PENDING)) {
            throw new InvalidReturnRefundStatusTransitionException(refundStatus, ReturnRefundStatus.PENDING);
        }
        this.refundStatus = ReturnRefundStatus.PENDING;
        this.refundedAt = null;
    }
}
