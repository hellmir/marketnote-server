package com.personal.marketnote.reward.adapter.out.persistence.attendance;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.entity.AttendancePolicyJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.repository.AttendancePolicyJpaRepository;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;
import com.personal.marketnote.reward.port.out.attendance.FindAttendancePolicyPort;
import com.personal.marketnote.reward.port.out.attendance.SaveAttendancePolicyPort;
import com.personal.marketnote.reward.port.out.attendance.UpdateAttendancePolicyPort;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class AttendancePolicyPersistenceAdapter implements FindAttendancePolicyPort, SaveAttendancePolicyPort, UpdateAttendancePolicyPort {
    private final AttendancePolicyJpaRepository repository;

    @Override
    public Optional<AttendancePolicy> findById(Short id) {
        return repository.findById(id)
                .map(AttendancePolicyJpaEntity::toDomain);
    }

    @Override
    public Optional<AttendancePolicy> findByIdForUpdate(Short id) {
        return repository.findWithLockingById(id).map(AttendancePolicyJpaEntity::toDomain);
    }

    @Override
    public Optional<AttendancePolicy> findByContinuousPeriodAndAttendenceDate(
            short continuousPeriod, LocalDate attendedDate
    ) {
        return repository.findTop1ByContinuousPeriodAndAttendenceDate(continuousPeriod, attendedDate)
                .map(AttendancePolicyJpaEntity::toDomain);
    }

    @Override
    public Optional<AttendancePolicy> findByContinuousPeriodAndAttendenceDateIsNull(short continuousPeriod) {
        return repository.findTop1ByContinuousPeriodAndAttendenceDateIsNullOrderByOrderNumDesc(continuousPeriod)
                .map(AttendancePolicyJpaEntity::toDomain);
    }

    @Override
    public AttendancePolicy save(AttendancePolicy attendancePolicy) {
        AttendancePolicyJpaEntity savedEntity = java.util.Objects.requireNonNull(
                repository.save(AttendancePolicyJpaEntity.from(attendancePolicy)),
                "출석 정책 저장에 실패했습니다."
        );
        savedEntity.setIdToOrderNum();

        return savedEntity.toDomain();
    }

    @Override
    public void update(AttendancePolicy attendancePolicy) {
        Short id = attendancePolicy.getId();
        AttendancePolicyJpaEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("출석 정책을 찾을 수 없습니다. id: " + id));

        entity.updateFrom(attendancePolicy);
    }

    @Override
    public java.util.List<AttendancePolicy> findAllOrderByOrderNumDesc() {
        return repository.findAllByOrderByOrderNumDesc()
                .stream()
                .map(AttendancePolicyJpaEntity::toDomain)
                .toList();
    }
}
