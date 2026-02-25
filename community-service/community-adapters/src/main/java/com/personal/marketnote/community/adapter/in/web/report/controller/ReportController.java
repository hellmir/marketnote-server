package com.personal.marketnote.community.adapter.in.web.report.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.community.adapter.in.web.report.ReportRequestToCommandMapper;
import com.personal.marketnote.community.adapter.in.web.report.apidocs.*;
import com.personal.marketnote.community.adapter.in.web.report.request.RegisterReportRequest;
import com.personal.marketnote.community.adapter.in.web.report.request.UpdateTargetStatusRequest;
import com.personal.marketnote.community.adapter.in.web.report.response.GetReportsResponse;
import com.personal.marketnote.community.adapter.in.web.report.response.UpdateTargetStatusResponse;
import com.personal.marketnote.community.domain.report.ReportTargetType;
import com.personal.marketnote.community.port.in.result.report.GetReportsResult;
import com.personal.marketnote.community.port.in.usecase.report.GetReportUseCase;
import com.personal.marketnote.community.port.in.usecase.report.RegisterReportUseCase;
import com.personal.marketnote.community.port.in.usecase.report.UpdateTargetStatusUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 신고 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-13
 * @Description 신고 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "신고 API", description = "신고 관련 API")
@RequiredArgsConstructor
public class ReportController {
    private final RegisterReportUseCase registerReportUseCase;
    private final GetReportUseCase getReportUseCase;
    private final UpdateTargetStatusUseCase updateTargetStatusUseCase;

    /**
     * 리뷰 신고
     *
     * @param id        리뷰 ID
     * @param request   리뷰 신고 요청
     * @param principal 인증된 사용자 정보
     * @Author 성효빈
     * @Date 2026-01-12
     * @Description 상품 리뷰를 신고합니다.
     */
    @PostMapping("/reviews/{id}/reports")
    @ReportReviewApiDocs
    public ResponseEntity<BaseResponse<Void>> reportReview(
            @PathVariable("id") Long id,
            @Valid @RequestBody RegisterReportRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        registerReportUseCase.report(
                ReportRequestToCommandMapper.mapToCommand(
                        ReportTargetType.REVIEW,
                        id,
                        ElementExtractor.extractUserId(principal),
                        request
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "리뷰 신고 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * 게시글 신고
     *
     * @param id        게시글 ID
     * @param request   문의글 신고 요청
     * @param principal 인증된 사용자 정보
     * @Author 성효빈
     * @Date 2026-01-13
     * @Description 게시글을 신고합니다.
     */
    @PostMapping("/posts/{id}/reports")
    @ReportPostApiDocs
    public ResponseEntity<BaseResponse<Void>> reportProductInqueryPost(
            @PathVariable("id") Long id,
            @Valid @RequestBody RegisterReportRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        registerReportUseCase.report(
                ReportRequestToCommandMapper.mapToCommand(
                        ReportTargetType.POST,
                        id,
                        ElementExtractor.extractUserId(principal),
                        request
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "게시글 신고 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * (관리자) 리뷰 신고 내역 조회
     *
     * @param id 리뷰 ID
     * @Author 성효빈
     * @Date 2026-01-12
     * @Description 상품 리뷰 신고 내역을 조회합니다.
     */
    @GetMapping("/reviews/{id}/reports")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetReviewReportsApiDocs
    public ResponseEntity<BaseResponse<GetReportsResponse>> getReviewReports(
            @PathVariable("id") Long id
    ) {
        GetReportsResult result = GetReportsResult.from(
                getReportUseCase.getReports(ReportTargetType.REVIEW, id)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetReportsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "리뷰 신고 내역 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 게시글 신고 내역 조회
     *
     * @param id 게시글 ID
     * @Author 성효빈
     * @Date 2026-01-13
     * @Description 게시글 신고 내역을 조회합니다.
     */
    @GetMapping("/posts/{id}/reports")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetPostReportsApiDocs
    public ResponseEntity<BaseResponse<GetReportsResponse>> getPostReports(
            @PathVariable("id") Long id
    ) {
        GetReportsResult result = GetReportsResult.from(
                getReportUseCase.getReports(ReportTargetType.POST, id)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetReportsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "게시글 신고 내역 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 대상 리뷰/게시글 노출/숨기기
     *
     * @param request 대상 리뷰/게시글 노출/숨기기 요청
     * @Author 성효빈
     * @Date 2026-01-15
     * @Description 대상 리뷰/게시글을 숨기거나 복구합니다.
     */
    @PatchMapping("/status")
    @PreAuthorize(ADMIN_POINTCUT)
    @UpdateTargetStatusApiDocs
    public ResponseEntity<BaseResponse<UpdateTargetStatusResponse>> updateTargetStatus(
            @Valid @RequestBody UpdateTargetStatusRequest request
    ) {
        updateTargetStatusUseCase.updateTargetStatus(
                ReportRequestToCommandMapper.mapToCommand(request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        UpdateTargetStatusResponse.of(request.isVisible()),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "대상 노출/숨기기 성공"
                ),
                HttpStatus.OK
        );
    }
}
