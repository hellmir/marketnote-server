package com.personal.marketnote.reward.adapter.in.web.gifticon.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.GetGifticonCategoriesApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetGifticonCategoriesResponse;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonCategoriesResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGifticonCategoriesUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

@RestController
@RequestMapping("/api/v1/gifticon")
@Tag(name = "기프티콘 카테고리 API", description = "기프티콘 카테고리 관련 API")
@RequiredArgsConstructor
public class GifticonCategoryController {

    private final GetGifticonCategoriesUseCase getGifticonCategoriesUseCase;

    /**
     * 기프티콘 카테고리 목록 조회
     *
     * @return 노출된 카테고리 목록 응답 {@link GetGifticonCategoriesResponse}
     * @Author 성효빈
     * @Date 2026-04-05
     * @Description 노출 설정된 기프티콘 카테고리 목록을 조회합니다.
     */
    @GetMapping("/categories")
    @GetGifticonCategoriesApiDocs
    public ResponseEntity<BaseResponse<GetGifticonCategoriesResponse>> getCategories() {
        GetGifticonCategoriesResult result = getGifticonCategoriesUseCase.getCategories();

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetGifticonCategoriesResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "기프티콘 카테고리 목록 조회 성공"
                )
        );
    }
}
