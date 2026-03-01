package com.personal.marketnote.reward.adapter.in.web.point;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.Role;
import com.personal.marketnote.reward.adapter.in.web.point.apidocs.GetMyPointApiDocs;
import com.personal.marketnote.reward.adapter.in.web.point.apidocs.GetUserPointByIdApiDocs;
import com.personal.marketnote.reward.adapter.in.web.point.apidocs.GetUserPointHistoriesApiDocs;
import com.personal.marketnote.reward.adapter.in.web.point.apidocs.ModifyUserPointApiDocs;
import com.personal.marketnote.reward.adapter.in.web.point.apidocs.RegisterUserPointApiDocs;
import com.personal.marketnote.reward.adapter.in.web.point.mapper.PointRequestToCommandMapper;
import com.personal.marketnote.reward.adapter.in.web.point.request.ModifyUserPointRequest;
import com.personal.marketnote.reward.adapter.in.web.point.response.GetMyPointReponse;
import com.personal.marketnote.reward.adapter.in.web.point.response.GetUserPointByIdResponse;
import com.personal.marketnote.reward.adapter.in.web.point.response.GetUserPointHistoryResponse;
import com.personal.marketnote.reward.adapter.in.web.point.response.UpdateUserPointResponse;
import com.personal.marketnote.reward.domain.point.UserPointHistoryFilter;
import com.personal.marketnote.reward.port.in.command.point.RegisterUserPointCommand;
import com.personal.marketnote.reward.port.in.result.point.GetUserPointHistoryResult;
import com.personal.marketnote.reward.port.in.result.point.GetUserPointResult;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointHistoryUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyUserPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.RegisterUserPointUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "포인트 API", description = "회원 포인트 관련 API")
@RequiredArgsConstructor
public class PointController {
    private final RegisterUserPointUseCase registerUserPointUseCase;
    private final ModifyUserPointUseCase modifyUserPointUseCase;
    private final GetUserPointUseCase getUserPointUseCase;
    private final GetUserPointHistoryUseCase getUserPointHistoryUseCase;

    /**
     * (관리자) 회원 포인트 정보 생성
     *
     * @param userId 회원 ID
     * @return 회원 포인트 정보 생성 응답 {@link UpdateUserPointResponse}
     * @Author 성효빈
     * @Date 2026-01-17
     * @Description 회원 포인트 정보를 생성합니다.
     */
    @PostMapping("/{userId}/points")
    @PreAuthorize(ADMIN_POINTCUT)
    @RegisterUserPointApiDocs
    public ResponseEntity<BaseResponse<Void>> registerUserPoint(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "userKey") String userKey
    ) {
        registerUserPointUseCase.register(
                RegisterUserPointCommand.of(userId, userKey)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "회원 포인트 정보 생성 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * 나의 포인트 정보 조회
     *
     * @param principal 인증된 사용자 정보
     * @return 회원 포인트 정보 조회 응답 {@link GetMyPointReponse}
     * @Author 성효빈
     * @Date 2026-01-18
     * @Description 나의 포인트 정보를 조회합니다.
     */
    @GetMapping("/me/points")
    @GetMyPointApiDocs
    public ResponseEntity<BaseResponse<GetMyPointReponse>> getMyPoint(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        GetUserPointResult getUserPointResult = GetUserPointResult.from(
                getUserPointUseCase.getUserPoint(
                        ElementExtractor.extractUserId(principal)
                )
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetMyPointReponse.from(getUserPointResult),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "나의 포인트 정보 조회 성공"
                )
        );
    }

    /**
     * (관리자) 회원 포인트 정보 조회
     *
     * @param userId 회원 ID
     * @return 회원 포인트 정보 조회 응답 {@link GetUserPointByIdResponse}
     * @Author 성효빈
     * @Date 2026-02-20
     * @Description 관리자가 특정 회원의 포인트 정보를 조회합니다.
     */
    @GetMapping("/{userId}/points")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetUserPointByIdApiDocs
    public ResponseEntity<BaseResponse<GetUserPointByIdResponse>> getUserPointById(
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
     * (관리자/본인) 회원 포인트 내역 조회
     *
     * @param principal 인증된 사용자 정보
     * @param filter    포인트 내역 조회 필터 (ALL/ACCRUAL/DEDUCTION)
     * @return 회원 포인트 내역 조회 응답 {@link GetUserPointHistoryResponse}
     */
    @GetMapping("/{userId}/points/histories")
    @GetUserPointHistoriesApiDocs
    public ResponseEntity<BaseResponse<GetUserPointHistoryResponse>> getUserPointHistories(
            @PathVariable("userId") Long userId,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @RequestParam(value = "filter", required = false) UserPointHistoryFilter filter
    ) {
        validateAuthentication(userId, principal);
        GetUserPointHistoryResult result = getUserPointHistoryUseCase.getUserPointHistories(
                userId,
                filter
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetUserPointHistoryResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "회원 포인트 내역 조회 성공"
                )
        );
    }

    private void validateAuthentication(Long userId, OAuth2AuthenticatedPrincipal principal) {
        List<String> authorities = principal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (
                authorities.stream()
                        .anyMatch(Role::isAdmin)
                        || FormatValidator.equals(ElementExtractor.extractUserId(principal), userId)
        ) {
            return;
        }

        throw new AccessDeniedException("권한이 없습니다.");
    }

    /**
     * (관리자) 회원 포인트 적립/차감
     */
    @PatchMapping("/{userId}/points")
    @PreAuthorize(ADMIN_POINTCUT)
    @ModifyUserPointApiDocs
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
}
