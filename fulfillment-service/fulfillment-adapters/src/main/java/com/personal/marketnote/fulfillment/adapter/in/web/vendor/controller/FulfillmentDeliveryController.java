package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.*;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper.FulfillmentDeliveryRequestToCommandMapper;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.*;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.*;
import com.personal.marketnote.fulfillment.port.in.result.vendor.*;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 파스토 출고 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-02-11
 * @Description 파스토 출고 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/vendors/fassto/deliveries")
@Tag(name = "파스토 출고 API", description = "파스토 출고 관련 API")
@RequiredArgsConstructor
public class FulfillmentDeliveryController {
    private final RegisterFulfillmentDeliveryUseCase registerFulfillmentDeliveryUseCase;
    private final UpdateFulfillmentDeliveryUseCase updateFulfillmentDeliveryUseCase;
    private final RegisterFulfillmentDeliveryCarUseCase registerFulfillmentDeliveryCarUseCase;
    private final RegisterFulfillmentDeliveryIcsUseCase registerFulfillmentDeliveryIcsUseCase;
    private final UpdateFulfillmentDeliveryCarUseCase updateFulfillmentDeliveryCarUseCase;
    private final GetFulfillmentDeliveriesUseCase getFulfillmentDeliveriesUseCase;
    private final GetFulfillmentDeliveryStatusesUseCase getFulfillmentDeliveryStatusesUseCase;
    private final GetFulfillmentDeliveryDetailUseCase getFulfillmentDeliveryDetailUseCase;
    private final GetFulfillmentDeliveryOutOrdGoodsDetailUseCase getFulfillmentDeliveryOutOrdGoodsDetailUseCase;
    private final GetFulfillmentDeliveryOutOrdGoodsByOrdNoUseCase getFulfillmentDeliveryOutOrdGoodsByOrdNoUseCase;
    private final GetFulfillmentDeliveryGoodDetailUseCase getFulfillmentDeliveryGoodDetailUseCase;
    private final CancelFulfillmentDeliveryUseCase cancelFulfillmentDeliveryUseCase;
    private final CompleteFulfillmentDeliveryIcsUseCase completeFulfillmentDeliveryIcsUseCase;

