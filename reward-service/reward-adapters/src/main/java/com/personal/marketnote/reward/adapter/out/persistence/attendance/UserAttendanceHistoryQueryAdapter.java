package com.personal.marketnote.reward.adapter.out.persistence.attendance;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.repository.UserAttendanceHistoryJpaRepository;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistory;
import com.personal.marketnote.reward.port.out.attendance.FindUserAttendanceHistoryPort;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class UserAttendanceHistoryQueryAdapter implements FindUserAttendanceHistoryPort {
    private final UserAttendanceHistoryJpaRepository repository;

    @Override
    public Optional<UserAttendanceHistory> findLatestByUserAttendanceId(Long userAttendanceId) {
        return repository.findTop1ByUserAttendanceIdOrderByAttendedAtDesc(userAttendanceId)
                .map(entity -> entity.toDomain());
    }

    @Override
    public boolean existsByUserAttendanceIdAndAttendedAtBetween(
            Long userAttendanceId, LocalDateTime startInclusive, LocalDateTime endExclusive
    ) {
        return repository.existsByUserAttendanceIdAndAttendedAtGreaterThanEqualAndAttendedAtLessThan(
                userAttendanceId,
                startInclusive,
                endExclusive
        );
    }
}

