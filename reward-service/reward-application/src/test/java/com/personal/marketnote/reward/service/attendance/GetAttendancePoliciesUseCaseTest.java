package com.personal.marketnote.reward.service.attendance;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicySnapshotState;
import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;
import com.personal.marketnote.reward.port.in.result.attendance.GetAttendancePoliciesResult;
import com.personal.marketnote.reward.port.out.attendance.FindAttendancePolicyPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAttendancePoliciesUseCase 테스트")
class GetAttendancePoliciesUseCaseTest {

    @InjectMocks
    private GetAttendancePoliciesService getAttendancePoliciesService;

    @Mock
    private FindAttendancePolicyPort findAttendancePolicyPort;

    @Test
    @DisplayName("출석 정책 목록이 존재하면 정렬된 목록을 반환한다")
    void shouldReturnPoliciesOrderedByOrderNumDesc() {
        // given
        AttendancePolicy policy1 = AttendancePolicy.from(
                AttendancePolicySnapshotState.builder()
                        .id((short) 1)
                        .continuousPeriod((short) 1)
                        .rewardType(AttendanceRewardType.POINT)
                        .rewardQuantity(50L)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
        AttendancePolicy policy2 = AttendancePolicy.from(
                AttendancePolicySnapshotState.builder()
                        .id((short) 2)
                        .continuousPeriod((short) 7)
                        .rewardType(AttendanceRewardType.POINT)
                        .rewardQuantity(200L)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );

        when(findAttendancePolicyPort.findAllOrderByOrderNumDesc()).thenReturn(List.of(policy2, policy1));

        // when
        GetAttendancePoliciesResult result = getAttendancePoliciesService.getAttendancePolicies();

        // then
        assertThat(result.policies()).hasSize(2);
        assertThat(result.policies().get(0).getId()).isEqualTo((short) 2);
        assertThat(result.policies().get(1).getId()).isEqualTo((short) 1);
        verify(findAttendancePolicyPort).findAllOrderByOrderNumDesc();
    }

    @Test
    @DisplayName("출석 정책이 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyListWhenNoPolicies() {
        // given
        when(findAttendancePolicyPort.findAllOrderByOrderNumDesc()).thenReturn(Collections.emptyList());

        // when
        GetAttendancePoliciesResult result = getAttendancePoliciesService.getAttendancePolicies();

        // then
        assertThat(result.policies()).isEmpty();
        verify(findAttendancePolicyPort).findAllOrderByOrderNumDesc();
    }
}
