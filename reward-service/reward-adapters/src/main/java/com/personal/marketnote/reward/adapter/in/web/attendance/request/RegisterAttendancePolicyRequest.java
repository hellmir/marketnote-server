package com.personal.marketnote.reward.adapter.in.web.attendance.request;

import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class RegisterAttendancePolicyRequest {
    @NotNull
    private Short continuousPeriod;
    @NotNull
    private AttendanceRewardType rewardType;
    @NotNull
    private Long rewardQuantity;
    private LocalDate attendenceDate;
}

