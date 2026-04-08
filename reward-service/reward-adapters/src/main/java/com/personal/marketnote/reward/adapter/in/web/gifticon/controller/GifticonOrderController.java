package com.personal.marketnote.reward.adapter.in.web.gifticon.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.GetMyGifticonOrderDetailApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.GetMyGifticonOrdersApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.PurchaseGifticonApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.request.PurchaseGifticonRequest;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetMyGifticonOrderDetailResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetMyGifticonOrdersResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.PurchaseGifticonResponse;
import com.personal.marketnote.reward.port.in.command.gifticon.GetMyGifticonOrderDetailCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.GetMyGifticonOrdersCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.PurchaseGifticonCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetMyGifticonOrderDetailResult;
import com.personal.marketnote.reward.port.in.result.gifticon.GetMyGifticonOrdersResult;
import com.personal.marketnote.reward.port.in.result.gifticon.PurchaseGifticonResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetMyGifticonOrderDetailUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetMyGifticonOrdersUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.PurchaseGifticonUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

@RestController
@RequestMapping("/api/v1/gifticons/orders")
@RequiredArgsConstructor
@Tag(name = "기프티콘 주문 API")
public class GifticonOrderController {

    private final PurchaseGifticonUseCase purchaseGifticonUseCase;
    private final GetMyGifticonOrdersUseCase getMyGifticonOrdersUseCase;
    private final GetMyGifticonOrderDetailUseCase getMyGifticonOrderDetailUseCase;

    @PostMapping
    @PurchaseGifticonApiDocs
    public ResponseEntity<BaseResponse<PurchaseGifticonResponse>> purchaseGifticon(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @Valid @RequestBody PurchaseGifticonRequest request
    ) {
        Long userId = ElementExtractor.extractUserId(principal);

        PurchaseGifticonResult result = purchaseGifticonUseCase.purchase(
                new PurchaseGifticonCommand(userId, request.goodsCode())
        );

        PurchaseGifticonResponse response = PurchaseGifticonResponse.from(result);

        return new ResponseEntity<>(
                BaseResponse.of(response, HttpStatus.OK, DEFAULT_SUCCESS_CODE, "기프티콘 구매 성공"),
                HttpStatus.OK
        );
    }

    @GetMapping("/me")
    @GetMyGifticonOrdersApiDocs
    public ResponseEntity<BaseResponse<GetMyGifticonOrdersResponse>> getMyGifticonOrders(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @RequestParam(name = "status", defaultValue = "AVAILABLE") String status,
            @RequestParam(name = "sort", defaultValue = "PURCHASE_LATEST") String sort,
            @RequestParam(name = "cursor", defaultValue = "-1") Long cursor,
            @RequestParam(name = "page-size", defaultValue = "10") @Min(1) @Max(100) int pageSize
    ) {
        Long userId = ElementExtractor.extractUserId(principal);

        GetMyGifticonOrdersResult result = getMyGifticonOrdersUseCase.getMyGifticonOrders(
                new GetMyGifticonOrdersCommand(userId, status, sort, cursor, pageSize)
        );

        GetMyGifticonOrdersResponse response = GetMyGifticonOrdersResponse.from(result);

        return new ResponseEntity<>(
                BaseResponse.of(response, HttpStatus.OK, DEFAULT_SUCCESS_CODE, "내 기프티콘 목록 조회 성공"),
                HttpStatus.OK
        );
    }

    @GetMapping("/me/{orderId}")
    @GetMyGifticonOrderDetailApiDocs
    public ResponseEntity<BaseResponse<GetMyGifticonOrderDetailResponse>> getMyGifticonOrderDetail(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @PathVariable Long orderId
    ) {
        Long userId = ElementExtractor.extractUserId(principal);

        GetMyGifticonOrderDetailResult result = getMyGifticonOrderDetailUseCase.getMyGifticonOrderDetail(
                new GetMyGifticonOrderDetailCommand(userId, orderId)
        );

        GetMyGifticonOrderDetailResponse response = GetMyGifticonOrderDetailResponse.from(result);

        return new ResponseEntity<>(
                BaseResponse.of(response, HttpStatus.OK, DEFAULT_SUCCESS_CODE, "내 기프티콘 상세 조회 성공"),
                HttpStatus.OK
        );
    }
}
