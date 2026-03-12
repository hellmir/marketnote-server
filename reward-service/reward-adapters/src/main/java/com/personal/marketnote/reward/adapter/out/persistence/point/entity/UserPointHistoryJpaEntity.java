package com.personal.marketnote.reward.adapter.out.persistence.point.entity;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.point.UserPointHistory;
import com.personal.marketnote.reward.domain.point.UserPointHistorySnapshotState;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_point_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_point_history_source",
                columnNames = {"user_id", "source_type", "source_id", "reason"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class UserPointHistoryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "reflected_yn", nullable = false)
    private Boolean isReflected;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 15)
    private UserPointSourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "reason")
    private String reason;

    @Column(name = "accumulated_at", nullable = false)
    private LocalDateTime accumulatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static UserPointHistoryJpaEntity from(UserPointHistory history) {
        if (FormatValidator.hasNoValue(history)) {
            return null;
        }

        return UserPointHistoryJpaEntity.builder()
                .id(history.getId())
                .userId(history.getUserId())
                .amount(history.getAmount())
                .isReflected(history.getIsReflected())
                .sourceType(history.getSourceType())
                .sourceId(history.getSourceId())
                .reason(history.getReason())
                .accumulatedAt(history.getAccumulatedAt())
                .createdAt(history.getCreatedAt())
                .build();
    }

    public UserPointHistory toDomain() {
        return UserPointHistory.from(
                UserPointHistorySnapshotState.builder()
                        .id(id)
                        .userId(userId)
                        .amount(amount)
                        .isReflected(isReflected)
                        .sourceType(sourceType)
                        .sourceId(sourceId)
                        .reason(reason)
                        .accumulatedAt(accumulatedAt)
                        .createdAt(createdAt)
                        .build()
        );
    }
}
