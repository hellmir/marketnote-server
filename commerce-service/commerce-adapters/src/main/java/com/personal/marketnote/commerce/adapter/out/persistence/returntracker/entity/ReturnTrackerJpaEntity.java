package com.personal.marketnote.commerce.adapter.out.persistence.returntracker.entity;

import com.personal.marketnote.commerce.domain.returntracker.ReturnInspectionStatus;
import com.personal.marketnote.commerce.domain.returntracker.ReturnRefundStatus;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "return_tracker",
        indexes = {
                @Index(name = "idx_return_tracker_inspection_status", columnList = "inspection_status")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_return_tracker_order_id", columnNames = "order_id"))
@DynamicInsert
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ReturnTrackerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "return_slip_number", length = 100)
    private String returnSlipNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_status", nullable = false, length = 20)
    private ReturnInspectionStatus inspectionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false, length = 20)
    private ReturnRefundStatus refundStatus;

    @Column(name = "inspected_at")
    private LocalDateTime inspectedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    public static ReturnTrackerJpaEntity from(ReturnTracker domain) {
        return ReturnTrackerJpaEntity.builder()
                .id(domain.getId())
                .orderId(domain.getOrderId())
                .returnSlipNumber(domain.getReturnSlipNumber())
                .inspectionStatus(domain.getInspectionStatus())
                .refundStatus(domain.getRefundStatus())
                .inspectedAt(domain.getInspectedAt())
                .refundedAt(domain.getRefundedAt())
                .createdAt(domain.getCreatedAt())
                .modifiedAt(domain.getModifiedAt())
                .build();
    }
}
