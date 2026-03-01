package com.personal.marketnote.commerce.adapter.in.web.order.controller;

import com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs.GetAdminOrdersApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.order.mapper.AdminOrderRequestToCommandMapper;
import com.personal.marketnote.commerce.adapter.in.web.order.response.GetAdminOrdersResponse;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.port.in.result.order.GetAdminOrdersResult;
import com.personal.marketnote.commerce.port.in.usecase.order.GetAdminOrdersUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
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
}
