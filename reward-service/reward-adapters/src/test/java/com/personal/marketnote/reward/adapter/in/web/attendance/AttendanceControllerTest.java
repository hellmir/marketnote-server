package com.personal.marketnote.reward.adapter.in.web.attendance;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.attendance.request.RegisterAttendanceRequest;
import com.personal.marketnote.reward.adapter.in.web.attendance.response.GetAttendanceRelayStatusResponse;
import com.personal.marketnote.reward.adapter.in.web.attendance.response.GetMonthlyAttendanceResponse;
import com.personal.marketnote.reward.adapter.in.web.attendance.response.RegisterAttendanceResponse;
import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;
import com.personal.marketnote.reward.port.in.command.attendance.GetMonthlyAttendanceQuery;
import com.personal.marketnote.reward.port.in.command.attendance.RegisterAttendanceCommand;
import com.personal.marketnote.reward.port.in.result.attendance.GetAttendanceRelayStatusResult;
import com.personal.marketnote.reward.port.in.result.attendance.GetMonthlyAttendanceResult;
import com.personal.marketnote.reward.port.in.result.attendance.RegisterAttendanceResult;
import com.personal.marketnote.reward.port.in.usecase.attendance.GetAttendanceRelayStatusUseCase;
import com.personal.marketnote.reward.port.in.usecase.attendance.GetMonthlyAttendanceUseCase;
import com.personal.marketnote.reward.port.in.usecase.attendance.RegisterAttendanceUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceController 테스트")
class AttendanceControllerTest {

    @Mock
    private RegisterAttendanceUseCase registerAttendanceUseCase;
    @Mock
    private GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase;
    @Mock
    private GetAttendanceRelayStatusUseCase getAttendanceRelayStatusUseCase;

    @InjectMocks
    private AttendanceController controller;

    private OAuth2AuthenticatedPrincipal buildPrincipal(String userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                userId,
                Map.of("name", userId),
                List.of(new SimpleGrantedAuthority("ROLE_BUYER"))
        );
    }

    @Nested
    @DisplayName("POST /api/v1/attendance")
    class RegisterAttendance {

        @Test
        @DisplayName("정상 요청 시 CREATED 상태로 등록 결과를 반환한다")
        void returnsCreatedOnValidRequest() {
            OAuth2AuthenticatedPrincipal principal = buildPrincipal("42");
            RegisterAttendanceRequest request = new RegisterAttendanceRequest();
            ReflectionTestUtils.setField(request, "attendedAt", LocalDateTime.of(2026, 4, 15, 10, 0));
            RegisterAttendanceResult result =
                    new RegisterAttendanceResult(100L, AttendanceRewardType.POINT, 50L, (short) 3);
            given(registerAttendanceUseCase.register(any(RegisterAttendanceCommand.class))).willReturn(result);

            ResponseEntity<BaseResponse<RegisterAttendanceResponse>> response =
                    controller.registerAttendance(principal, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().getContent()).isNotNull();
            ArgumentCaptor<RegisterAttendanceCommand> captor = ArgumentCaptor.forClass(RegisterAttendanceCommand.class);
            verify(registerAttendanceUseCase).register(captor.capture());
            assertThat(captor.getValue().userId()).isEqualTo(42L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/attendance/relay-status")
    class GetRelayStatus {

        @Test
        @DisplayName("OK 상태로 릴레이 현황 결과를 반환한다")
        void returnsOkWithRelayStatus() {
            OAuth2AuthenticatedPrincipal principal = buildPrincipal("7");
            GetAttendanceRelayStatusResult result = new GetAttendanceRelayStatusResult(
                    (short) 3, true, List.of()
            );
            given(getAttendanceRelayStatusUseCase.getRelayStatus(7L)).willReturn(result);

            ResponseEntity<BaseResponse<GetAttendanceRelayStatusResponse>> response =
                    controller.getAttendanceRelayStatus(principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).isNotNull();
            verify(getAttendanceRelayStatusUseCase).getRelayStatus(7L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/attendance")
    class GetMonthlyAttendance {

        @Test
        @DisplayName("연/월 파라미터가 주어지면 해당 값을 그대로 UseCase에 전달하고 OK를 반환한다")
        void returnsOkWithYearMonth() {
            OAuth2AuthenticatedPrincipal principal = buildPrincipal("7");
            GetMonthlyAttendanceResult result = new GetMonthlyAttendanceResult(
                    List.of(LocalDate.of(2026, 4, 1)), 1, 50L
            );
            given(getMonthlyAttendanceUseCase.getMonthlyAttendanceHistories(any(GetMonthlyAttendanceQuery.class)))
                    .willReturn(result);

            ResponseEntity<BaseResponse<GetMonthlyAttendanceResponse>> response =
                    controller.getMonthlyAttendanceHistories(principal, 2026, 4);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).isNotNull();
            ArgumentCaptor<GetMonthlyAttendanceQuery> captor = ArgumentCaptor.forClass(GetMonthlyAttendanceQuery.class);
            verify(getMonthlyAttendanceUseCase).getMonthlyAttendanceHistories(captor.capture());
            assertThat(captor.getValue().userId()).isEqualTo(7L);
            assertThat(captor.getValue().year()).isEqualTo(2026);
            assertThat(captor.getValue().month()).isEqualTo(4);
        }

        @Test
        @DisplayName("연/월 파라미터가 null이면 UseCase 쿼리에도 null이 전달된다")
        void returnsOkWhenYearMonthNull() {
            OAuth2AuthenticatedPrincipal principal = buildPrincipal("7");
            given(getMonthlyAttendanceUseCase.getMonthlyAttendanceHistories(any(GetMonthlyAttendanceQuery.class)))
                    .willReturn(GetMonthlyAttendanceResult.empty());

            ResponseEntity<BaseResponse<GetMonthlyAttendanceResponse>> response =
                    controller.getMonthlyAttendanceHistories(principal, null, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<GetMonthlyAttendanceQuery> captor = ArgumentCaptor.forClass(GetMonthlyAttendanceQuery.class);
            verify(getMonthlyAttendanceUseCase).getMonthlyAttendanceHistories(captor.capture());
        }
    }
}
