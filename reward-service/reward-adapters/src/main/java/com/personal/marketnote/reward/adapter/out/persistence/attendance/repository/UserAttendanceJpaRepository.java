package com.personal.marketnote.reward.adapter.out.persistence.attendance.repository;

import com.personal.marketnote.common.domain.calendar.Month;
import com.personal.marketnote.common.domain.calendar.Year;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.entity.UserAttendanceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAttendanceJpaRepository extends JpaRepository<UserAttendanceJpaEntity, Long> {
    Optional<UserAttendanceJpaEntity> findTop1ByUserIdAndYearAndMonth(Long userId, Year year, Month month);
}

