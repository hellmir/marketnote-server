package com.personal.marketnote.reward.service.attendance;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;
import com.personal.marketnote.reward.port.in.usecase.attendance.DeleteAttendancePolicyUseCase;
import com.personal.marketnote.reward.port.out.attendance.FindAttendancePolicyPort;
import com.personal.marketnote.reward.port.out.attendance.UpdateAttendancePolicyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class DeleteAttendancePolicyService implements DeleteAttendancePolicyUseCase {
    private final FindAttendancePolicyPort findAttendancePolicyPort;
    private final UpdateAttendancePolicyPort updateAttendancePolicyPort;

    @Override
    public void delete(Short id) {
        AttendancePolicy policy = findAttendancePolicyPort.findByIdForUpdate(id)
                .orElseThrow(() -> new DomainNotFoundException("대상 출석 정책을 찾을 수 없습니다. 전송된 id: " + id));

        policy.delete();
        updateAttendancePolicyPort.update(policy);
    }
}
