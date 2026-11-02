package com.personal.marketnote.reward.adapter.out.persistence.attendance;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.entity.UserAttendanceHistoryJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.entity.UserAttendanceJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.repository.UserAttendanceHistoryJpaRepository;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.repository.UserAttendanceJpaRepository;
import com.personal.marketnote.reward.domain.attendance.UserAttendance;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistory;
import com.personal.marketnote.reward.port.out.attendance.FindUserAttendancePort;
import com.personal.marketnote.reward.port.out.attendance.SaveUserAttendancePort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class UserAttendancePersistenceAdapter implements SaveUserAttendancePort, FindUserAttendancePort {
    private final UserAttendanceJpaRepository repository;
    private final UserAttendanceHistoryJpaRepository historyRepository;

    @Override
    public UserAttendance save(UserAttendance attendance) {
        UserAttendanceJpaEntity saved = java.util.Objects.requireNonNull(
                repository.save(UserAttendanceJpaEntity.from(attendance)),
                "user_attendance 저장에 실패했습니다."
        );
        return saved.toDomain(loadHistories(saved.getId()));
    }

    @Override
    public Optional<UserAttendance> findByUserIdAndYearAndMonth(
            Long userId, com.personal.marketnote.common.domain.calendar.Year year, com.personal.marketnote.common.domain.calendar.Month month
    ) {
        return repository.findTop1ByUserIdAndYearAndMonth(userId, year, month)
                .map(entity -> entity.toDomain(loadHistories(entity.getId())));
    }

    private List<UserAttendanceHistory> loadHistories(Long userAttendanceId) {
        return historyRepository.findAllByUserAttendanceIdOrderByAttendedAtAsc(userAttendanceId).stream()
                .map(UserAttendanceHistoryJpaEntity::toDomain)
                .toList();
    }
}

