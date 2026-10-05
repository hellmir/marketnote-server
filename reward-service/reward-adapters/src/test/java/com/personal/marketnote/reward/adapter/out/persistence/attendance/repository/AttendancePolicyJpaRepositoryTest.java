package com.personal.marketnote.reward.adapter.out.persistence.attendance.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.entity.AttendancePolicyJpaEntity;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicySnapshotState;
import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;
import com.personal.marketnote.reward.domain.attendance.ContinuousPeriod;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import(AuditConfig.class)
@DisplayName("AttendancePolicyJpaRepository 테스트")
class AttendancePolicyJpaRepositoryTest {

    @Autowired
    private AttendancePolicyJpaRepository repository;

    @PersistenceContext
    private EntityManager em;

    private AttendancePolicyJpaEntity persistPolicy(short continuousPeriod, LocalDate attendenceDate, short orderNum) {
        AttendancePolicy domain = AttendancePolicy.from(AttendancePolicySnapshotState.builder()
                .continuousPeriod(ContinuousPeriod.of(continuousPeriod))
                .rewardType(AttendanceRewardType.POINT)
                .rewardQuantity(RewardQuantity.of(100L))
                .attendenceDate(attendenceDate)
                .status(EntityStatus.ACTIVE)
                .build());
        AttendancePolicyJpaEntity entity = AttendancePolicyJpaEntity.from(domain);
        em.persist(entity);
        em.flush();
        ReflectionTestUtils.setField(entity, "orderNum", orderNum);
        em.flush();
        return entity;
    }

    @Test
    @DisplayName("findWithLockingById는 비관적 잠금으로 정책을 조회한다")
    void shouldFindWithLockingById() {
        AttendancePolicyJpaEntity persisted = persistPolicy((short) 1, null, (short) 1);
        em.clear();
        Optional<AttendancePolicyJpaEntity> result = repository.findWithLockingById(persisted.getId());
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("findTop1ByContinuousPeriodAndAttendenceDate는 연속일/지정일 정책을 조회한다")
    void shouldFindByContinuousPeriodAndAttendenceDate() {
        LocalDate today = LocalDate.of(2026, 4, 14);
        persistPolicy((short) 5, today, (short) 1);
        em.clear();

        Optional<AttendancePolicyJpaEntity> result = repository.findTop1ByContinuousPeriodAndAttendenceDate((short) 5, today);
        assertThat(result).isPresent();
        assertThat(result.get().getAttendenceDate()).isEqualTo(today);
    }

    @Test
    @DisplayName("findTop1ByContinuousPeriodAndAttendenceDateIsNullOrderByOrderNumDesc는 attendenceDate가 NULL인 정책 중 orderNum 내림차순으로 조회한다")
    void shouldFindByContinuousPeriodAndAttendenceDateIsNull() {
        persistPolicy((short) 1, null, (short) 1);
        AttendancePolicyJpaEntity higher = persistPolicy((short) 1, null, (short) 5);
        em.clear();

        Optional<AttendancePolicyJpaEntity> result = repository
                .findTop1ByContinuousPeriodAndAttendenceDateIsNullOrderByOrderNumDesc((short) 1);
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(higher.getId());
    }

    @Test
    @DisplayName("findAllByOrderByOrderNumDesc는 orderNum 내림차순으로 모든 정책을 반환한다")
    void shouldFindAllOrderByOrderNumDesc() {
        persistPolicy((short) 1, null, (short) 1);
        persistPolicy((short) 2, null, (short) 3);
        persistPolicy((short) 3, null, (short) 2);
        em.clear();

        List<AttendancePolicyJpaEntity> result = repository.findAllByOrderByOrderNumDesc();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getOrderNum()).isEqualTo((short) 3);
        assertThat(result.get(2).getOrderNum()).isEqualTo((short) 1);
    }
}
