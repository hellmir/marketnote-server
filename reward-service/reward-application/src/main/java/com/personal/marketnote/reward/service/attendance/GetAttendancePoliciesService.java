package com.personal.marketnote.reward.service.attendance;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.port.in.result.attendance.GetAttendancePoliciesResult;
import com.personal.marketnote.reward.port.in.usecase.attendance.GetAttendancePoliciesUseCase;
import com.personal.marketnote.reward.port.out.attendance.FindAttendancePolicyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetAttendancePoliciesService implements GetAttendancePoliciesUseCase {
    private final FindAttendancePolicyPort findAttendancePolicyPort;

    @Override
    public GetAttendancePoliciesResult getAttendancePolicies() {
        return GetAttendancePoliciesResult.from(
                findAttendancePolicyPort.findAllOrderByOrderNumDesc()
        );
    }
}

