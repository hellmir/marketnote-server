package com.personal.marketnote.reward.adapter.out.persistence.attendance.repository;

import com.personal.marketnote.reward.adapter.out.persistence.attendance.entity.AttendancePolicyJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendancePolicyJpaRepository extends JpaRepository<AttendancePolicyJpaEntity, Short> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AttendancePolicyJpaEntity> findWithLockingById(Short id);

    Optional<AttendancePolicyJpaEntity> findTop1ByContinuousPeriodAndAttendenceDate(
            short continuousPeriod, LocalDate attendenceDate
    );

    Optional<AttendancePolicyJpaEntity> findTop1ByContinuousPeriodAndAttendenceDateIsNullOrderByOrderNumDesc(
            short continuousPeriod
    );

    List<AttendancePolicyJpaEntity> findAllByOrderByOrderNumDesc();
}

