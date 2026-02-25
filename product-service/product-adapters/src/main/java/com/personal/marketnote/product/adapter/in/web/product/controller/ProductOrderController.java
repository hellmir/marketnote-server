package com.personal.marketnote.product.adapter.in.web.product.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.product.adapter.in.web.cart.controller.apidocs.GetMyOrderingProductsApiDocs;
import com.personal.marketnote.product.adapter.in.web.cart.request.GetMyOrderingProductsRequest;
import com.personal.marketnote.product.adapter.in.web.cart.response.GetMyOrderingProductsResponse;
import com.personal.marketnote.product.adapter.in.web.product.mapper.ProductRequestToCommandMapper;
import com.personal.marketnote.product.port.in.result.product.GetMyOrderProductsResult;
import com.personal.marketnote.product.port.in.usecase.product.GetMyOrderingProductsUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 상품 주문 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-05
 * @Description 상품 주문 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/products/ordering")
@Tag(name = "상품 주문 API", description = "상품 주문 관련 API")
@RequiredArgsConstructor
public class ProductOrderController {
    private final GetMyOrderingProductsUseCase getMyOrderingProductsUseCase;

    /**
     * 회원 주문 대기 상품 목록 조회
     *
     * @param request 주문 대기 상품 목록 조회 요청
     * @return 주문 대기 상품 목록 {@link GetMyOrderingProductsResponse}
     * @Author 성효빈
     * @Date 2026-01-05
     * @Description 회원의 주문 대기 상품 목록을 조회합니다.
     */
    @PostMapping
    @GetMyOrderingProductsApiDocs
    public ResponseEntity<BaseResponse<GetMyOrderingProductsResponse>> getMyOrderingProducts(
            @Valid @RequestBody GetMyOrderingProductsRequest request
    ) {
        GetMyOrderProductsResult result = getMyOrderingProductsUseCase.getMyOrderingProducts(
                ProductRequestToCommandMapper.mapToCommand(request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetMyOrderingProductsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "회원 주문 대기 상품 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
