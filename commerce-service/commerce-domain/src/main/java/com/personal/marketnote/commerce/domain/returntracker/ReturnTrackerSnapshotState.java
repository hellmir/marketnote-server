package com.personal.marketnote.commerce.domain.returntracker;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ReturnTrackerSnapshotState {
    private Long id;
    private Long orderId;
    private String returnSlipNumber;
    private ReturnInspectionStatus inspectionStatus;
    private ReturnRefundStatus refundStatus;
    private LocalDateTime inspectedAt;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
