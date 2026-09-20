package com.personal.marketnote.user.adapter.out.persistence.user.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.user.domain.user.UserPenaltyHistory;
import com.personal.marketnote.user.domain.user.UserPenaltyHistorySnapshotState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_penalty_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class UserPenaltyHistoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "previous_count", nullable = false)
    private int previousCount;

    @Column(name = "current_count", nullable = false)
    private int currentCount;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    public static UserPenaltyHistoryJpaEntity from(UserPenaltyHistory history) {
        return UserPenaltyHistoryJpaEntity.builder()
                .id(history.getId())
                .userId(history.getUserId())
                .previousCount(history.getPreviousCount())
                .currentCount(history.getCurrentCount())
                .reason(history.getReason())
                .createdBy(history.getCreatedBy())
                .build();
    }

    public UserPenaltyHistory toDomain() {
        return UserPenaltyHistory.from(
                UserPenaltyHistorySnapshotState.builder()
                        .id(id)
                        .userId(userId)
                        .previousCount(previousCount)
                        .currentCount(currentCount)
                        .reason(reason)
                        .createdBy(createdBy)
                        .createdAt(getCreatedAt())
                        .build()
        );
    }
}