    /**
     * (관리자) 파스토 출고 등록 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      출고 등록 요청 정보
     * @Author 성효빈
     * @Date 2026-02-11
     * @Description 파스토 출고 등록(택배)을 요청합니다.
     */
    @PostMapping("/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @RegisterFulfillmentDeliveryApiDocs
    public ResponseEntity<BaseResponse<RegisterFulfillmentDeliveryResponse>> registerDelivery(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<RegisterFulfillmentDeliveryRequest> request
    ) {
        RegisterFulfillmentDeliveryResult result = registerFulfillmentDeliveryUseCase.registerDelivery(
                FulfillmentDeliveryRequestToCommandMapper.mapToRegisterCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFulfillmentDeliveryResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 출고 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * (관리자) 파스토 출고 수정(택배) 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      출고 수정(택배) 요청 정보
     * @Author 성효빈
     * @Date 2026-02-17
     * @Description 파스토 출고 수정(택배)을 요청합니다.
     */
    @PatchMapping("/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @UpdateFulfillmentDeliveryApiDocs
    public ResponseEntity<BaseResponse<RegisterFulfillmentDeliveryResponse>> updateDelivery(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<UpdateFulfillmentDeliveryRequest> request
    ) {
        RegisterFulfillmentDeliveryResult result = updateFulfillmentDeliveryUseCase.updateDelivery(
                FulfillmentDeliveryRequestToCommandMapper.mapToUpdateCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFulfillmentDeliveryResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 출고 수정(택배) 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 출고 등록(차량) 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      출고 등록(차량) 요청 정보
     * @Author 성효빈
     * @Date 2026-02-17
     * @Description 파스토 출고 등록(차량)을 요청합니다.
     */
    @PostMapping("/car/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @RegisterFulfillmentDeliveryCarApiDocs
    public ResponseEntity<BaseResponse<RegisterFulfillmentDeliveryResponse>> registerDeliveryCar(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<RegisterFulfillmentDeliveryCarRequest> request
    ) {
        RegisterFulfillmentDeliveryResult result = registerFulfillmentDeliveryCarUseCase.registerDeliveryCar(
                FulfillmentDeliveryRequestToCommandMapper.mapToRegisterCarCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFulfillmentDeliveryResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 출고 등록(차량) 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * (관리자) 파스토 출고 등록(해외) 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      출고 등록(해외) 요청 정보
     * @Author 성효빈
     * @Date 2026-02-18
     * @Description 파스토 출고 등록(해외)을 요청합니다.
     */
    @PostMapping("/ics/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @RegisterFulfillmentDeliveryIcsApiDocs
    public ResponseEntity<BaseResponse<RegisterFulfillmentDeliveryResponse>> registerDeliveryIcs(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<RegisterFulfillmentDeliveryIcsRequest> request
    ) {
        RegisterFulfillmentDeliveryResult result = registerFulfillmentDeliveryIcsUseCase.registerDeliveryIcs(
                FulfillmentDeliveryRequestToCommandMapper.mapToRegisterIcsCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFulfillmentDeliveryResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 출고 등록(해외) 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * (관리자) 파스토 출고 수정(차량) 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      출고 수정(차량) 요청 정보
     * @Author 성효빈
     * @Date 2026-02-17
     * @Description 파스토 출고 수정(차량)을 요청합니다.
     */
    @PatchMapping("/car/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @UpdateFulfillmentDeliveryCarApiDocs
    public ResponseEntity<BaseResponse<RegisterFulfillmentDeliveryResponse>> updateDeliveryCar(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<UpdateFulfillmentDeliveryCarRequest> request
    ) {
        RegisterFulfillmentDeliveryResult result = updateFulfillmentDeliveryCarUseCase.updateDeliveryCar(
                FulfillmentDeliveryRequestToCommandMapper.mapToUpdateCarCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFulfillmentDeliveryResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 출고 수정(차량) 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 출고 취소 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      출고 취소 요청 정보
     * @Author 성효빈
     * @Date 2026-02-12
     * @Description 파스토 출고 요청을 취소합니다.
     */
    @PatchMapping("/cancel/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @CancelFulfillmentDeliveryApiDocs
    public ResponseEntity<BaseResponse<CancelFulfillmentDeliveryResponse>> cancelDelivery(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<CancelFulfillmentDeliveryRequest> request
    ) {
        CancelFulfillmentDeliveryResult result = cancelFulfillmentDeliveryUseCase.cancelDelivery(
                FulfillmentDeliveryRequestToCommandMapper.mapToCancelCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        CancelFulfillmentDeliveryResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 출고 취소 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 출고 목록 조회
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param startDate    조회 시작일(YYYY-MM-DD)
     * @param endDate      조회 종료일(YYYY-MM-DD)
     * @param status       작업상태 코드(ALL:전체, ORDER:출고요청, WORKING:출고작업중, DONE:출고완료, PARTDONE:부분출고,
     *                     CANCEL:출고요청취소, SHORTAGE:재고부족결품)
     * @param outDiv       출고 구분(1:택배, 2:차량배송)
     * @param ordNo        고객사 주문번호
     * @Author 성효빈
     * @Date 2026-02-11
     * @Description 파스토 출고 목록을 조회합니다.
     */
    @GetMapping("/{customerCode}/{startDate}/{endDate}/{status}/{outDiv}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFulfillmentDeliveriesApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentDeliveriesResponse>> getDeliveries(
            @PathVariable String customerCode,
            @PathVariable String startDate,
            @PathVariable String endDate,
            @PathVariable String status,
            @PathVariable String outDiv,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam(required = false) String ordNo
    ) {
        GetFulfillmentDeliveriesResult result = getFulfillmentDeliveriesUseCase.getDeliveries(
                FulfillmentDeliveryRequestToCommandMapper.mapToDeliveriesCommand(
                        customerCode,
                        accessToken,
                        startDate,
                        endDate,
                        status,
                        outDiv,
                        ordNo
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentDeliveriesResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 출고 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 출고 배송 조회
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param startDate    검색 시작일(YYYY-MM-DD)
     * @param endDate      검색 종료일(YYYY-MM-DD)
     * @param outDiv       출고 구분(ALL:전체, 1:택배, 2:차량배송, COUPANG:쿠팡쉽먼트, ONE_DAY:원데이배송)
     * @Author 성효빈
     * @Date 2026-02-13
     * @Description 파스토 출고 배송 상태를 조회합니다.
     */
    @GetMapping("/parcel/{customerCode}/{startDate}/{endDate}/{outDiv}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFulfillmentDeliveryStatusesApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentDeliveryStatusesResponse>> getDeliveryStatuses(
            @PathVariable String customerCode,
            @PathVariable String startDate,
            @PathVariable String endDate,
            @PathVariable String outDiv,
            @RequestHeader("accessToken") String accessToken
    ) {
        GetFulfillmentDeliveryStatusesResult result = getFulfillmentDeliveryStatusesUseCase.getDeliveryStatuses(
                FulfillmentDeliveryRequestToCommandMapper.mapToDeliveryStatusesCommand(
                        customerCode,
                        accessToken,
                        startDate,
                        endDate,
                        outDiv
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentDeliveryStatusesResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 출고 배송 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 출고 상세 조회
     *
     * @param customerCode 파스토 고객사 코드
     * @param slipNo       파스토 출고요청번호
     * @param accessToken  파스토 액세스 토큰
     * @param ordNo        주문번호
     * @Author 성효빈
     * @Date 2026-02-12
     * @Description 파스토 출고 상세 정보를 조회합니다.
     */
    @GetMapping("/detail/{customerCode}/{slipNo}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFulfillmentDeliveryDetailApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentDeliveryDetailResponse>> getDeliveryDetail(
            @PathVariable String customerCode,
            @PathVariable String slipNo,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam(required = false) String ordNo
    ) {
        GetFulfillmentDeliveryDetailResult result = getFulfillmentDeliveryDetailUseCase.getDeliveryDetail(
                FulfillmentDeliveryRequestToCommandMapper.mapToDeliveryDetailCommand(customerCode, accessToken, slipNo, ordNo)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentDeliveryDetailResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 출고 상세 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 출고중 상품 송장별 조회
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param outOrdSlipNo 파스토 출고요청번호
     * @Author 성효빈
     * @Date 2026-02-13
     * @Description 파스토 출고중 상품의 송장별 상품 정보를 조회합니다.
     */
    @GetMapping("/out-ord/goods-detail/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFulfillmentDeliveryOutOrdGoodsDetailApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentDeliveryOutOrdGoodsDetailResponse>> getOutOrdGoodsDetail(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam("outOrdSlipNo") String outOrdSlipNo
    ) {
        GetFulfillmentDeliveryOutOrdGoodsDetailResult result = getFulfillmentDeliveryOutOrdGoodsDetailUseCase.getOutOrdGoodsDetail(
                FulfillmentDeliveryRequestToCommandMapper.mapToOutOrdGoodsDetailCommand(
                        customerCode,
                        accessToken,
                        outOrdSlipNo
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentDeliveryOutOrdGoodsDetailResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 출고중 상품 송장별 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 주문번호 기반 출고중 상품 조회
     *
     * @param customerCode 파스토 고객사 코드
     * @param startDate    조회 시작일(YYYY-MM-DD)
     * @param endDate      조회 종료일(YYYY-MM-DD)
     * @param accessToken  파스토 액세스 토큰
     * @param ordNo        고객사 주문번호
     * @Author 성효빈
     * @Date 2026-02-17
     * @Description 파스토 주문번호 기준 출고중 상품 정보를 조회합니다.
     */
    @GetMapping("/out-ord/goods-ord-no/{customerCode}/{startDate}/{endDate}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFulfillmentDeliveryOutOrdGoodsByOrdNoApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentDeliveryOutOrdGoodsByOrdNoResponse>> getOutOrdGoodsByOrdNo(
            @PathVariable String customerCode,
            @PathVariable String startDate,
            @PathVariable String endDate,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam(required = false) String ordNo
    ) {
        GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult result = getFulfillmentDeliveryOutOrdGoodsByOrdNoUseCase.getOutOrdGoodsByOrdNo(
                FulfillmentDeliveryRequestToCommandMapper.mapToOutOrdGoodsByOrdNoCommand(
                        customerCode,
                        accessToken,
                        startDate,
                        endDate,
                        ordNo
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentDeliveryOutOrdGoodsByOrdNoResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 주문번호 기반 출고중 상품 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 출고 상품 상세 목록 조회
     *
     * @param customerCode 파스토 고객사 코드
     * @param startDate    조회 시작일(YYYY-MM-DD)
     * @param endDate      조회 종료일(YYYY-MM-DD)
     * @param accessToken  파스토 액세스 토큰
     * @param ordNo        고객사 주문번호
     * @Author 성효빈
     * @Date 2026-02-18
     * @Description 파스토 출고 상품의 상세 정보(금액, 배송유형 등)를 조회합니다.
     */
    @GetMapping("/good-detail/{customerCode}/{startDate}/{endDate}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFulfillmentDeliveryGoodDetailApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentDeliveryGoodDetailResponse>> getDeliveryGoodDetail(
            @PathVariable String customerCode,
            @PathVariable String startDate,
            @PathVariable String endDate,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam(required = false) String ordNo
    ) {
        GetFulfillmentDeliveryGoodDetailResult result = getFulfillmentDeliveryGoodDetailUseCase.getDeliveryGoodDetail(
                FulfillmentDeliveryRequestToCommandMapper.mapToDeliveryGoodDetailCommand(
                        customerCode,
                        accessToken,
                        startDate,
                        endDate,
                        ordNo
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentDeliveryGoodDetailResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 출고 상품 상세 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 배송완료 처리(해외)
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      배송완료 처리 요청 정보
     * @Author 성효빈
     * @Date 2026-02-18
     * @Description 파스토 해외(ICS) 배송완료 처리를 요청합니다.
     */
    @PatchMapping("/ics/completed/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @CompleteFulfillmentDeliveryIcsApiDocs
    public ResponseEntity<BaseResponse<CompleteFulfillmentDeliveryIcsResponse>> completeDeliveryIcs(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody CompleteFulfillmentDeliveryIcsRequest request
    ) {
        CompleteFulfillmentDeliveryIcsResult result = completeFulfillmentDeliveryIcsUseCase.completeDeliveryIcs(
                FulfillmentDeliveryRequestToCommandMapper.mapToIcsCompletionCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        CompleteFulfillmentDeliveryIcsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 배송완료 처리(해외) 성공"
                ),
                HttpStatus.OK
        );
    }
}
