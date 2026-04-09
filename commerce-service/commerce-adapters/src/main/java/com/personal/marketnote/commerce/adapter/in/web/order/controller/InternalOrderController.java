package com.personal.marketnote.commerce.adapter.in.web.order.controller;

import com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs.GetInternalOrderProductApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs.VerifyOrderOwnershipApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.order.response.InternalOrderProductResponse;
import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 내부 주문 컨트롤러 (서비스 간 통신용)
 *
 * @Author 성효빈
 * @Date 2026-04-05
 * @Description HMAC 인증 기반 서비스 간 통신용 주문 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/internal/orders")
@Tag(
        name = "내부 주문 API",
        description = "서비스 간 통신용 주문 API"
)
@RequiredArgsConstructor
@Slf4j
public class InternalOrderController {
    private final GetOrderUseCase getOrderUseCase;

    /**
     * 주문 소유권 확인 (서비스 간 통신용)
     *
     * @param orderId 주문 ID
     * @param buyerId 구매자 ID
     */
    @VerifyOrderOwnershipApiDocs
    @GetMapping("/{orderId}/ownership")
    public ResponseEntity<BaseResponse<Void>> verifyOrderOwnership(
            @PathVariable Long orderId,
            @RequestParam Long buyerId
    ) {
        Order order = getOrderUseCase.getOrder(orderId);

        if (!order.isBuyer(buyerId)) {
            log.warn("주문 소유자 불일치 - orderId: {}, 요청자: {}", orderId, buyerId);
            throw new UnauthorizedOrderAccessException();
        }

        return new ResponseEntity<>(
                BaseResponse.of(
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "주문 소유권 확인 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 주문 상품 조회 (서비스 간 통신용)
     *
     * @param orderId       주문 ID
     * @param pricePolicyId 가격 정책 ID
     */
    @GetInternalOrderProductApiDocs
    @GetMapping("/{orderId}/order-products/{pricePolicyId}")
    public ResponseEntity<BaseResponse<InternalOrderProductResponse>> getOrderProduct(
            @PathVariable Long orderId,
            @PathVariable Long pricePolicyId
    ) {
        OrderProduct orderProduct = getOrderUseCase.getOrderProduct(orderId, pricePolicyId);

        return new ResponseEntity<>(
                BaseResponse.of(
                        InternalOrderProductResponse.from(orderProduct),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "주문 상품 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
