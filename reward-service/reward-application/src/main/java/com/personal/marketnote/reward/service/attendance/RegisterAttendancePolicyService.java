package com.personal.marketnote.reward.service.attendance;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;
import com.personal.marketnote.reward.mapper.RewardCommandToStateMapper;
import com.personal.marketnote.reward.port.in.command.attendance.RegisterAttendancePolicyCommand;
import com.personal.marketnote.reward.port.in.result.attendance.RegisterAttendancePolicyResult;
import com.personal.marketnote.reward.port.in.usecase.attendance.RegisterAttendancePolicyUseCase;
import com.personal.marketnote.reward.port.out.attendance.SaveAttendancePolicyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterAttendancePolicyService implements RegisterAttendancePolicyUseCase {
    private final SaveAttendancePolicyPort saveAttendancePolicyPort;

    @Override
    public RegisterAttendancePolicyResult register(RegisterAttendancePolicyCommand command) {
        AttendancePolicy saved = saveAttendancePolicyPort.save(
                AttendancePolicy.from(
                        RewardCommandToStateMapper.mapToAttendancePolicyCreateState(
                                command
                        )
                )
        );

        return RegisterAttendancePolicyResult.builder()
                .id(saved.getId())
                .build();
    }
}

