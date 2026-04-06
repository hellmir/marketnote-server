package com.personal.marketnote.reward.adapter.in.web.gifticon.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.GetAdminGifticonCategoriesApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.UpdateGifticonCategoryApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.request.UpdateGifticonCategoryRequest;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetAdminGifticonCategoriesResponse;
import com.personal.marketnote.reward.port.in.result.gifticon.GetAdminGifticonCategoriesResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetAdminGifticonCategoriesUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.UpdateGifticonCategoryUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@RestController
@RequestMapping("/api/v1/admin/gifticon/categories")
@Tag(name = "관리자 기프티콘 카테고리 API", description = "관리자 기프티콘 카테고리 관련 API")
@RequiredArgsConstructor
public class AdminGifticonCategoryController {

    private final GetAdminGifticonCategoriesUseCase getAdminGifticonCategoriesUseCase;
    private final UpdateGifticonCategoryUseCase updateGifticonCategoryUseCase;

    /**
     * (관리자) 기프티콘 카테고리 목록 조회
     *
     * @Author 성효빈
     * @Date 2026-04-05
     * @Description 관리자가 전체 기프티콘 카테고리를 조회합니다.
     */
    @GetMapping
    @PreAuthorize(ADMIN_POINTCUT)
    @GetAdminGifticonCategoriesApiDocs
    public ResponseEntity<BaseResponse<GetAdminGifticonCategoriesResponse>> getAdminGifticonCategories() {
        GetAdminGifticonCategoriesResult result = getAdminGifticonCategoriesUseCase.getAdminGifticonCategories();

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetAdminGifticonCategoriesResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "관리자 기프티콘 카테고리 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 기프티콘 카테고리 수정
     *
     * @param categoryId 카테고리 ID
     * @param request 카테고리 수정 요청
     * @Author 성효빈
     * @Date 2026-04-05
     * @Description 기프티콘 카테고리의 표시명과 아이콘 URL을 수정합니다.
     */
    @PatchMapping("/{categoryId}")
    @PreAuthorize(ADMIN_POINTCUT)
    @UpdateGifticonCategoryApiDocs
    public ResponseEntity<BaseResponse<Void>> updateGifticonCategory(
            @PathVariable("categoryId") Long categoryId,
            @RequestBody UpdateGifticonCategoryRequest request
    ) {
        updateGifticonCategoryUseCase.updateGifticonCategory(request.toCommand(categoryId));

        return ResponseEntity.ok(
                BaseResponse.of(
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "기프티콘 카테고리 수정 성공"
                )
        );
    }
}
