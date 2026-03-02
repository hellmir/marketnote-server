package com.personal.marketnote.commerce.adapter.in.web.order.controller;

import com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs.GetAdminOrdersApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs.GetOrderStatusHistoryApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs.RegisterTrackingInfoApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.order.mapper.AdminOrderRequestToCommandMapper;
import com.personal.marketnote.commerce.adapter.in.web.order.mapper.OrderRequestToCommandMapper;
import com.personal.marketnote.commerce.adapter.in.web.order.request.RegisterTrackingInfoRequest;
import com.personal.marketnote.commerce.adapter.in.web.order.response.GetAdminOrdersResponse;
import com.personal.marketnote.commerce.adapter.in.web.order.response.GetOrderStatusHistoryResponse;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.port.in.result.order.GetAdminOrdersResult;
import com.personal.marketnote.commerce.port.in.result.order.GetOrderStatusHistoryResult;
import com.personal.marketnote.commerce.port.in.usecase.order.GetAdminOrdersUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderStatusHistoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.RegisterTrackingInfoUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_OR_SELLER_PRINCIPAL_POINTCUT;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 관리자 주문 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-03-02
 * @Description 관리자 주문 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
@Tag(name = "(관리자) 주문 API", description = "관리자 주문 관련 API")
@RequiredArgsConstructor
@Validated
public class AdminOrderController {
    private final GetAdminOrdersUseCase getAdminOrdersUseCase;
    private final GetOrderStatusHistoryUseCase getOrderStatusHistoryUseCase;
    private final RegisterTrackingInfoUseCase registerTrackingInfoUseCase;

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
    @GetMapping
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
    @GetMapping("/{id}/status-history")
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

    /**
     * 송장 정보 등록/수정
     *
     * @param id      주문 ID
     * @param request 송장 정보 등록 요청 {@link RegisterTrackingInfoRequest}
     * @return 송장 정보 등록 성공 응답
     * @Author 성효빈
     * @Date 2026-03-02
     * @Description 주문의 송장 정보(택배사, 송장번호)를 등록/수정합니다.
     */
    @PutMapping("/{id}/tracking")
    @PreAuthorize(ADMIN_OR_SELLER_PRINCIPAL_POINTCUT)
    @RegisterTrackingInfoApiDocs
    public ResponseEntity<BaseResponse<Void>> registerTrackingInfo(
            @PathVariable("id") Long id,
            @Valid @RequestBody RegisterTrackingInfoRequest request
    ) {
        registerTrackingInfoUseCase.registerTrackingInfo(
                OrderRequestToCommandMapper.mapToTrackingInfoCommand(id, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "송장 정보 등록 성공"
                ),
                HttpStatus.OK
        );
    }
}
