package com.personal.marketnote.commerce.adapter.in.web.order.controller;

import com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs.*;
import com.personal.marketnote.commerce.adapter.in.web.order.mapper.OrderRequestToCommandMapper;
import com.personal.marketnote.commerce.adapter.in.web.order.request.ChangeOrderStatusRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.request.RegisterOrderRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.response.*;
import com.personal.marketnote.commerce.domain.order.OrderPeriod;
import com.personal.marketnote.commerce.domain.order.OrderStatusFilter;
import com.personal.marketnote.commerce.port.in.command.order.GetBuyerOrderHistoryQuery;
import com.personal.marketnote.commerce.port.in.command.order.UpdateOrderProductReviewStatusCommand;
import com.personal.marketnote.commerce.port.in.result.order.*;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.RegisterOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.UpdateOrderProductUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 주문 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-05
 * @Description 주문 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "주문 API", description = "주문 관련 API")
@RequiredArgsConstructor
public class OrderController {
    private final RegisterOrderUseCase registerOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
    private final UpdateOrderProductUseCase updateOrderProductUseCase;

    /**
     * 주문 등록
     *
     * @param request   주문 등록 요청
     * @param principal 인증된 사용자 정보
     * @return 주문 등록 응답 {@link RegisterOrderResponse}
     * @Author 성효빈
     * @Date 2026-01-05
     * @Description 주문을 등록합니다.
     */
    @PostMapping
    @RegisterOrderApiDocs
    public ResponseEntity<BaseResponse<RegisterOrderResponse>> registerOrder(
            @Valid @RequestBody RegisterOrderRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long buyerId = ElementExtractor.extractUserId(principal);

        RegisterOrderResult result = registerOrderUseCase.registerOrder(
                OrderRequestToCommandMapper.mapToCommand(request, buyerId)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterOrderResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "주문 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * 주문 키 조회
     *
     * @param id 주문 ID
     * @return 주문 키 조회 응답 {@link GetOrderKeyResponse}
     * @Author 성효빈
     * @Date 2026-02-25
     * @Description 주문 ID로 주문 키를 조회합니다.
     */
    @GetMapping("/{id}/order-key")
    @GetOrderKeyApiDocs
    public ResponseEntity<BaseResponse<GetOrderKeyResponse>> getOrderKey(
            @PathVariable("id") Long id
    ) {
        GetOrderKeyResult getOrderKeyResult = getOrderUseCase.getOrderKey(id);

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetOrderKeyResponse.from(getOrderKeyResult),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "주문 키 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 주문 정보 조회
     *
     * @param id 주문 ID
     * @return 주문 정보 조회 응답 {@link GetOrderResponse}
     * @Author 성효빈
     * @Date 2026-01-05
     * @Description 주문 정보를 조회합니다.
     */
    @GetMapping("/{id}")
    @GetOrderInfoApiDocs
    public ResponseEntity<BaseResponse<GetOrderResponse>> getOrder(@PathVariable("id") Long id) {
        GetOrderResult getOrderResult = getOrderUseCase.getOrderAndOrderProducts(id);

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetOrderResponse.from(getOrderResult),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "주문 정보 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 나의 주문 내역 조회
     *
     * @param principal 인증된 사용자 정보
     * @return 주문 내역 조회 응답 {@link GetMyOrdersResponse}
     * @Author 성효빈
     * @Date 2026-01-05
     * @Description 나의 주문 내역을 조회합니다.
     */
    @GetMapping("/me")
    @GetOrdersApiDocs
    public ResponseEntity<BaseResponse<GetMyOrdersResponse>> getMyOrderHistory(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @RequestParam(value = "period", required = false) OrderPeriod period,
            @RequestParam(value = "status", required = false) OrderStatusFilter statusFilter,
            @RequestParam(value = "productName", required = false) String productName
    ) {
        GetBuyerOrdersResult getBuyerOrderResult = getOrderUseCase.getBuyerOrderHistory(
                GetBuyerOrderHistoryQuery.of(
                        ElementExtractor.extractUserId(principal),
                        period,
                        statusFilter,
                        productName
                )
        );
        GetBuyerOrderHistoryResult getBuyerOrderHistoryResult = GetBuyerOrderHistoryResult.from(
                getBuyerOrderResult.orders(),
                getBuyerOrderResult.orderedProducts()
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetMyOrdersResponse.from(getBuyerOrderHistoryResult),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "나의 주문 내역 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 나의 주문 내역 개수 조회
     *
     * @param principal 인증된 사용자 정보
     * @return 나의 주문 내역 개수 조회 응답 {@link GetOrderCountResponse}
     * @Author 성효빈
     * @Date 2026-01-19
     * @Description 나의 주문 내역 개수를 조회합니다.
     */
    @GetMapping("/me/count")
    @GetOrdersCountApiDocs
    public ResponseEntity<BaseResponse<GetOrderCountResponse>> getMyOrderCount(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @RequestParam(value = "period", required = false) OrderPeriod period,
            @RequestParam(value = "status", required = false) OrderStatusFilter statusFilter,
            @RequestParam(value = "productName", required = false) String productName
    ) {
        GetOrderCountResult getOrderCountResult = getOrderUseCase.getBuyerOrderCount(
                GetBuyerOrderHistoryQuery.of(
                        ElementExtractor.extractUserId(principal),
                        period,
                        statusFilter,
                        productName
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetOrderCountResponse.from(getOrderCountResult),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "나의 주문 내역 개수 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 주문 상태 변경
     *
     * @param request 주문 상태 변겅 요청
     * @Author 성효빈
     * @Date 2026-01-05
     * @Description 주문 상태를 변경합니다.
     */
    @PatchMapping("/{id}")
    @ChangeOrderStatusApiDocs
    public ResponseEntity<BaseResponse<Void>> changeOrderStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody ChangeOrderStatusRequest request
    ) {
        changeOrderStatusUseCase.changeOrderStatus(
                OrderRequestToCommandMapper.mapToCommand(id, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "주문 상태 변경 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 주문 상품의 리뷰 작성 여부 업데이트
     *
     * @param orderId       주문 ID
     * @param pricePolicyId 가격 정책 ID
     * @param isReviewed    리뷰 작성 여부
     * @Author 성효빈
     * @Date 2026-01-12
     * @Description 주문 상품의 리뷰 작성 여부를 업데이트합니다.
     */
    @PatchMapping("/{orderId}/order-products/{pricePolicyId}/review")
    @PreAuthorize(ADMIN_POINTCUT)
    @UpdateOrderProductReviewStatusApiDocs
    public ResponseEntity<BaseResponse<Void>> updateOrderProductReviewStatus(
            @PathVariable("orderId") Long orderId,
            @PathVariable("pricePolicyId") Long pricePolicyId,
            @RequestParam Boolean isReviewed
    ) {
        updateOrderProductUseCase.updateReviewStatus(
                UpdateOrderProductReviewStatusCommand.of(orderId, pricePolicyId, isReviewed)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "리뷰 작성 여부 업데이트 성공"
                ),
                HttpStatus.OK
        );
    }
}
