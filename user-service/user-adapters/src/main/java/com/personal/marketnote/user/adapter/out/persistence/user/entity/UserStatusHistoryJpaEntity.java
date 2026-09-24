package com.personal.marketnote.user.adapter.out.persistence.user.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.user.domain.user.UserStatusAction;
import com.personal.marketnote.user.domain.user.UserStatusActor;
import com.personal.marketnote.user.domain.user.UserStatusHistory;
import com.personal.marketnote.user.domain.user.UserStatusHistorySnapshotState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_status_histories", indexes = {
        @Index(name = "idx_user_status_histories_user_id", columnList = "user_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class UserStatusHistoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_action", nullable = false, length = 20)
    private UserStatusAction statusAction;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "deactivated_until")
    private LocalDateTime deactivatedUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor", nullable = false, length = 20)
    private UserStatusActor actor;

    @Column(name = "created_by")
    private Long createdBy;

    public static UserStatusHistoryJpaEntity from(UserStatusHistory history) {
        return UserStatusHistoryJpaEntity.builder()
                .id(history.getId())
                .userId(history.getUserId())
                .statusAction(history.getStatusAction())
                .reason(history.getReason())
                .deactivatedUntil(history.getDeactivatedUntil())
                .actor(history.getActor())
                .createdBy(history.getCreatedBy())
                .build();
    }

    public UserStatusHistory toDomain() {
        return UserStatusHistory.from(
                UserStatusHistorySnapshotState.builder()
                        .id(id)
                        .userId(userId)
                        .statusAction(statusAction)
                        .reason(reason)
                        .deactivatedUntil(deactivatedUntil)
                        .actor(actor)
                        .createdBy(createdBy)
                        .createdAt(getCreatedAt())
                        .build()
        );
    }
}
