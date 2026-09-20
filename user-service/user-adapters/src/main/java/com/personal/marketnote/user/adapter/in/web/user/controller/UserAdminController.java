package com.personal.marketnote.user.adapter.in.web.user.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.user.adapter.in.web.user.controller.apidocs.ApplyUserPenaltyApiDocs;
import com.personal.marketnote.user.adapter.in.web.user.controller.apidocs.GetLoginHistoriesApiDocs;
import com.personal.marketnote.user.adapter.in.web.user.controller.apidocs.GetUserInfoApiDocs;
import com.personal.marketnote.user.adapter.in.web.user.controller.apidocs.GetUsersApiDocs;
import com.personal.marketnote.user.adapter.in.web.user.controller.apidocs.UpdateUserInfoApiDocs;
import com.personal.marketnote.user.adapter.in.web.user.mapper.UserRequestToCommandMapper;
import com.personal.marketnote.user.adapter.in.web.user.request.ApplyUserPenaltyRequest;
import com.personal.marketnote.user.adapter.in.web.user.request.UpdateUserInfoRequest;
import com.personal.marketnote.user.adapter.in.web.user.response.ApplyUserPenaltyResponse;
import com.personal.marketnote.user.adapter.in.web.user.response.GetLoginHistoriesResponse;
import com.personal.marketnote.user.adapter.in.web.user.response.GetUserInfoResponse;
import com.personal.marketnote.user.adapter.in.web.user.response.GetUsersResponse;
import com.personal.marketnote.user.domain.user.LoginHistorySortProperty;
import com.personal.marketnote.user.domain.user.UserSearchTarget;
import com.personal.marketnote.user.domain.user.UserSortProperty;
import com.personal.marketnote.user.port.in.command.ApplyUserPenaltyCommand;
import com.personal.marketnote.user.port.in.result.ApplyUserPenaltyResult;
import com.personal.marketnote.user.port.in.usecase.user.ApplyUserPenaltyUseCase;
import com.personal.marketnote.user.port.in.usecase.user.GetLoginHistoryUseCase;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.in.usecase.user.UpdateUserUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;
import static com.personal.marketnote.common.utility.ApiConstant.DEFAULT_PAGE_NUMBER;

