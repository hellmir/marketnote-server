package com.personal.marketnote.reward.adapter.in.web.gifticon.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.GetAdminGifticonGoodsApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.ManageGifticonGoodsExposureApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.request.ManageGifticonGoodsExposureRequest;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetAdminGifticonGoodsResponse;
import com.personal.marketnote.reward.port.in.command.gifticon.GetAdminGifticonGoodsCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsExposureCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsExposureCommand.ExposureItem;
import com.personal.marketnote.reward.port.in.result.gifticon.GetAdminGifticonGoodsResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetAdminGifticonGoodsUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.ManageGifticonGoodsExposureUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@RestController
@RequestMapping("/api/v1/admin/gifticon/goods")
@Tag(name = "관리자 기프티콘 상품 API", description = "관리자 기프티콘 상품 관련 API")
@RequiredArgsConstructor
public class AdminGifticonGoodsController {

    private final GetAdminGifticonGoodsUseCase getAdminGifticonGoodsUseCase;
    private final ManageGifticonGoodsExposureUseCase manageGifticonGoodsExposureUseCase;

    /**
     * (관리자) 기프티콘 전체 상품 목록 조회
     *
     * @Author 성효빈
     * @Date 2026-04-05
     * @Description 관리자가 기프티쇼에서 동기화된 전체 기프티콘 상품을 조회합니다.
     */
    @GetMapping
    @PreAuthorize(ADMIN_POINTCUT)
    @GetAdminGifticonGoodsApiDocs
    public ResponseEntity<BaseResponse<GetAdminGifticonGoodsResponse>> getAdminGifticonGoods(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "page-size", defaultValue = "20") int pageSize,
            @RequestParam(value = "goods-status", required = false) String goodsStatus,
            @RequestParam(value = "exposed", required = false) Boolean exposed,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        GetAdminGifticonGoodsCommand command = new GetAdminGifticonGoodsCommand(
                page, pageSize, goodsStatus, exposed, keyword
        );

        GetAdminGifticonGoodsResult result = getAdminGifticonGoodsUseCase.getAdminGifticonGoods(command);

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetAdminGifticonGoodsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "관리자 기프티콘 상품 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 기프티콘 노출 상품 관리
     *
     * @param request 노출 관리 요청
     * @Author 성효빈
     * @Date 2026-04-05
     * @Description 관리자가 기프티콘 상품의 노출 여부를 변경합니다.
     */
    @PatchMapping("/exposure")
    @PreAuthorize(ADMIN_POINTCUT)
    @ManageGifticonGoodsExposureApiDocs
    public ResponseEntity<BaseResponse<Void>> manageGifticonGoodsExposure(
            @Valid @RequestBody ManageGifticonGoodsExposureRequest request
    ) {
        ManageGifticonGoodsExposureCommand command = new ManageGifticonGoodsExposureCommand(
                request.items().stream()
                        .map(item -> new ExposureItem(item.goodsCode(), item.exposed()))
                        .toList()
        );

        manageGifticonGoodsExposureUseCase.manageExposure(command);

        return ResponseEntity.ok(
                BaseResponse.of(
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "기프티콘 상품 노출 관리 성공"
                )
        );
    }
}
