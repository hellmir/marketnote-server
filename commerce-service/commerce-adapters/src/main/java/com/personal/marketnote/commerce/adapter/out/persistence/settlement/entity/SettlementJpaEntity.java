package com.personal.marketnote.commerce.adapter.out.persistence.settlement.entity;

import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.domain.settlement.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "settlement")
@DynamicInsert
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class SettlementJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "total_allocated_amount", nullable = false)
    private Long totalAllocatedAmount;

    @Column(name = "pg_fee_amount", nullable = false)
    private Long pgFeeAmount;

    @Column(name = "platform_fee_amount", nullable = false)
    private Long platformFeeAmount;

    @Column(name = "seller_payout_amount", nullable = false)
    private Long sellerPayoutAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private SettlementStatus status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    public static SettlementJpaEntity from(Settlement settlement) {
        return SettlementJpaEntity.builder()
                .id(settlement.getId())
                .sellerId(settlement.getSellerId())
                .year(settlement.getYear())
                .month(settlement.getMonth())
                .totalAllocatedAmount(settlement.getTotalAllocatedAmount())
                .pgFeeAmount(settlement.getPgFeeAmount())
                .platformFeeAmount(settlement.getPlatformFeeAmount())
                .sellerPayoutAmount(settlement.getSellerPayoutAmount())
                .status(settlement.getStatus())
                .version(settlement.getVersion())
                .createdAt(settlement.getCreatedAt())
                .modifiedAt(settlement.getModifiedAt())
                .build();
    }
}
