package com.personal.marketnote.reward.service.attendance;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicySnapshotState;
import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;
import com.personal.marketnote.reward.port.out.attendance.FindAttendancePolicyPort;
import com.personal.marketnote.reward.port.out.attendance.UpdateAttendancePolicyPort;
import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteAttendancePolicyUseCase 테스트")
class DeleteAttendancePolicyUseCaseTest {

    @InjectMocks
    private DeleteAttendancePolicyService deleteAttendancePolicyService;

    @Mock
    private FindAttendancePolicyPort findAttendancePolicyPort;

    @Mock
    private UpdateAttendancePolicyPort updateAttendancePolicyPort;

    @Test
    @DisplayName("출석 정책을 삭제하면 상태를 INACTIVE로 변경하고 업데이트한다")
    void shouldDeletePolicyBySettingStatusToInactive() {
        // given
        Short policyId = (short) 1;
        AttendancePolicy policy = AttendancePolicy.from(
                AttendancePolicySnapshotState.builder()
                        .id(policyId)
                        .continuousPeriod((short) 3)
                        .rewardType(AttendanceRewardType.POINT)
                        .rewardQuantity(100L)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );

        when(findAttendancePolicyPort.findByIdForUpdate(policyId)).thenReturn(Optional.of(policy));

        // when
        deleteAttendancePolicyService.delete(policyId);

        // then
        verify(findAttendancePolicyPort).findByIdForUpdate(policyId);
        verify(updateAttendancePolicyPort).update(policy);
    }

    @Test
    @DisplayName("출석 정책이 존재하지 않으면 DomainNotFoundException이 발생한다")
    void shouldThrowWhenPolicyNotFound() {
        // given
        Short policyId = (short) 999;
        when(findAttendancePolicyPort.findByIdForUpdate(policyId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deleteAttendancePolicyService.delete(policyId))
                .isInstanceOf(DomainNotFoundException.class);
    }
}
