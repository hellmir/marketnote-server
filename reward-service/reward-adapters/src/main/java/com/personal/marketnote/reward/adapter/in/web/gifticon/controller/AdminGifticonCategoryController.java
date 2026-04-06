package com.personal.marketnote.reward.adapter.in.web.gifticon.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.GetAdminGifticonCategoriesApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetAdminGifticonCategoriesResponse;
import com.personal.marketnote.reward.port.in.result.gifticon.GetAdminGifticonCategoriesResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetAdminGifticonCategoriesUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@RestController
@RequestMapping("/api/v1/admin/gifticon/categories")
@Tag(name = "관리자 기프티콘 카테고리 API", description = "관리자 기프티콘 카테고리 관련 API")
@RequiredArgsConstructor
public class AdminGifticonCategoryController {

    private final GetAdminGifticonCategoriesUseCase getAdminGifticonCategoriesUseCase;

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
}
