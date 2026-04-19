package com.personal.marketnote.reward.domain.attendance;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class AttendancePolicy {
    private Short id;
    private short continuousPeriod;
    private AttendanceRewardType rewardType;
    private long rewardQuantity;
    private LocalDate attendenceDate;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public AttendancePolicy withStatus(EntityStatus status) {
        return AttendancePolicy.builder()
                .id(id)
                .continuousPeriod(continuousPeriod)
                .rewardType(rewardType)
                .rewardQuantity(rewardQuantity)
                .attendenceDate(attendenceDate)
                .status(status)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();
    }

    public boolean isActive() {
        return status.isActive();
    }

    public boolean isInactive() {
        return status.isInactive();
    }

    public void delete() {
        status = EntityStatus.INACTIVE;
    }

    public static AttendancePolicy from(AttendancePolicyCreateState state) {
        return AttendancePolicy.builder()
                .continuousPeriod(state.getContinuousPeriod())
                .rewardType(state.getRewardType())
                .rewardQuantity(state.getRewardQuantity())
                .attendenceDate(state.getAttendenceDate())
                .build();
    }

    public static AttendancePolicy from(AttendancePolicySnapshotState state) {
        return AttendancePolicy.builder()
                .id(state.getId())
                .continuousPeriod(state.getContinuousPeriod())
                .rewardType(state.getRewardType())
                .rewardQuantity(state.getRewardQuantity())
                .attendenceDate(state.getAttendenceDate())
                .status(state.getStatus())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }
}
