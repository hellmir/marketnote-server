package com.personal.marketnote.commerce.adapter.in.web.order.controller;

import com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs.GetOrderProductsApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.order.response.GetMyOrderProductsResponse;
import com.personal.marketnote.commerce.port.in.command.order.GetBuyerOrderProductsQuery;
import com.personal.marketnote.commerce.port.in.result.order.GetBuyerOrderProductsResult;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 주문 상품 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 주문 상품 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/order-products")
@Tag(name = "주문 상품 API", description = "주문 상품 관련 API")
@RequiredArgsConstructor
public class OrderProductController {
    private final GetOrderUseCase getOrderUseCase;

    /**
     * 나의 주문 상품 목록 조회
     *
     * @param principal 인증된 사용자 정보
     * @return 주문 상품 목록 조회 응답 {@link GetMyOrderProductsResponse}
     * @Author 성효빈
     * @Date 2026-01-31
     * @Description 나의 주문 상품 목록을 조회합니다.
     */
    @GetMapping("/me")
    @GetOrderProductsApiDocs
    public ResponseEntity<BaseResponse<GetMyOrderProductsResponse>> getMyOrderProducts(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @RequestParam(value = "isReviewed", required = false) Boolean isReviewed
    ) {
        Long buyerId = ElementExtractor.extractUserId(principal);
        GetBuyerOrderProductsResult result = getOrderUseCase.getBuyerOrderProducts(
                GetBuyerOrderProductsQuery.of(buyerId, isReviewed)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetMyOrderProductsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "나의 주문 상품 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
