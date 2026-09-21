package com.personal.marketnote.reward.domain.attendance;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class UserAttendanceHistory {
    private Long id;
    private Long userAttendanceId;
    private Short attendancePolicyId;
    private AttendanceRewardType rewardType;
    private RewardQuantity rewardQuantity;
    private ContinuousPeriod continuousPeriod;

    public long getRewardQuantityValue() {
        return rewardQuantity.getValue();
    }

    public short getContinuousPeriodValue() {
        return continuousPeriod.getValue();
    }
    private Boolean rewardYn;
    private LocalDate attendedDate;
    private LocalDateTime attendedAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static UserAttendanceHistory from(UserAttendanceHistoryCreateState state) {
        return UserAttendanceHistory.builder()
                .userAttendanceId(state.getUserAttendanceId())
                .attendancePolicyId(state.getAttendancePolicyId())
                .rewardType(state.getRewardType())
                .rewardQuantity(state.getRewardQuantity())
                .continuousPeriod(state.getContinuousPeriod())
                .rewardYn(state.getRewardYn())
                .attendedDate(state.getAttendedDate())
                .attendedAt(state.getAttendedAt())
                .build();
    }

    public static UserAttendanceHistory from(UserAttendanceHistorySnapshotState state) {
        return UserAttendanceHistory.builder()
                .id(state.getId())
                .userAttendanceId(state.getUserAttendanceId())
                .attendancePolicyId(state.getAttendancePolicyId())
                .rewardType(state.getRewardType())
                .rewardQuantity(state.getRewardQuantity())
                .continuousPeriod(state.getContinuousPeriod())
                .rewardYn(state.getRewardYn())
                .attendedDate(state.getAttendedDate())
                .attendedAt(state.getAttendedAt())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }
}

