package com.personal.marketnote.reward.adapter.in.web.gifticon.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.GetGifticonBrandsApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetGifticonBrandsResponse;
import com.personal.marketnote.reward.port.in.command.gifticon.GetGifticonBrandsCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonBrandsResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGifticonBrandsUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

@RestController
@RequestMapping("/api/v1/gifticon")
@Tag(name = "기프티콘 브랜드 API", description = "기프티콘 브랜드 관련 API")
@RequiredArgsConstructor
public class GifticonBrandController {

    private final GetGifticonBrandsUseCase getGifticonBrandsUseCase;

    /**
     * 기프티콘 브랜드 목록 조회
     *
     * @param categoryCode 카테고리 코드
     * @return 해당 카테고리의 브랜드 목록 응답 {@link GetGifticonBrandsResponse}
     * @Author 성효빈
     * @Date 2026-04-05
     * @Description 특정 카테고리에 노출된 판매 중 상품이 있는 브랜드 목록을 조회합니다.
     */
    @GetMapping("/brands")
    @GetGifticonBrandsApiDocs
    public ResponseEntity<BaseResponse<GetGifticonBrandsResponse>> getBrands(
            @RequestParam(name = "category-code") String categoryCode
    ) {
        GetGifticonBrandsResult result = getGifticonBrandsUseCase.getBrands(
                new GetGifticonBrandsCommand(categoryCode)
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetGifticonBrandsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "기프티콘 브랜드 목록 조회 성공"
                )
        );
    }
}
