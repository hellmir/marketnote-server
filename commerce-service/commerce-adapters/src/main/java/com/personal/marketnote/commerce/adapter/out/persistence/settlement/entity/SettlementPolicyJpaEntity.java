package com.personal.marketnote.commerce.adapter.out.persistence.settlement.entity;

import com.personal.marketnote.commerce.domain.settlement.SettlementCycle;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicy;
import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_policy")
@DynamicInsert
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class SettlementPolicyJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "pg_fee_rate", nullable = false)
    private Integer pgFeeRate;

    @Column(name = "platform_fee_rate", nullable = false)
    private Integer platformFeeRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_cycle", nullable = false, length = 20)
    private SettlementCycle settlementCycle;

    @Column(name = "min_payout_amount", nullable = false)
    private Long minPayoutAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private EntityStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    public static SettlementPolicyJpaEntity from(SettlementPolicy policy) {
        return SettlementPolicyJpaEntity.builder()
                .id(policy.getId())
                .sellerId(policy.getSellerId())
                .pgFeeRate(policy.getPgFeeRate())
                .platformFeeRate(policy.getPlatformFeeRate())
                .settlementCycle(policy.getSettlementCycle())
                .minPayoutAmount(policy.getMinPayoutAmount())
                .status(policy.getStatus())
                .createdAt(policy.getCreatedAt())
                .modifiedAt(policy.getModifiedAt())
                .build();
    }
}
