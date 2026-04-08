package com.personal.marketnote.reward.adapter.in.web.gifticon.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.GetAdminGifticonGoodsApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.ManageGifticonGoodsExposureApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.ManageFeaturedGifticonGoodsApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.ManageGifticonGoodsOrderApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.request.ManageFeaturedGifticonGoodsRequest;
import com.personal.marketnote.reward.adapter.in.web.gifticon.request.ManageGifticonGoodsExposureRequest;
import com.personal.marketnote.reward.adapter.in.web.gifticon.request.ManageGifticonGoodsOrderRequest;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetAdminGifticonGoodsResponse;
import com.personal.marketnote.reward.port.in.command.gifticon.GetAdminGifticonGoodsCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageFeaturedGifticonGoodsCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageFeaturedGifticonGoodsCommand.FeaturedGoodsItem;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsExposureCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsExposureCommand.ExposureItem;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsOrderCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsOrderCommand.OrderItem;
import com.personal.marketnote.reward.port.in.result.gifticon.GetAdminGifticonGoodsResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetAdminGifticonGoodsUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.ManageFeaturedGifticonGoodsUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.ManageGifticonGoodsExposureUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.ManageGifticonGoodsOrderUseCase;
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
    private final ManageGifticonGoodsOrderUseCase manageGifticonGoodsOrderUseCase;
    private final ManageFeaturedGifticonGoodsUseCase manageFeaturedGifticonGoodsUseCase;

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

    /**
     * (관리자) 기프티콘 상품 노출 순서 관리
     *
     * @param request 순서 관리 요청
     * @Author 성효빈
     * @Date 2026-04-05
     * @Description 관리자가 노출 상품의 정렬 순서를 설정합니다.
     */
    @PatchMapping("/order")
    @PreAuthorize(ADMIN_POINTCUT)
    @ManageGifticonGoodsOrderApiDocs
    public ResponseEntity<BaseResponse<Void>> manageGifticonGoodsOrder(
            @Valid @RequestBody ManageGifticonGoodsOrderRequest request
    ) {
        ManageGifticonGoodsOrderCommand command = new ManageGifticonGoodsOrderCommand(
                request.items().stream()
                        .map(item -> new OrderItem(item.goodsCode(), item.orderNum()))
                        .toList()
        );

        manageGifticonGoodsOrderUseCase.manageOrder(command);

        return ResponseEntity.ok(
                BaseResponse.of(
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "기프티콘 상품 노출 순서 관리 성공"
                )
        );
    }

    /**
     * (관리자) 기프티콘 인기상품 관리
     *
     * @param request 인기상품 관리 요청
     * @Author 성효빈
     * @Date 2026-04-06
     * @Description 관리자가 기프티콘 상품의 인기상품 여부와 정렬 순서를 관리합니다.
     */
    @PatchMapping("/featured")
    @PreAuthorize(ADMIN_POINTCUT)
    @ManageFeaturedGifticonGoodsApiDocs
    public ResponseEntity<BaseResponse<Void>> manageFeaturedGifticonGoods(
            @Valid @RequestBody ManageFeaturedGifticonGoodsRequest request
    ) {
        ManageFeaturedGifticonGoodsCommand command = new ManageFeaturedGifticonGoodsCommand(
                request.items().stream()
                        .map(item -> new FeaturedGoodsItem(item.goodsCode(), item.popular(), item.popularOrderNum()))
                        .toList()
        );

        manageFeaturedGifticonGoodsUseCase.manageFeatured(command);

        return ResponseEntity.ok(
                BaseResponse.of(
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "기프티콘 인기상품 관리 성공"
                )
        );
    }
}
