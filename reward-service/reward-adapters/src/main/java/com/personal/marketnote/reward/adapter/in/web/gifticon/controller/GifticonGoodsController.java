package com.personal.marketnote.reward.adapter.in.web.gifticon.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.GetGifticonGoodsApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.GetGifticonGoodsDetailApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.GetPopularGifticonGoodsApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetGifticonGoodsDetailResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetPopularGifticonGoodsResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetGifticonGoodsResponse;
import com.personal.marketnote.reward.port.in.command.gifticon.GetGifticonGoodsCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.GetGifticonGoodsDetailCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonGoodsDetailResult;
import com.personal.marketnote.reward.port.in.result.gifticon.GetPopularGifticonGoodsResult;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonGoodsResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGifticonGoodsDetailUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetPopularGifticonGoodsUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGifticonGoodsUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.DEFAULT_PAGE_NUMBER;

@RestController
@RequestMapping("/api/v1/gifticon")
@Tag(name = "기프티콘 상품 API", description = "기프티콘 상품 관련 API")
@RequiredArgsConstructor
public class GifticonGoodsController {

    private final GetGifticonGoodsUseCase getGifticonGoodsUseCase;
    private final GetGifticonGoodsDetailUseCase getGifticonGoodsDetailUseCase;
    private final GetPopularGifticonGoodsUseCase getPopularGifticonGoodsUseCase;

    /**
     * 기프티콘 상품 목록 조회
     *
     * @param categoryCode 카테고리 코드 (선택)
     * @param brandCode    브랜드 코드 (선택)
     * @param page         페이지 번호 (기본: 1)
     * @param pageSize     페이지 크기 (기본: 20)
     * @return 상품 목록 응답 {@link GetGifticonGoodsResponse}
     * @Author 성효빈
     * @Date 2026-04-05
     * @Description 노출 설정 및 판매 중인 기프티콘 상품 목록을 페이징으로 조회합니다.
     */
    @GetMapping("/goods")
    @GetGifticonGoodsApiDocs
    public ResponseEntity<BaseResponse<GetGifticonGoodsResponse>> getGoods(
            @RequestParam(name = "category-code", required = false) String categoryCode,
            @RequestParam(name = "brand-code", required = false) String brandCode,
            @RequestParam(name = "page", required = false, defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(name = "page-size", required = false, defaultValue = "20") int pageSize
    ) {
        GetGifticonGoodsResult result = getGifticonGoodsUseCase.getGoods(
                new GetGifticonGoodsCommand(categoryCode, brandCode, page, pageSize)
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetGifticonGoodsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "기프티콘 상품 목록 조회 성공"
                )
        );
    }

    /**
     * 기프티콘 인기 상품 목록 조회
     *
     * @return 인기 상품 목록 응답 {@link GetPopularGifticonGoodsResponse}
     * @Author 성효빈
     * @Date 2026-04-05
     * @Description 인기 설정된 기프티콘 상품 목록을 조회합니다. 최대 10개.
     */
    @GetMapping("/goods/popular")
    @GetPopularGifticonGoodsApiDocs
    public ResponseEntity<BaseResponse<GetPopularGifticonGoodsResponse>> getPopularGoods() {
        GetPopularGifticonGoodsResult result = getPopularGifticonGoodsUseCase.getPopularGoods();

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetPopularGifticonGoodsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "기프티콘 인기 상품 목록 조회 성공"
                )
        );
    }

    /**
     * 기프티콘 상품 상세 조회
     *
     * @param goodsCode 상품 코드
     * @param principal 인증된 사용자 정보
     * @return 상품 상세 응답 {@link GetGifticonGoodsDetailResponse}
     * @Author 성효빈
     * @Date 2026-04-05
     * @Description 기프티콘 상품 상세 정보와 사용자 캐시 잔액을 조회합니다.
     */
    @GetMapping("/goods/{goodsCode}")
    @GetGifticonGoodsDetailApiDocs
    public ResponseEntity<BaseResponse<GetGifticonGoodsDetailResponse>> getGoodsDetail(
            @PathVariable("goodsCode") String goodsCode,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long userId = ElementExtractor.extractUserId(principal);
        GetGifticonGoodsDetailResult result = getGifticonGoodsDetailUseCase.getGoodsDetail(
                new GetGifticonGoodsDetailCommand(goodsCode, userId)
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetGifticonGoodsDetailResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "기프티콘 상품 상세 조회 성공"
                )
        );
    }
}
