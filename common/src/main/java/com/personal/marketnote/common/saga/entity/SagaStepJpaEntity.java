package com.personal.marketnote.common.saga.entity;

import com.personal.marketnote.common.saga.SagaStepStatus;
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
        name = "saga_step",
        indexes = @Index(
                name = "idx_saga_step_instance_id_index",
                columnList = "saga_instance_id, step_index"
        )
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SagaStepJpaEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "saga_instance_id", nullable = false)
    private Long sagaInstanceId;

    @Column(name = "step_name", nullable = false, length = 100)
    private String stepName;

    @Column(name = "step_index", nullable = false)
    private int stepIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SagaStepStatus status;

    @Column(name = "request", nullable = false, columnDefinition = "TEXT")
    private String request;

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Column(name = "compensation_request", columnDefinition = "TEXT")
    private String compensationRequest;

    @Column(name = "compensation_response", columnDefinition = "TEXT")
    private String compensationResponse;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    private SagaStepJpaEntity(Long sagaInstanceId, String stepName, int stepIndex,
                               SagaStepStatus status, String request) {
        this.sagaInstanceId = sagaInstanceId;
        this.stepName = stepName;
        this.stepIndex = stepIndex;
        this.status = status;
        this.request = request;
    }

    public static SagaStepJpaEntity from(com.personal.marketnote.common.saga.SagaStep step) {
        return new SagaStepJpaEntity(
                step.getSagaInstanceId(),
                step.getStepName(),
                step.getStepIndex(),
                step.getStatus(),
                step.getRequest()
        );
    }

    public void updateFrom(com.personal.marketnote.common.saga.SagaStep step) {
        this.status = step.getStatus();
        this.response = step.getResponse();
        this.compensationRequest = step.getCompensationRequest();
        this.compensationResponse = step.getCompensationResponse();
    }
}
