package com.personal.marketnote.reward.domain.attendance;

import com.personal.marketnote.common.domain.calendar.Month;
import com.personal.marketnote.common.domain.calendar.Year;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class UserAttendance {
    private Long id;
    private Long userId;
    private Year year;
    private Month month;
    private LocalDateTime createdAt;
    private RewardQuantity totalRewardQuantity;
    private List<UserAttendanceHistory> histories;

    public long getTotalRewardQuantityValue() {
        return totalRewardQuantity.getValue();
    }

    public static UserAttendance from(UserAttendanceCreateState state) {
        return UserAttendance.builder()
                .userId(state.getUserId())
                .year(state.getYear())
                .month(state.getMonth())
                .totalRewardQuantity(state.getTotalRewardQuantity())
                .histories(state.getHistories())
                .build();
    }

    public static UserAttendance from(UserAttendanceSnapshotState state) {
        return UserAttendance.builder()
                .id(state.getId())
                .userId(state.getUserId())
                .year(state.getYear())
                .month(state.getMonth())
                .createdAt(state.getCreatedAt())
                .totalRewardQuantity(state.getTotalRewardQuantity())
                .histories(state.getHistories())
                .build();
    }

    public UserAttendance withAddedReward(long rewardQuantity) {
        RewardQuantity added = RewardQuantity.of(rewardQuantity);
        return UserAttendance.builder()
                .id(id)
                .userId(userId)
                .year(year)
                .month(month)
                .createdAt(createdAt)
                .totalRewardQuantity(totalRewardQuantity.add(added))
                .histories(histories)
                .build();
    }
}

