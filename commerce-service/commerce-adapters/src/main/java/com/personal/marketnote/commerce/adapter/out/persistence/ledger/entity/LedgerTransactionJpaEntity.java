package com.personal.marketnote.commerce.adapter.out.persistence.ledger.entity;

import com.personal.marketnote.commerce.domain.ledger.LedgerTransaction;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_transaction",
        uniqueConstraints = @UniqueConstraint(columnNames = "idempotency_key"))
@DynamicInsert
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class LedgerTransactionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 31)
    private LedgerTransactionType transactionType;

    @Column(name = "target_type", nullable = false, length = 15)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "description")
    private String description;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static LedgerTransactionJpaEntity from(LedgerTransaction transaction) {
        return LedgerTransactionJpaEntity.builder()
                .transactionType(transaction.getTransactionType())
                .targetType(transaction.getTargetType())
                .targetId(transaction.getTargetId())
                .description(transaction.getDescription())
                .idempotencyKey(transaction.getIdempotencyKey())
                .build();
    }
}
