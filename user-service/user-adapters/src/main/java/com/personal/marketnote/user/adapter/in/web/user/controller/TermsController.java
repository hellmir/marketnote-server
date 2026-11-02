package com.personal.marketnote.user.adapter.in.web.user.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.user.adapter.in.web.user.controller.apidocs.AcceptOrCancelTermsApiDocs;
import com.personal.marketnote.user.adapter.in.web.user.controller.apidocs.GetAllTermsApiDocs;
import com.personal.marketnote.user.adapter.in.web.user.controller.apidocs.GetUserTermsApiDocs;
import com.personal.marketnote.user.adapter.in.web.user.mapper.TermsRequestToCommandMapper;
import com.personal.marketnote.user.adapter.in.web.user.request.AcceptOrCancelTermsRequest;
import com.personal.marketnote.user.adapter.in.web.user.response.GetTermsResponse;
import com.personal.marketnote.user.adapter.in.web.user.response.GetUserTermsResponse;
import com.personal.marketnote.user.port.in.result.GetUserTermsResult;
import com.personal.marketnote.user.port.in.usecase.terms.GetTermsUseCase;
import com.personal.marketnote.user.port.in.usecase.terms.UpdateTermsUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 회원 약관 컨트롤러
 *
 * @Author 성효빈
 * @Date 2025-12-28
 * @Description 회원 약관 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/terms")
@Tag(
        name = "회원 약관 API",
        description = "회원 약관 관련 API"
)
@RequiredArgsConstructor
@Slf4j
public class TermsController {
    private final GetTermsUseCase getTermsUseCase;
    private final UpdateTermsUseCase updateTermsUseCase;

    /**
     * 전체 약관 목록 조회
     *
     * @return 전체 약관 목록 응답 {@link GetTermsResponse}
     * @Author 성효빈
     * @Date 2025-12-28
     * @Description 전체 약관 목록을 조회합니다.
     */
    @GetMapping()
    @GetAllTermsApiDocs
    public ResponseEntity<BaseResponse<GetTermsResponse>> getAllTerms() {
        GetTermsResponse getTermsResponse = GetTermsResponse.from(
                getTermsUseCase.getAllTerms()
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        getTermsResponse,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "전체 약관 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 회원 약관 동의 여부 목록 조회
     *
     * @param principal OAuth2 인증 정보
     * @return 회원 약관 동의 여부 목록 응답 {@link GetUserTermsResponse}
     * @Author 성효빈
     * @Date 2025-12-28
     * @Description 회원 약관 동의 여부 목록을 조회합니다.
     */
    @GetMapping("/user")
    @GetUserTermsApiDocs
    public ResponseEntity<BaseResponse<GetUserTermsResponse>> getUserTerms(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        GetUserTermsResult getUserTermsResult = getTermsUseCase.getUserTerms(
                ElementExtractor.extractUserId(principal)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetUserTermsResponse.from(getUserTermsResult),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "약관 동의/철회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 약관 동의 또는 철회
     *
     * @param acceptOrCancelTermsRequest 약관 동의 또는 철회 요청
     * @param principal                  OAuth2 인증 정보
     * @return 약관 동의 또는 철회 응답 {@link GetUserTermsResponse}
     * @Author 성효빈
     * @Date 2025-12-28
     * @Description 약관 동의 또는 철회를 수행합니다.
     */
    @PatchMapping("/user")
    @AcceptOrCancelTermsApiDocs
    public ResponseEntity<BaseResponse<GetUserTermsResponse>> acceptOrCancelTerms(
            @Valid @RequestBody AcceptOrCancelTermsRequest acceptOrCancelTermsRequest,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        GetUserTermsResult getUserTermsResult = updateTermsUseCase.acceptOrCancelTerms(
                ElementExtractor.extractUserId(principal),
                TermsRequestToCommandMapper.mapToCommand(acceptOrCancelTermsRequest)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetUserTermsResponse.from(getUserTermsResult),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "약관 동의/철회 성공"
                ),
                HttpStatus.OK
        );
    }
}
