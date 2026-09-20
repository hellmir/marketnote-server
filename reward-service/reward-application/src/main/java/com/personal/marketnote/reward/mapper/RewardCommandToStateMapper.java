package com.personal.marketnote.reward.mapper;

import com.personal.marketnote.common.domain.calendar.Month;
import com.personal.marketnote.common.domain.calendar.Year;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicyCreateState;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceCreateState;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistoryCreateState;
import com.personal.marketnote.reward.domain.offerwall.OfferwallMapperCreateState;
import com.personal.marketnote.reward.domain.point.PointAmount;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointCreateState;
import com.personal.marketnote.reward.domain.point.UserPointHistoryCreateState;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.port.in.command.attendance.RegisterAttendanceCommand;
import com.personal.marketnote.reward.port.in.command.attendance.RegisterAttendancePolicyCommand;
import com.personal.marketnote.reward.port.in.command.offerwall.RegisterOfferwallRewardCommand;
import com.personal.marketnote.reward.port.in.command.point.ConfirmPendingPointCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingPointCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.command.point.RegisterUserPointCommand;

import java.time.LocalDateTime;

public class RewardCommandToStateMapper {
    public static OfferwallMapperCreateState mapToOfferwallMapperCreateState(
            RegisterOfferwallRewardCommand command, boolean isSuccess
    ) {
        return OfferwallMapperCreateState.builder()
                .offerwallType(command.offerwallType())
                .rewardKey(command.rewardKey())
                .userKey(command.userKey())
                .userDeviceType(command.userDeviceType())
                .campaignKey(command.campaignKey())
                .campaignType(command.campaignType())
                .campaignName(command.campaignName())
                .quantity(command.quantity())
                .signedValue(command.signedValue())
                .appKey(command.appKey())
                .appName(command.appName())
                .adid(command.adid())
                .idfa(command.idfa())
                .isSuccess(isSuccess)
                .attendedAt(command.attendedAt())
                .build();
    }

    public static UserPointCreateState mapToUserPointCreateState(RegisterUserPointCommand command) {
        return UserPointCreateState.builder()
                .userId(command.userId())
                .userKey(command.userKey())
                .amount(PointAmount.zero())
                .addExpectedAmount(PointAmount.zero())
                .expireExpectedAmount(PointAmount.zero())
                .build();
    }

    public static UserPointHistoryCreateState mapToUserPointHistoryCreateState(
            RegisterUserPointCommand command,
            LocalDateTime accumulatedAt
    ) {
        return UserPointHistoryCreateState.builder()
                .userId(command.userId())
                .changeType(UserPointChangeType.ACCRUAL)
                .amount(PointAmount.zero())
                .isReflected(Boolean.TRUE)
                .sourceType(UserPointSourceType.USER)
                .sourceId(command.userId())
                .reason("회원 가입")
                .accumulatedAt(accumulatedAt)
                .build();
    }

    public static UserPointHistoryCreateState mapToUserPointHistoryCreateState(
            ModifyUserPointCommand command,
            Long userId,
            LocalDateTime accumulatedAt
    ) {
        return UserPointHistoryCreateState.builder()
                .userId(userId)
                .changeType(command.changeType())
                .amount(PointAmount.of(command.amount()))
                .isReflected(Boolean.TRUE)
                .sourceType(command.sourceType())
                .sourceId(command.sourceId())
                .reason(command.reason())
                .accumulatedAt(accumulatedAt)
                .build();
    }

    public static UserPointHistoryCreateState mapToPendingPointHistoryCreateState(
            ModifyPendingPointCommand command,
            Long userId,
            LocalDateTime accumulatedAt
    ) {
        return UserPointHistoryCreateState.builder()
                .userId(userId)
                .changeType(command.changeType())
                .amount(PointAmount.of(command.amount()))
                .isReflected(Boolean.FALSE)
                .sourceType(command.sourceType())
                .sourceId(command.sourceId())
                .reason(command.reason())
                .accumulatedAt(accumulatedAt)
                .build();
    }

    public static UserPointHistoryCreateState mapToConfirmedPointHistoryCreateState(
            ConfirmPendingPointCommand command,
            Long totalAmount,
            Long userId,
            LocalDateTime accumulatedAt
    ) {
        return UserPointHistoryCreateState.builder()
                .userId(userId)
                .changeType(UserPointChangeType.ACCRUAL)
                .amount(PointAmount.of(totalAmount))
                .isReflected(Boolean.TRUE)
                .sourceType(command.sourceType())
                .sourceId(command.sourceId())
                .reason(command.reason())
                .accumulatedAt(accumulatedAt)
                .build();
    }

    public static UserAttendanceHistoryCreateState mapToUserAttendanceHistoryCreateState(
            RegisterAttendanceCommand command,
            AttendancePolicy attendancePolicy,
            short continuousPeriod,
            Long userAttendanceId
    ) {
        return UserAttendanceHistoryCreateState.builder()
                .userAttendanceId(userAttendanceId)
                .attendancePolicyId(attendancePolicy.getId())
                .rewardType(attendancePolicy.getRewardType())
                .rewardQuantity(attendancePolicy.getRewardQuantity())
                .continuousPeriod(continuousPeriod)
                .rewardYn(Boolean.TRUE)
                .attendedDate(command.attendedAt().toLocalDate())
                .attendedAt(command.attendedAt())
                .build();
    }

    public static UserAttendanceCreateState mapToUserAttendanceCreateState(
            Long userId,
            Year year,
            Month month
    ) {
        return UserAttendanceCreateState.builder()
                .userId(userId)
                .year(year)
                .month(month)
                .totalRewardQuantity(0L)
                .build();
    }

    public static AttendancePolicyCreateState mapToAttendancePolicyCreateState(
            RegisterAttendancePolicyCommand command
    ) {
        return AttendancePolicyCreateState.builder()
                .continuousPeriod(command.continuousPeriod())
                .rewardType(command.rewardType())
                .rewardQuantity(command.rewardQuantity())
                .attendenceDate(command.attendenceDate())
                .build();
    }
}
