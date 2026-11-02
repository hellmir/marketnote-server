package com.personal.marketnote.reward.adapter.in.web.attendance.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class RegisterAttendanceRequest {
    @NotNull
    private LocalDateTime attendedAt;
}

