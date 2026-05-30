package com.personal.marketnote.commerce.adapter.in.web.order.controller;

import com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs.*;
import com.personal.marketnote.commerce.adapter.in.web.order.mapper.AdminOrderRequestToCommandMapper;
import com.personal.marketnote.commerce.adapter.in.web.order.mapper.OrderRequestToCommandMapper;
import com.personal.marketnote.commerce.adapter.in.web.order.request.CancelOrderRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.request.ChangeOrderStatusRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.request.RegisterOrderRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.request.RequestReturnRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.response.*;
import com.personal.marketnote.commerce.domain.order.OrderPeriod;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusFilter;
import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import com.personal.marketnote.commerce.port.in.command.order.ConfirmOrderCommand;
import com.personal.marketnote.commerce.port.in.command.order.GetBuyerOrderHistoryQuery;
import com.personal.marketnote.commerce.port.in.command.order.GetReturnRefundInfoCommand;
import com.personal.marketnote.commerce.port.in.command.order.UpdateOrderProductReviewStatusCommand;
import com.personal.marketnote.commerce.port.in.result.order.*;
import com.personal.marketnote.commerce.port.in.usecase.order.*;
import com.personal.marketnote.commerce.port.out.user.UpdateUserShippingAddressDeliveryRequestPort;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.common.utility.FormatValidator;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
@Tag(name = "주문 API", description = "주문 관련 API")
@RequiredArgsConstructor
@Validated
public class OrderController {
    private final RegisterOrderUseCase registerOrderUseCase;
    private final UpdateUserShippingAddressDeliveryRequestPort updateUserShippingAddressDeliveryRequestPort;
    private final GetOrderUseCase getOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final RequestReturnUseCase requestReturnUseCase;
    private final ConfirmOrderUseCase confirmOrderUseCase;
    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
    private final UpdateOrderProductUseCase updateOrderProductUseCase;
    private final GetAdminOrdersUseCase getAdminOrdersUseCase;
    private final GetOrderStatusHistoryUseCase getOrderStatusHistoryUseCase;
    private final GetReturnRefundInfoUseCase getReturnRefundInfoUseCase;

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
    @PostMapping("/api/v1/orders")
    @RegisterOrderApiDocs
    public ResponseEntity<BaseResponse<RegisterOrderResponse>> registerOrder(
            @Valid @RequestBody RegisterOrderRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long buyerId = ElementExtractor.extractUserId(principal);

        RegisterOrderResult result = registerOrderUseCase.registerOrder(
                OrderRequestToCommandMapper.mapToCommand(request, buyerId)
        );

        updateDeliveryRequestIfPresent(request, buyerId);

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

    private void updateDeliveryRequestIfPresent(RegisterOrderRequest request, Long buyerId) {
        if (FormatValidator.hasNoValue(request.getDeliveryRequestType())) {
            return;
        }

        updateUserShippingAddressDeliveryRequestPort.updateDeliveryRequest(
                request.getShippingAddressId(),
                buyerId,
                request.getDeliveryRequestType(),
                request.getDeliveryRequestMessage()
        );
    }

    /**
     * 주문 키 조회
     *
     * @param id        주문 ID
     * @param principal 인증된 사용자 정보
     * @return 주문 키 조회 응답 {@link GetOrderKeyResponse}
     * @Author 성효빈
     * @Date 2026-02-25
     * @Description 주문 ID로 주문 키를 조회합니다. 구매자 소유자 검증을 수행합니다.
     */
    @GetMapping("/api/v1/orders/{id}/order-key")
    @GetOrderKeyApiDocs
    public ResponseEntity<BaseResponse<GetOrderKeyResponse>> getOrderKey(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long buyerId = ElementExtractor.extractUserId(principal);
        GetOrderKeyResult getOrderKeyResult = getOrderUseCase.getOrderKey(id, buyerId);

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
     * @param id        주문 ID
     * @param principal 인증된 사용자 정보
     * @return 주문 정보 조회 응답 {@link GetOrderResponse}
     * @Author 성효빈
     * @Date 2026-01-05
     * @Description 주문 정보를 조회합니다. 구매자 소유자 검증을 수행합니다.
     */
    @GetMapping("/api/v1/orders/{id}")
    @GetOrderInfoApiDocs
    public ResponseEntity<BaseResponse<GetOrderResponse>> getOrder(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long buyerId = ElementExtractor.extractUserId(principal);
        GetOrderResult getOrderResult = getOrderUseCase.getOrderAndOrderProducts(id, buyerId);

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
    @GetMapping("/api/v1/orders/me")
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
    @GetMapping("/api/v1/orders/me/count")
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
     * 주문 취소 요청
     *
     * @param id        주문 ID
     * @param request   주문 취소 요청
     * @param principal 인증된 사용자 정보
     * @Author 성효빈
     * @Date 2026-04-05
     * @Description 구매자가 주문 취소를 요청합니다. 주문 상태를 CANCELLED로 변경합니다.
     */
    @PostMapping("/api/v1/orders/{id}/cancel")
    @CancelOrderApiDocs
    public ResponseEntity<BaseResponse<Void>> cancelOrder(
            @PathVariable("id") Long id,
            @Valid @RequestBody CancelOrderRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long buyerId = ElementExtractor.extractUserId(principal);

        cancelOrderUseCase.cancelOrder(
                OrderRequestToCommandMapper.mapToCancelCommand(id, request, buyerId)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "주문 취소 요청 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 반품 요청
     *
     * @param id        주문 ID
     * @param request   반품 요청
     * @param principal 인증된 사용자 정보
     * @Author 성효빈
     * @Date 2026-04-05
     * @Description 구매자가 반품을 요청합니다. 주문 상태를 RETURN_REQUESTED로 변경합니다.
     */
    @PostMapping("/api/v1/orders/{id}/return-request")
    @RequestReturnApiDocs
    public ResponseEntity<BaseResponse<Void>> requestReturn(
            @PathVariable("id") Long id,
            @Valid @RequestBody RequestReturnRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long buyerId = ElementExtractor.extractUserId(principal);

        requestReturnUseCase.requestReturn(
                OrderRequestToCommandMapper.mapToReturnRequestCommand(id, request, buyerId)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "반품 요청 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 반품 환불 예정 정보 조회
     *
     * @param id                    주문 ID
     * @param reasonCategory        반품 사유 카테고리
     * @param returnPricePolicyIds  반품 대상 가격 정책 ID 목록
     * @param principal             인증된 사용자 정보
     * @return 반품 환불 예정 정보 조회 응답 {@link GetReturnRefundInfoResponse}
     * @Author 성효빈
     * @Date 2026-04-09
     * @Description 반품 신청 전 환불 예정 정보를 조회합니다. 구매자 소유권 및 반품 가능 상태 검증을 수행합니다.
     */
    @GetMapping("/api/v1/orders/{id}/return-refund-info")
    @GetReturnRefundInfoApiDocs
    public ResponseEntity<BaseResponse<GetReturnRefundInfoResponse>> getReturnRefundInfo(
            @PathVariable("id") Long id,
            @RequestParam("reason-category") OrderStatusReasonCategory reasonCategory,
            @RequestParam(value = "return-price-policy-ids", required = false) List<Long> returnPricePolicyIds,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long buyerId = ElementExtractor.extractUserId(principal);

        GetReturnRefundInfoResult result = getReturnRefundInfoUseCase.getReturnRefundInfo(
                GetReturnRefundInfoCommand.builder()
                        .orderId(id)
                        .buyerId(buyerId)
                        .reasonCategory(reasonCategory)
                        .returnPricePolicyIds(returnPricePolicyIds)
                        .build()
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetReturnRefundInfoResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "반품 환불 예정 정보 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 구매 확정
     *
     * @param id        주문 ID
     * @param principal 인증된 사용자 정보
     * @Author 성효빈
     * @Date 2026-04-05
     * @Description 구매자가 구매 확정을 요청합니다. 주문 상태를 CONFIRMED로 변경합니다.
     */
    @PostMapping("/api/v1/orders/{id}/confirm")
    @ConfirmOrderApiDocs
    public ResponseEntity<BaseResponse<Void>> confirmOrder(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long buyerId = ElementExtractor.extractUserId(principal);

        confirmOrderUseCase.confirmOrder(
                ConfirmOrderCommand.builder()
                        .id(id)
                        .buyerId(buyerId)
                        .build()
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "구매 확정 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 주문 상태 변경
     *
     * @param id        주문 ID
     * @param request   주문 상태 변경 요청
     * @param principal 인증된 사용자 정보
     * @Author 성효빈
     * @Date 2026-01-05
     * @Description 주문 상태를 변경합니다. 구매자 역할일 경우 허용된 상태만 변경 가능합니다.
     */
    @PatchMapping("/api/v1/orders/{id}")
    @ChangeOrderStatusApiDocs
    public ResponseEntity<BaseResponse<Void>> changeOrderStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody ChangeOrderStatusRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long buyerId = ElementExtractor.extractUserId(principal);
        String role = ElementExtractor.extractRole(principal);

        changeOrderStatusUseCase.changeOrderStatus(
                OrderRequestToCommandMapper.mapToCommand(id, request, role, buyerId)
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
    @PatchMapping("/api/v1/orders/{orderId}/order-products/{pricePolicyId}/review")
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

    /**
     * 관리자 주문 내역 조회
     *
     * @param sellerId    판매자 ID (선택)
     * @param startDate   조회 시작 일시 (선택)
     * @param endDate     조회 종료 일시 (선택)
     * @param orderStatus 주문 상태 (선택)
     * @return 주문 내역 조회 응답 {@link GetAdminOrdersResponse}
     * @Author 성효빈
     * @Date 2026-03-02
     * @Description 관리자가 전체 주문 내역을 판매자별, 기간별, 상태별로 조회합니다.
     */
    @GetMapping("/api/v1/admin/orders")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetAdminOrdersApiDocs
    public ResponseEntity<BaseResponse<GetAdminOrdersResponse>> getAdminOrders(
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) OrderStatus orderStatus
    ) {
        GetAdminOrdersResult result = getAdminOrdersUseCase.getAdminOrders(
                AdminOrderRequestToCommandMapper.mapToQuery(sellerId, startDate, endDate, orderStatus)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetAdminOrdersResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "관리자 주문 내역 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 주문 상태 이력 조회
     *
     * @param id 주문 ID
     * @return 주문 상태 이력 조회 응답 {@link GetOrderStatusHistoryResponse}
     * @Author 성효빈
     * @Date 2026-03-02
     * @Description 관리자가 특정 주문의 상태 변경 이력을 조회합니다.
     */
    @GetMapping("/api/v1/admin/orders/{id}/status-history")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetOrderStatusHistoryApiDocs
    public ResponseEntity<BaseResponse<GetOrderStatusHistoryResponse>> getOrderStatusHistory(
            @PathVariable("id") Long id
    ) {
        GetOrderStatusHistoryResult result = getOrderStatusHistoryUseCase.getOrderStatusHistory(id);

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetOrderStatusHistoryResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "주문 상태 이력 조회 성공"
                ),
                HttpStatus.OK
        );
    }

}
