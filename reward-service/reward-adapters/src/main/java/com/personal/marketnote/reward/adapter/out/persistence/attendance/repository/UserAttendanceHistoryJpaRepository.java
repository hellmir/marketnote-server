package com.personal.marketnote.reward.adapter.out.persistence.attendance.repository;

import com.personal.marketnote.reward.adapter.out.persistence.attendance.entity.UserAttendanceHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserAttendanceHistoryJpaRepository extends JpaRepository<UserAttendanceHistoryJpaEntity, Long> {
    Optional<UserAttendanceHistoryJpaEntity> findTop1ByUserAttendanceIdOrderByAttendedAtDesc(Long userAttendanceId);

    boolean existsByUserAttendanceIdAndAttendedAtGreaterThanEqualAndAttendedAtLessThan(
            Long userAttendanceId,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    List<UserAttendanceHistoryJpaEntity> findAllByUserAttendanceIdOrderByAttendedAtAsc(Long userAttendanceId);
}

