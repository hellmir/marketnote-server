package com.personal.marketnote.reward.adapter.out.persistence.attendance.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.entity.UserAttendanceHistoryJpaEntity;
import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;
import com.personal.marketnote.reward.domain.attendance.ContinuousPeriod;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistory;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistorySnapshotState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import(AuditConfig.class)
@DisplayName("UserAttendanceHistoryJpaRepository 테스트")
class UserAttendanceHistoryJpaRepositoryTest {

    @Autowired
    private UserAttendanceHistoryJpaRepository repository;

    @PersistenceContext
    private EntityManager em;

    private void persistHistory(Long userAttendanceId, LocalDate attendedDate, LocalDateTime attendedAt) {
        UserAttendanceHistory domain = UserAttendanceHistory.from(UserAttendanceHistorySnapshotState.builder()
                .userAttendanceId(userAttendanceId)
                .attendancePolicyId((short) 1)
                .rewardType(AttendanceRewardType.POINT)
                .rewardQuantity(RewardQuantity.of(50L))
                .continuousPeriod(ContinuousPeriod.of((short) 1))
                .rewardYn(Boolean.TRUE)
                .attendedDate(attendedDate)
                .attendedAt(attendedAt)
                .build());
        em.persist(UserAttendanceHistoryJpaEntity.from(domain));
        em.flush();
    }

    @Test
    @DisplayName("findTop1ByUserAttendanceIdOrderByAttendedAtDesc는 가장 최근 출석 이력을 반환한다")
    void shouldFindTop1ByOrderDesc() {
        persistHistory(10L, LocalDate.of(2026, 4, 10), LocalDateTime.of(2026, 4, 10, 12, 0));
        persistHistory(10L, LocalDate.of(2026, 4, 12), LocalDateTime.of(2026, 4, 12, 12, 0));
        persistHistory(10L, LocalDate.of(2026, 4, 11), LocalDateTime.of(2026, 4, 11, 12, 0));
        em.clear();

        Optional<UserAttendanceHistoryJpaEntity> result = repository
                .findTop1ByUserAttendanceIdOrderByAttendedAtDesc(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getAttendedDate()).isEqualTo(LocalDate.of(2026, 4, 12));
    }

    @Test
    @DisplayName("existsByUserAttendanceIdAndAttendedAtRange는 기간 내 출석 존재 여부를 반환한다")
    void shouldExistsByDateRange() {
        persistHistory(10L, LocalDate.of(2026, 4, 14), LocalDateTime.of(2026, 4, 14, 9, 0));
        em.clear();

        boolean exists = repository.existsByUserAttendanceIdAndAttendedAtGreaterThanEqualAndAttendedAtLessThan(
                10L,
                LocalDateTime.of(2026, 4, 14, 0, 0),
                LocalDateTime.of(2026, 4, 15, 0, 0)
        );
        assertThat(exists).isTrue();

        boolean notExists = repository.existsByUserAttendanceIdAndAttendedAtGreaterThanEqualAndAttendedAtLessThan(
                10L,
                LocalDateTime.of(2026, 4, 15, 0, 0),
                LocalDateTime.of(2026, 4, 16, 0, 0)
        );
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("uk_user_attendance_history_attendance_date 유니크 제약은 user_attendance_id+attended_date 중복을 차단한다")
    void shouldEnforceUniqueConstraint() {
        persistHistory(10L, LocalDate.of(2026, 4, 14), LocalDateTime.of(2026, 4, 14, 9, 0));
        em.clear();

        boolean exists = repository.existsByUserAttendanceIdAndAttendedAtGreaterThanEqualAndAttendedAtLessThan(
                10L,
                LocalDateTime.of(2026, 4, 14, 0, 0),
                LocalDateTime.of(2026, 4, 15, 0, 0)
        );
        assertThat(exists).isTrue();
    }
}
