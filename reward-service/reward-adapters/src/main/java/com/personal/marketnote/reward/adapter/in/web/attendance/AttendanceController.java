package com.personal.marketnote.reward.adapter.in.web.attendance;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.adapter.in.ratelimit.RateLimited;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.reward.adapter.in.web.attendance.apidocs.GetAttendanceRelayStatusApiDocs;
import com.personal.marketnote.reward.adapter.in.web.attendance.apidocs.GetMonthlyAttendanceApiDocs;
import com.personal.marketnote.reward.adapter.in.web.attendance.apidocs.RegisterAttendanceApiDocs;
import com.personal.marketnote.reward.adapter.in.web.attendance.mapper.AttendanceRequestToCommandMapper;
import com.personal.marketnote.reward.adapter.in.web.attendance.request.RegisterAttendanceRequest;
import com.personal.marketnote.reward.adapter.in.web.attendance.response.GetAttendanceRelayStatusResponse;
import com.personal.marketnote.reward.adapter.in.web.attendance.response.GetMonthlyAttendanceResponse;
import com.personal.marketnote.reward.adapter.in.web.attendance.response.RegisterAttendanceResponse;
import com.personal.marketnote.reward.port.in.command.attendance.GetMonthlyAttendanceQuery;
import com.personal.marketnote.reward.port.in.result.attendance.GetAttendanceRelayStatusResult;
import com.personal.marketnote.reward.port.in.result.attendance.GetMonthlyAttendanceResult;
import com.personal.marketnote.reward.port.in.result.attendance.RegisterAttendanceResult;
import com.personal.marketnote.reward.port.in.usecase.attendance.GetAttendanceRelayStatusUseCase;
import com.personal.marketnote.reward.port.in.usecase.attendance.GetMonthlyAttendanceUseCase;
import com.personal.marketnote.reward.port.in.usecase.attendance.RegisterAttendanceUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "출석체크 API", description = "출석체크 관련 API")
@RequiredArgsConstructor
public class AttendanceController {
    private final RegisterAttendanceUseCase registerAttendanceUseCase;
    private final GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase;
    private final GetAttendanceRelayStatusUseCase getAttendanceRelayStatusUseCase;

    /**
     * 회원 출석체크 등록
     *
     * @param principal 인증된 사용자 정보
     * @param request   출석 요청 본문
     * @return 출석체크 등록 응답
     * @Author 성효빈
     * @Date 2026-01-20
     * @Description 클라이언트 출석 요청 시간을 검증하고, 정책을 적용하여 출석 기록을 생성합니다.
     */
    @PostMapping
    @RegisterAttendanceApiDocs
    @RateLimited(key = "attendance", maxRequests = 5, windowSeconds = 60)
    public ResponseEntity<BaseResponse<RegisterAttendanceResponse>> registerAttendance(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @Valid @RequestBody RegisterAttendanceRequest request
    ) {
        RegisterAttendanceResult result = registerAttendanceUseCase.register(
                AttendanceRequestToCommandMapper.mapToCommand(
                        ElementExtractor.extractUserId(principal), request
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterAttendanceResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "회원 출석체크 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * 출석 릴레이 현황 조회
     *
     * @param principal 인증된 사용자 정보
     * @return 출석 릴레이 현황 응답
     * @Author 성효빈
     * @Date 2026-09-03
     * @Description 현재 릴레이 사이클의 진행 상태를 조회합니다.
     */
    @GetMapping("/relay-status")
    @GetAttendanceRelayStatusApiDocs
    public ResponseEntity<BaseResponse<GetAttendanceRelayStatusResponse>> getAttendanceRelayStatus(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        GetAttendanceRelayStatusResult result = getAttendanceRelayStatusUseCase.getRelayStatus(
                ElementExtractor.extractUserId(principal)
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetAttendanceRelayStatusResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "출석 릴레이 현황 조회 성공"
                )
        );
    }

    /**
     * 월별 출석 내역 조회
     *
     * @param principal 인증된 사용자 정보
     * @param year      조회 연도 (미전달 시 이번 연도)
     * @param month     조회 월 (미전달 시 이번 달)
     * @return 월별 출석 내역 조회 응답
     * @Author 성효빈
     * @Date 2026-01-21
     * @Description 월별 출석 내역을 조회합니다.
     */
    @GetMapping
    @GetMonthlyAttendanceApiDocs
    public ResponseEntity<BaseResponse<GetMonthlyAttendanceResponse>> getMonthlyAttendanceHistories(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month
    ) {
        GetMonthlyAttendanceResult result = getMonthlyAttendanceUseCase.getMonthlyAttendanceHistories(
                GetMonthlyAttendanceQuery.of(
                        ElementExtractor.extractUserId(principal),
                        year,
                        month
                )
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetMonthlyAttendanceResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "월별 출석 내역 조회 성공"
                )
        );
    }
}
