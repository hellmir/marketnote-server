package com.personal.marketnote.reward.adapter.out.persistence.attendance;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.entity.UserAttendanceJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.repository.UserAttendanceJpaRepository;
import com.personal.marketnote.reward.domain.attendance.UserAttendance;
import com.personal.marketnote.reward.port.out.attendance.FindUserAttendancePort;
import com.personal.marketnote.reward.port.out.attendance.SaveUserAttendancePort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class UserAttendancePersistenceAdapter implements SaveUserAttendancePort, FindUserAttendancePort {
    private final UserAttendanceJpaRepository repository;

    @Override
    public UserAttendance save(UserAttendance attendance) {
        UserAttendanceJpaEntity saved = java.util.Objects.requireNonNull(
                repository.save(UserAttendanceJpaEntity.from(attendance)),
                "user_attendance 저장에 실패했습니다."
        );
        return saved.toDomain();
    }

    @Override
    public Optional<UserAttendance> findByUserIdAndYearAndMonth(
            Long userId, com.personal.marketnote.common.domain.calendar.Year year, com.personal.marketnote.common.domain.calendar.Month month
    ) {
        return repository.findTop1ByUserIdAndYearAndMonth(userId, year, month)
                .map(UserAttendanceJpaEntity::toDomain);
    }
}

