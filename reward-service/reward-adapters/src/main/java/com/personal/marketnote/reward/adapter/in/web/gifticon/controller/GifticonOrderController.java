package com.personal.marketnote.reward.adapter.in.web.gifticon.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.PurchaseGifticonApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.request.PurchaseGifticonRequest;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.PurchaseGifticonResponse;
import com.personal.marketnote.reward.port.in.command.gifticon.PurchaseGifticonCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.PurchaseGifticonResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.PurchaseGifticonUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

@RestController
@RequestMapping("/api/v1/gifticons/orders")
@RequiredArgsConstructor
@Tag(name = "기프티콘 주문 API")
public class GifticonOrderController {

    private final PurchaseGifticonUseCase purchaseGifticonUseCase;

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
}
