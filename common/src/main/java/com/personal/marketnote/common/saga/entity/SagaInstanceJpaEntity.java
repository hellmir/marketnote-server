package com.personal.marketnote.common.saga.entity;

import com.personal.marketnote.common.saga.SagaStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(
        name = "saga_instance",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_saga_instance_saga_id",
                columnNames = {"saga_id"}
        ),
        indexes = {
                @Index(name = "idx_saga_instance_type_status", columnList = "saga_type, status"),
                @Index(name = "idx_saga_instance_status_modified_at", columnList = "status, modified_at")
        }
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SagaInstanceJpaEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "saga_id", nullable = false, length = 100)
    private String sagaId;

    @Column(name = "saga_type", nullable = false, length = 100)
    private String sagaType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SagaStatus status;

    @Column(name = "current_step_index", nullable = false)
    private int currentStepIndex;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private SagaInstanceJpaEntity(String sagaId, String sagaType, SagaStatus status,
                                  int currentStepIndex, String payload) {
        this.sagaId = sagaId;
        this.sagaType = sagaType;
        this.status = status;
        this.currentStepIndex = currentStepIndex;
        this.payload = payload;
    }

    public static SagaInstanceJpaEntity from(com.personal.marketnote.common.saga.SagaInstance instance) {
        SagaInstanceJpaEntity entity = new SagaInstanceJpaEntity(
                instance.getSagaId(),
                instance.getSagaType(),
                instance.getStatus(),
                instance.getCurrentStepIndex(),
                instance.getPayload()
        );
        entity.completedAt = instance.getCompletedAt();
        return entity;
    }

    public void updateFrom(com.personal.marketnote.common.saga.SagaInstance instance) {
        this.status = instance.getStatus();
        this.currentStepIndex = instance.getCurrentStepIndex();
        this.completedAt = instance.getCompletedAt();
    }
}
