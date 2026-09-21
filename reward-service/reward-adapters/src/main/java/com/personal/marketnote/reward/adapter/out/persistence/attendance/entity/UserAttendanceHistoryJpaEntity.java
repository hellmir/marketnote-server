package com.personal.marketnote.reward.adapter.out.persistence.attendance.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistory;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistorySnapshotState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_attendance_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_attendance_history_attendance_date",
                columnNames = {"user_attendance_id", "attended_date"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class UserAttendanceHistoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_attendance_id", nullable = false)
    private Long userAttendanceId;

    @Column(name = "attendance_policy_id")
    private Short attendancePolicyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 31)
    private AttendanceRewardType rewardType;

    @Column(name = "reward_quantity", nullable = false)
    private long rewardQuantity;

    @Column(name = "continuous_period", nullable = false)
    private short continuousPeriod;

    @Column(name = "reward_yn", nullable = false)
    private Boolean rewardYn;

    @Column(name = "attended_date", nullable = false)
    private LocalDate attendedDate;

    @Column(name = "attended_at", nullable = false)
    private LocalDateTime attendedAt;

    public static UserAttendanceHistoryJpaEntity from(UserAttendanceHistory history) {
        if (FormatValidator.hasNoValue(history)) {
            return null;
        }

        return UserAttendanceHistoryJpaEntity.builder()
                .id(history.getId())
                .userAttendanceId(history.getUserAttendanceId())
                .attendancePolicyId(history.getAttendancePolicyId())
                .rewardType(history.getRewardType())
                .rewardQuantity(history.getRewardQuantityValue())
                .continuousPeriod(history.getContinuousPeriod())
                .rewardYn(history.getRewardYn())
                .attendedDate(history.getAttendedDate())
                .attendedAt(history.getAttendedAt())
                .build();
    }

    public UserAttendanceHistory toDomain() {
        return UserAttendanceHistory.from(
                UserAttendanceHistorySnapshotState.builder()
                        .id(id)
                        .userAttendanceId(userAttendanceId)
                        .attendancePolicyId(attendancePolicyId)
                        .rewardType(rewardType)
                        .rewardQuantity(RewardQuantity.of(rewardQuantity))
                        .continuousPeriod(continuousPeriod)
                        .rewardYn(rewardYn)
                        .attendedDate(attendedDate)
                        .attendedAt(attendedAt)
                        .createdAt(getCreatedAt())
                        .modifiedAt(getModifiedAt())
                        .build()
        );
    }
}

