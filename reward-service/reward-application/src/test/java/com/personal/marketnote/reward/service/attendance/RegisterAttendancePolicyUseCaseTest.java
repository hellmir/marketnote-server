package com.personal.marketnote.reward.service.attendance;

import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicySnapshotState;
import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;
import com.personal.marketnote.reward.port.in.command.attendance.RegisterAttendancePolicyCommand;
import com.personal.marketnote.reward.port.in.result.attendance.RegisterAttendancePolicyResult;
import com.personal.marketnote.reward.port.out.attendance.SaveAttendancePolicyPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterAttendancePolicyUseCase 테스트")
class RegisterAttendancePolicyUseCaseTest {

    @InjectMocks
    private RegisterAttendancePolicyService registerAttendancePolicyService;

    @Mock
    private SaveAttendancePolicyPort saveAttendancePolicyPort;

    @Test
    @DisplayName("출석 정책 등록 시 저장된 정책의 ID를 반환한다")
    void shouldRegisterAttendancePolicyAndReturnId() {
        // given
        RegisterAttendancePolicyCommand command = RegisterAttendancePolicyCommand.builder()
                .continuousPeriod((short) 3)
                .rewardType(AttendanceRewardType.POINT)
                .rewardQuantity(100L)
                .build();

        AttendancePolicy savedPolicy = AttendancePolicy.from(
                AttendancePolicySnapshotState.builder()
                        .id((short) 1)
                        .continuousPeriod((short) 3)
                        .rewardType(AttendanceRewardType.POINT)
                        .rewardQuantity(100L)
                        .build()
        );

        when(saveAttendancePolicyPort.save(any(AttendancePolicy.class))).thenReturn(savedPolicy);

        // when
        RegisterAttendancePolicyResult result = registerAttendancePolicyService.register(command);

        // then
        assertThat(result.getId()).isEqualTo((short) 1);
        verify(saveAttendancePolicyPort).save(any(AttendancePolicy.class));
    }
}