/**
 * 회원 관리자 컨트롤러
 *
 * @Author 성효빈
 * @Date 2025-12-29
 * @Description 회원 관련 관리자 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(
        name = "회원 관리자 API",
        description = "회원 관련 관리자 API"
)
@RequiredArgsConstructor
@Slf4j
public class UserAdminController {
    private static final String GET_USERS_DEFAULT_PAGE_SIZE = "10";
    private static final String GET_LOGIN_HISTORIES_DEFAULT_PAGE_SIZE = "20";

    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final GetLoginHistoryUseCase getLoginHistoryUseCase;
    private final ApplyUserPenaltyUseCase applyUserPenaltyUseCase;

    /**
     * (관리자) 회원 목록 조회
     *
     * @param pageSize      페이지 크기
     * @param pageNumber    페이지 번호
     * @param sortDirection 정렬 방향
     * @param sortProperty  정렬 속성
     * @param searchTarget  검색 대상
     * @param searchKeyword 검색 키워드
     * @return 회원 목록 조회 응답 {@link GetUsersResponse}
     * @Author 성효빈
     * @Date 2025-12-29
     * @Description 회원 정보를 조회합니다. 관리자만 가능합니다.
     */
    @GetMapping
    @PreAuthorize(ADMIN_POINTCUT)
    @GetUsersApiDocs
    public ResponseEntity<BaseResponse<GetUsersResponse>> getUsers(
            @RequestParam(required = false, defaultValue = GET_USERS_DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGE_NUMBER) int pageNumber,
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(required = false, defaultValue = "ORDER_NUM") UserSortProperty sortProperty,
            @RequestParam(required = false, defaultValue = "EMAIL") UserSearchTarget searchTarget,
            @RequestParam(required = false) String searchKeyword
    ) {
        GetUsersResponse getUsersResponse = GetUsersResponse.from(
                getUserUseCase.getAllStatusUsers(
                        pageSize,
                        pageNumber - 1,
                        sortDirection,
                        sortProperty,
                        searchTarget,
                        searchKeyword
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        getUsersResponse,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "회원 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 회원 정보 조회
     *
     * @param id 회원 ID
     * @return 회원 정보 조회 응답 {@link GetUserInfoResponse}
     * @Author 성효빈
     * @Date 2025-12-29
     * @Description 회원 정보를 조회합니다. 관리자만 가능합니다.
     */
    @GetMapping("/{id}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetUserInfoApiDocs
    public ResponseEntity<BaseResponse<GetUserInfoResponse>> getUserInfo(@PathVariable Long id) {
        GetUserInfoResponse getUserInfoResponse = GetUserInfoResponse.from(
                getUserUseCase.getAllStatusUserInfo(id)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        getUserInfoResponse,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "회원 정보 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 회원 정보 수정
     *
     * @param updateUserInfoRequest 회원 정보 수정 요청
     * @return 회원 정보 수정 응답 {@link Void}
     * @Author 성효빈
     * @Date 2025-12-29
     * @Description 계정 비활성화를 포함한 회원 정보를 수정합니다. 관리자만 가능합니다.
     */
    @PatchMapping("/{id}")
    @PreAuthorize(ADMIN_POINTCUT)
    @UpdateUserInfoApiDocs
    public ResponseEntity<BaseResponse<Void>> updateUserInfo(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserInfoRequest updateUserInfoRequest
    ) {
        updateUserUseCase.updateUserInfo(
                true, id, UserRequestToCommandMapper.mapToCommand(updateUserInfoRequest)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "회원 정보 수정 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 특정 회원의 로그인 내역 조회
     *
     * @param userId        회원 ID
     * @param pageSize      페이지 크기
     * @param pageNumber    페이지 번호
     * @param sortDirection 정렬 방향
     * @param sortProperty  정렬 속성
     * @return 로그인 내역 목록 조회 응답 {@link GetLoginHistoriesResponse}
     * @Author 시스템
     * @Date 2025-12-30
     * @Description 특정 회원의 로그인 내역을 조회합니다. 관리자만 가능합니다.
     */
    @GetMapping("/{userId}/login-histories")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetLoginHistoriesApiDocs
    public ResponseEntity<BaseResponse<GetLoginHistoriesResponse>> getLoginHistories(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = GET_LOGIN_HISTORIES_DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGE_NUMBER) int pageNumber,
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(required = false, defaultValue = "ID") LoginHistorySortProperty sortProperty
    ) {
        GetLoginHistoriesResponse response = com.personal.marketnote.user.adapter.in.web.user.response.GetLoginHistoriesResponse.from(
                getLoginHistoryUseCase.getLoginHistories(
                        userId,
                        pageSize,
                        pageNumber - 1,
                        sortDirection,
                        sortProperty)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        response,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "회원 로그인 내역 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 회원 패널티 부과
     *
     * @param userId    회원 ID
     * @param request   패널티 부과 요청
     * @param principal 인증된 관리자
     * @return 회원 패널티 부과 응답 {@link ApplyUserPenaltyResponse}
     * @Author 성효빈
     * @Date 2026-09-20
     * @Description 특정 회원에게 패널티를 부과합니다. 관리자만 가능합니다.
     */
    @PostMapping("/{userId}/penalties")
    @PreAuthorize(ADMIN_POINTCUT)
    @ApplyUserPenaltyApiDocs
    public ResponseEntity<BaseResponse<ApplyUserPenaltyResponse>> applyUserPenalty(
            @PathVariable Long userId,
            @Valid @RequestBody ApplyUserPenaltyRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long adminId = ElementExtractor.extractUserId(principal);
        ApplyUserPenaltyResult result = applyUserPenaltyUseCase.applyPenalty(
                new ApplyUserPenaltyCommand(userId, adminId, request.reason())
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        ApplyUserPenaltyResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "회원 패널티 부과 성공"
                ),
                HttpStatus.OK
        );
    }
}
