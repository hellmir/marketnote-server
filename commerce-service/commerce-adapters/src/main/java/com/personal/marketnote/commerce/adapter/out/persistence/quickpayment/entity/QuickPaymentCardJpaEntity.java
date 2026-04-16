package com.personal.marketnote.commerce.adapter.out.persistence.quickpayment.entity;

import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;
import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "quick_payment_card",
        indexes = @Index(name = "idx_quick_payment_card_user_id", columnList = "user_id"),
        uniqueConstraints = @UniqueConstraint(name = "uk_quick_payment_card_batch_key", columnNames = "batch_key"))
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class QuickPaymentCardJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "batch_key", nullable = false)
    private String batchKey;

    @Column(name = "group_id", length = 12)
    private String groupId;

    @Column(name = "card_code", nullable = false, length = 4)
    private String cardCode;

    @Column(name = "card_name", nullable = false, length = 20)
    private String cardName;

    @Column(name = "masked_card_number", length = 20)
    private String maskedCardNumber;

    @Column(name = "card_bin_type_01", nullable = false, length = 1)
    private String cardBinType01;

    @Column(name = "card_bin_type_02", nullable = false, length = 1)
    private String cardBinType02;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private EntityStatus status;

    public static QuickPaymentCardJpaEntity from(QuickPaymentCard domain) {
        return QuickPaymentCardJpaEntity.builder()
                .userId(domain.getUserId())
                .batchKey(domain.getBatchKey())
                .groupId(domain.getGroupId())
                .cardCode(domain.getCardCode())
                .cardName(domain.getCardName())
                .maskedCardNumber(domain.getMaskedCardNumber())
                .cardBinType01(domain.getCardBinType01())
                .cardBinType02(domain.getCardBinType02())
                .status(domain.getStatus())
                .build();
    }

    public void markInactive() {
        this.status = EntityStatus.INACTIVE;
    }
}
