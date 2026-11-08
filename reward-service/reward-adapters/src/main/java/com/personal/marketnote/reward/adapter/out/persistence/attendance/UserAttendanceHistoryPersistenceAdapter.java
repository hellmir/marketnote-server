package com.personal.marketnote.reward.adapter.out.persistence.attendance;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.entity.UserAttendanceHistoryJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.repository.UserAttendanceHistoryJpaRepository;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistory;
import com.personal.marketnote.reward.exception.DuplicateAttendanceException;
import com.personal.marketnote.reward.port.out.attendance.SaveUserAttendanceHistoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Objects;

@Slf4j
@PersistenceAdapter
@RequiredArgsConstructor
public class UserAttendanceHistoryPersistenceAdapter implements SaveUserAttendanceHistoryPort {
    private final UserAttendanceHistoryJpaRepository repository;

    @Override
    public UserAttendanceHistory save(UserAttendanceHistory history) {
        try {
            UserAttendanceHistoryJpaEntity saved = Objects.requireNonNull(
                    repository.save(UserAttendanceHistoryJpaEntity.from(history)),
                    "출석 기록 저장에 실패했습니다."
            );
            return saved.toDomain();
        } catch (DataIntegrityViolationException dive) {
            log.warn(
                    "이미 출석이 등록된 날짜입니다. userAttendanceId={}, attendedDate={}",
                    history.getUserAttendanceId(), history.getAttendedDate()
            );
            throw new DuplicateAttendanceException();
        }
    }
}

