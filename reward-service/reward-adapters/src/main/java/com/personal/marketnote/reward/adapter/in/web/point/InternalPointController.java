package com.personal.marketnote.reward.adapter.in.web.point;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.point.mapper.PointRequestToCommandMapper;
import com.personal.marketnote.reward.adapter.in.web.point.request.CancelPendingPointRequest;
import com.personal.marketnote.reward.adapter.in.web.point.request.ConfirmPendingPointRequest;
import com.personal.marketnote.reward.adapter.in.web.point.request.ModifyPendingPointRequest;
import com.personal.marketnote.reward.adapter.in.web.point.request.ModifyUserPointRequest;
import com.personal.marketnote.reward.adapter.in.web.point.response.GetUserPointByIdResponse;
import com.personal.marketnote.reward.adapter.in.web.point.response.UpdateUserPointResponse;
import com.personal.marketnote.reward.port.in.result.point.GetUserPointResult;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;
import com.personal.marketnote.reward.port.in.usecase.point.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 내부 포인트 컨트롤러 (서비스 간 통신용)
 *
 * @Author 성효빈
 * @Date 2026-03-28
 * @Description HMAC 인증 기반 서비스 간 통신용 포인트 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/internal/users")
@Tag(
        name = "내부 포인트 API",
        description = "서비스 간 통신용 포인트 API"
)
@RequiredArgsConstructor
@Slf4j
public class InternalPointController {
    private final GetUserPointUseCase getUserPointUseCase;
    private final ModifyUserPointUseCase modifyUserPointUseCase;
    private final ModifyPendingPointUseCase modifyPendingPointUseCase;
    private final ConfirmPendingPointUseCase confirmPendingPointUseCase;
    private final CancelPendingPointUseCase cancelPendingPointUseCase;

    /**
     * 회원 포인트 정보 조회 (서비스 간 통신용)
     *
     * @param userId 회원 ID
     * @return 회원 포인트 정보 조회 응답 {@link GetUserPointByIdResponse}
     */
    @GetMapping("/{userId}/points")
    public ResponseEntity<BaseResponse<GetUserPointByIdResponse>> getUserPoint(
            @PathVariable("userId") Long userId
    ) {
        GetUserPointResult result = GetUserPointResult.from(
                getUserPointUseCase.getUserPoint(userId)
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetUserPointByIdResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "회원 포인트 정보 조회 성공"
                )
        );
    }

    /**
     * 회원 포인트 적립/차감 (서비스 간 통신용)
     *
     * @param userId  회원 ID
     * @param request 회원 포인트 수정 요청 {@link ModifyUserPointRequest}
     * @return 회원 포인트 수정 응답 {@link UpdateUserPointResponse}
     */
    @PatchMapping("/{userId}/points")
    public ResponseEntity<BaseResponse<UpdateUserPointResponse>> modifyUserPoint(
            @PathVariable("userId") Long userId,
            @RequestBody @Valid ModifyUserPointRequest request
    ) {
        UpdateUserPointResult result = modifyUserPointUseCase.modify(
                PointRequestToCommandMapper.mapToModifyUserPointCommand(userId, request)
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        UpdateUserPointResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "회원 포인트 수정 성공"
                )
        );
    }

    /**
     * 적립 예정 포인트 추가/차감 (서비스 간 통신용)
     *
     * @param userId  회원 ID
     * @param request 적립 예정 포인트 수정 요청 {@link ModifyPendingPointRequest}
     * @return 적립 예정 포인트 수정 응답 {@link UpdateUserPointResponse}
     */
    @PatchMapping("/{userId}/points/pending")
    public ResponseEntity<BaseResponse<UpdateUserPointResponse>> modifyPendingPoint(
            @PathVariable("userId") Long userId,
            @RequestBody @Valid ModifyPendingPointRequest request
    ) {
        UpdateUserPointResult result = modifyPendingPointUseCase.modifyPending(
                PointRequestToCommandMapper.mapToModifyPendingPointCommand(userId, request)
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        UpdateUserPointResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "적립 예정 포인트 수정 성공"
                )
        );
    }

    /**
     * 적립 예정 포인트 확정 (서비스 간 통신용)
     *
     * @param userId  회원 ID
     * @param request 적립 예정 포인트 확정 요청 {@link ConfirmPendingPointRequest}
     * @return 적립 예정 포인트 확정 응답 {@link UpdateUserPointResponse}
     */
    @PostMapping("/{userId}/points/pending/confirm")
    public ResponseEntity<BaseResponse<UpdateUserPointResponse>> confirmPendingPoint(
            @PathVariable("userId") Long userId,
            @RequestBody @Valid ConfirmPendingPointRequest request
    ) {
        UpdateUserPointResult result = confirmPendingPointUseCase.confirmPending(
                PointRequestToCommandMapper.mapToConfirmPendingPointCommand(userId, request)
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        UpdateUserPointResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "적립 예정 포인트 확정 성공"
                )
        );
    }

    /**
     * 적립 예정 포인트 취소 (서비스 간 통신용)
     *
     * @param userId  회원 ID
     * @param request 적립 예정 포인트 취소 요청 {@link CancelPendingPointRequest}
     * @return 적립 예정 포인트 취소 응답 {@link UpdateUserPointResponse}
     */
    @PostMapping("/{userId}/points/pending/cancel")
    public ResponseEntity<BaseResponse<UpdateUserPointResponse>> cancelPendingPoint(
            @PathVariable("userId") Long userId,
            @RequestBody @Valid CancelPendingPointRequest request
    ) {
        UpdateUserPointResult result = cancelPendingPointUseCase.cancelPending(
                PointRequestToCommandMapper.mapToCancelPendingPointCommand(userId, request)
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        UpdateUserPointResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "적립 예정 포인트 취소 성공"
                )
        );
    }
}
