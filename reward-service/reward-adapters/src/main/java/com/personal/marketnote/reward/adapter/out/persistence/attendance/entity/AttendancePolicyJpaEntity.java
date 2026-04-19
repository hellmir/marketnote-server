package com.personal.marketnote.reward.adapter.out.persistence.attendance.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicySnapshotState;
import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;

@Entity
@Table(name = "attendance_policy")
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class AttendancePolicyJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(name = "continuous_period", nullable = false)
    private short continuousPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 31)
    private AttendanceRewardType rewardType;

    @Column(name = "reward_quantity", nullable = false)
    private long rewardQuantity;

    @Column(name = "attendence_date")
    private LocalDate attendenceDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EntityStatus status;

    private Short orderNum;

    public static AttendancePolicyJpaEntity from(AttendancePolicy policy) {
        return AttendancePolicyJpaEntity.builder()
                .continuousPeriod(policy.getContinuousPeriod())
                .rewardType(policy.getRewardType())
                .rewardQuantity(policy.getRewardQuantity())
                .attendenceDate(policy.getAttendenceDate())
                .status(EntityStatus.ACTIVE)
                .build();
    }

    public AttendancePolicy toDomain() {
        return AttendancePolicy.from(
                AttendancePolicySnapshotState.builder()
                        .id(id)
                        .continuousPeriod(continuousPeriod)
                        .rewardType(rewardType)
                        .rewardQuantity(rewardQuantity)
                        .attendenceDate(attendenceDate)
                        .status(status)
                        .createdAt(getCreatedAt())
                        .modifiedAt(getModifiedAt())
                        .build()
        );
    }

    public void updateFrom(AttendancePolicy policy) {
        updateActivation(policy);
        continuousPeriod = policy.getContinuousPeriod();
        rewardType = policy.getRewardType();
        rewardQuantity = policy.getRewardQuantity();
        attendenceDate = policy.getAttendenceDate();
    }

    private void updateActivation(AttendancePolicy policy) {
        if (policy.isActive()) {
            activate();
            return;
        }

        if (policy.isInactive()) {
            deactivate();
            return;
        }

        hide();
    }

    private void activate() {
        status = EntityStatus.ACTIVE;
    }

    private void deactivate() {
        status = EntityStatus.INACTIVE;
    }

    private void hide() {
        status = EntityStatus.UNEXPOSED;
    }

    public void setIdToOrderNum() {
        orderNum = id;
    }
}
