package com.personal.marketnote.reward.adapter.in.web.attendance;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.attendance.request.RegisterAttendancePolicyRequest;
import com.personal.marketnote.reward.adapter.in.web.attendance.response.GetAttendancePoliciesResponse;
import com.personal.marketnote.reward.adapter.in.web.attendance.response.RegisterAttendancePolicyResponse;
import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;
import com.personal.marketnote.reward.port.in.command.attendance.RegisterAttendancePolicyCommand;
import com.personal.marketnote.reward.port.in.result.attendance.GetAttendancePoliciesResult;
import com.personal.marketnote.reward.port.in.result.attendance.RegisterAttendancePolicyResult;
import com.personal.marketnote.reward.port.in.usecase.attendance.DeleteAttendancePolicyUseCase;
import com.personal.marketnote.reward.port.in.usecase.attendance.GetAttendancePoliciesUseCase;
import com.personal.marketnote.reward.port.in.usecase.attendance.RegisterAttendancePolicyUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendancePolicyController 테스트")
class AttendancePolicyControllerTest {

    @Mock
    private RegisterAttendancePolicyUseCase registerAttendancePolicyUseCase;
    @Mock
    private GetAttendancePoliciesUseCase getAttendancePoliciesUseCase;
    @Mock
    private DeleteAttendancePolicyUseCase deleteAttendancePolicyUseCase;

    @InjectMocks
    private AttendancePolicyController controller;

    @Nested
    @DisplayName("POST /api/v1/attendance/policies")
    class RegisterAttendancePolicy {

        @Test
        @DisplayName("정상 요청 시 CREATED 상태로 등록 결과를 반환한다")
        void returnsCreatedOnValidRequest() {
            RegisterAttendancePolicyRequest request = new RegisterAttendancePolicyRequest();
            ReflectionTestUtils.setField(request, "continuousPeriod", (short) 7);
            ReflectionTestUtils.setField(request, "rewardType", AttendanceRewardType.POINT);
            ReflectionTestUtils.setField(request, "rewardQuantity", 100L);
            given(registerAttendancePolicyUseCase.register(any(RegisterAttendancePolicyCommand.class)))
                    .willReturn(RegisterAttendancePolicyResult.builder().id((short) 5).build());

            ResponseEntity<BaseResponse<RegisterAttendancePolicyResponse>> response =
                    controller.registerAttendancePolicy(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().getContent()).isNotNull();
            verify(registerAttendancePolicyUseCase).register(any(RegisterAttendancePolicyCommand.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/attendance/policies")
    class GetAttendancePolicies {

        @Test
        @DisplayName("OK 상태로 정책 목록을 반환한다")
        void returnsOkWithPolicies() {
            given(getAttendancePoliciesUseCase.getAttendancePolicies())
                    .willReturn(GetAttendancePoliciesResult.from(List.of()));

            ResponseEntity<BaseResponse<GetAttendancePoliciesResponse>> response = controller.getAttendancePolicies();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).isNotNull();
            verify(getAttendancePoliciesUseCase).getAttendancePolicies();
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/attendance/policies/{id}")
    class DeleteAttendancePolicy {

        @Test
        @DisplayName("정상 요청 시 OK 상태를 반환하고 UseCase를 호출한다")
        void returnsOkAndDelegates() {
            ResponseEntity<BaseResponse<Void>> response = controller.deleteAttendancePolicy((short) 9);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(deleteAttendancePolicyUseCase).delete((short) 9);
        }
    }
}
