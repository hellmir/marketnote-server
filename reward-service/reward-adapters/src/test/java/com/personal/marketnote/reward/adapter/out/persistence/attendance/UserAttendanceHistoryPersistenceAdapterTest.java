package com.personal.marketnote.reward.adapter.out.persistence.attendance;

import com.personal.marketnote.reward.adapter.out.persistence.attendance.entity.UserAttendanceHistoryJpaEntity;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.repository.UserAttendanceHistoryJpaRepository;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistory;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistorySnapshotState;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.exception.DuplicateAttendanceException;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAttendanceHistoryPersistenceAdapter 테스트")
class UserAttendanceHistoryPersistenceAdapterTest {

    @InjectMocks
    private UserAttendanceHistoryPersistenceAdapter adapter;

    @Mock
    private UserAttendanceHistoryJpaRepository repository;

    @Test
    @DisplayName("유니크 제약조건 위반 시 DataIntegrityViolationException이 DuplicateAttendanceException으로 변환된다")
    void shouldConvertDataIntegrityViolationToDuplicateAttendanceException() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        UserAttendanceHistory history = UserAttendanceHistory.from(
                UserAttendanceHistorySnapshotState.builder()
                        .id(null)
                        .userAttendanceId(10L)
                        .attendancePolicyId((short) 1)
                        .rewardType(AttendanceRewardType.POINT)
                        .rewardQuantity(RewardQuantity.of(50L))
                        .continuousPeriod((short) 1)
                        .rewardYn(Boolean.TRUE)
                        .attendedDate(today)
                        .attendedAt(now)
                        .build()
        );

        when(repository.save(any(UserAttendanceHistoryJpaEntity.class)))
                .thenThrow(new DataIntegrityViolationException("uk_user_attendance_history_attendance_date"));

        // when & then
        assertThatThrownBy(() -> adapter.save(history))
                .isInstanceOf(DuplicateAttendanceException.class)
                .hasMessageContaining("userAttendanceId=10")
                .hasMessageContaining("attendedDate=" + today);
    }

    @Test
    @DisplayName("동일 사용자가 같은 날 두 번 출석 시 DuplicateAttendanceException이 발생한다")
    void shouldThrowDuplicateAttendanceExceptionOnSameDayDuplicateAttendance() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        UserAttendanceHistory history = UserAttendanceHistory.from(
                UserAttendanceHistorySnapshotState.builder()
                        .id(null)
                        .userAttendanceId(10L)
                        .attendancePolicyId((short) 1)
                        .rewardType(AttendanceRewardType.POINT)
                        .rewardQuantity(RewardQuantity.of(50L))
                        .continuousPeriod((short) 1)
                        .rewardYn(Boolean.TRUE)
                        .attendedDate(today)
                        .attendedAt(now)
                        .build()
        );

        when(repository.save(any(UserAttendanceHistoryJpaEntity.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "could not execute statement; constraint [uk_user_attendance_history_attendance_date]"
                ));

        // when & then
        assertThatThrownBy(() -> adapter.save(history))
                .isInstanceOf(DuplicateAttendanceException.class);
    }
}
