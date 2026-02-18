package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.*;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper.FasstoDeliveryRequestToCommandMapper;
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

@RestController
@RequestMapping("/api/v1/vendors/fassto/deliveries")
@Tag(name = "파스토 출고 API", description = "파스토 출고 관련 API")
@RequiredArgsConstructor
public class FasstoDeliveryController {
    private final RegisterFasstoDeliveryUseCase registerFasstoDeliveryUseCase;
    private final UpdateFasstoDeliveryUseCase updateFasstoDeliveryUseCase;
    private final RegisterFasstoDeliveryCarUseCase registerFasstoDeliveryCarUseCase;
    private final RegisterFasstoDeliveryIcsUseCase registerFasstoDeliveryIcsUseCase;
    private final UpdateFasstoDeliveryCarUseCase updateFasstoDeliveryCarUseCase;
    private final GetFasstoDeliveriesUseCase getFasstoDeliveriesUseCase;
    private final GetFasstoDeliveryStatusesUseCase getFasstoDeliveryStatusesUseCase;
    private final GetFasstoDeliveryDetailUseCase getFasstoDeliveryDetailUseCase;
    private final GetFasstoDeliveryOutOrdGoodsDetailUseCase getFasstoDeliveryOutOrdGoodsDetailUseCase;
    private final GetFasstoDeliveryOutOrdGoodsByOrdNoUseCase getFasstoDeliveryOutOrdGoodsByOrdNoUseCase;
    private final GetFasstoDeliveryGoodDetailUseCase getFasstoDeliveryGoodDetailUseCase;
    private final CancelFasstoDeliveryUseCase cancelFasstoDeliveryUseCase;

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
    @RegisterFasstoDeliveryApiDocs
    public ResponseEntity<BaseResponse<RegisterFasstoDeliveryResponse>> registerDelivery(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<RegisterFasstoDeliveryRequest> request
    ) {
        RegisterFasstoDeliveryResult result = registerFasstoDeliveryUseCase.registerDelivery(
                FasstoDeliveryRequestToCommandMapper.mapToRegisterCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFasstoDeliveryResponse.from(result),
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
    @UpdateFasstoDeliveryApiDocs
    public ResponseEntity<BaseResponse<RegisterFasstoDeliveryResponse>> updateDelivery(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<UpdateFasstoDeliveryRequest> request
    ) {
        RegisterFasstoDeliveryResult result = updateFasstoDeliveryUseCase.updateDelivery(
                FasstoDeliveryRequestToCommandMapper.mapToUpdateCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFasstoDeliveryResponse.from(result),
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
    @RegisterFasstoDeliveryCarApiDocs
    public ResponseEntity<BaseResponse<RegisterFasstoDeliveryResponse>> registerDeliveryCar(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<RegisterFasstoDeliveryCarRequest> request
    ) {
        RegisterFasstoDeliveryResult result = registerFasstoDeliveryCarUseCase.registerDeliveryCar(
                FasstoDeliveryRequestToCommandMapper.mapToRegisterCarCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFasstoDeliveryResponse.from(result),
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
    @RegisterFasstoDeliveryIcsApiDocs
    public ResponseEntity<BaseResponse<RegisterFasstoDeliveryResponse>> registerDeliveryIcs(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<RegisterFasstoDeliveryIcsRequest> request
    ) {
        RegisterFasstoDeliveryResult result = registerFasstoDeliveryIcsUseCase.registerDeliveryIcs(
                FasstoDeliveryRequestToCommandMapper.mapToRegisterIcsCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFasstoDeliveryResponse.from(result),
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
    @UpdateFasstoDeliveryCarApiDocs
    public ResponseEntity<BaseResponse<RegisterFasstoDeliveryResponse>> updateDeliveryCar(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<UpdateFasstoDeliveryCarRequest> request
    ) {
        RegisterFasstoDeliveryResult result = updateFasstoDeliveryCarUseCase.updateDeliveryCar(
                FasstoDeliveryRequestToCommandMapper.mapToUpdateCarCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFasstoDeliveryResponse.from(result),
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
    @CancelFasstoDeliveryApiDocs
    public ResponseEntity<BaseResponse<CancelFasstoDeliveryResponse>> cancelDelivery(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<CancelFasstoDeliveryRequest> request
    ) {
        CancelFasstoDeliveryResult result = cancelFasstoDeliveryUseCase.cancelDelivery(
                FasstoDeliveryRequestToCommandMapper.mapToCancelCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        CancelFasstoDeliveryResponse.from(result),
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
    @GetFasstoDeliveriesApiDocs
    public ResponseEntity<BaseResponse<GetFasstoDeliveriesResponse>> getDeliveries(
            @PathVariable String customerCode,
            @PathVariable String startDate,
            @PathVariable String endDate,
            @PathVariable String status,
            @PathVariable String outDiv,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam(required = false) String ordNo
    ) {
        GetFasstoDeliveriesResult result = getFasstoDeliveriesUseCase.getDeliveries(
                FasstoDeliveryRequestToCommandMapper.mapToDeliveriesCommand(
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
                        GetFasstoDeliveriesResponse.from(result),
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
    @GetFasstoDeliveryStatusesApiDocs
    public ResponseEntity<BaseResponse<GetFasstoDeliveryStatusesResponse>> getDeliveryStatuses(
            @PathVariable String customerCode,
            @PathVariable String startDate,
            @PathVariable String endDate,
            @PathVariable String outDiv,
            @RequestHeader("accessToken") String accessToken
    ) {
        GetFasstoDeliveryStatusesResult result = getFasstoDeliveryStatusesUseCase.getDeliveryStatuses(
                FasstoDeliveryRequestToCommandMapper.mapToDeliveryStatusesCommand(
                        customerCode,
                        accessToken,
                        startDate,
                        endDate,
                        outDiv
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFasstoDeliveryStatusesResponse.from(result),
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
    @GetFasstoDeliveryDetailApiDocs
    public ResponseEntity<BaseResponse<GetFasstoDeliveryDetailResponse>> getDeliveryDetail(
            @PathVariable String customerCode,
            @PathVariable String slipNo,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam(required = false) String ordNo
    ) {
        GetFasstoDeliveryDetailResult result = getFasstoDeliveryDetailUseCase.getDeliveryDetail(
                FasstoDeliveryRequestToCommandMapper.mapToDeliveryDetailCommand(customerCode, accessToken, slipNo, ordNo)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFasstoDeliveryDetailResponse.from(result),
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
    @GetFasstoDeliveryOutOrdGoodsDetailApiDocs
    public ResponseEntity<BaseResponse<GetFasstoDeliveryOutOrdGoodsDetailResponse>> getOutOrdGoodsDetail(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam("outOrdSlipNo") String outOrdSlipNo
    ) {
        GetFasstoDeliveryOutOrdGoodsDetailResult result = getFasstoDeliveryOutOrdGoodsDetailUseCase.getOutOrdGoodsDetail(
                FasstoDeliveryRequestToCommandMapper.mapToOutOrdGoodsDetailCommand(
                        customerCode,
                        accessToken,
                        outOrdSlipNo
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFasstoDeliveryOutOrdGoodsDetailResponse.from(result),
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
    @GetFasstoDeliveryOutOrdGoodsByOrdNoApiDocs
    public ResponseEntity<BaseResponse<GetFasstoDeliveryOutOrdGoodsByOrdNoResponse>> getOutOrdGoodsByOrdNo(
            @PathVariable String customerCode,
            @PathVariable String startDate,
            @PathVariable String endDate,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam(required = false) String ordNo
    ) {
        GetFasstoDeliveryOutOrdGoodsByOrdNoResult result = getFasstoDeliveryOutOrdGoodsByOrdNoUseCase.getOutOrdGoodsByOrdNo(
                FasstoDeliveryRequestToCommandMapper.mapToOutOrdGoodsByOrdNoCommand(
                        customerCode,
                        accessToken,
                        startDate,
                        endDate,
                        ordNo
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFasstoDeliveryOutOrdGoodsByOrdNoResponse.from(result),
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
    @GetFasstoDeliveryGoodDetailApiDocs
    public ResponseEntity<BaseResponse<GetFasstoDeliveryGoodDetailResponse>> getDeliveryGoodDetail(
            @PathVariable String customerCode,
            @PathVariable String startDate,
            @PathVariable String endDate,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam(required = false) String ordNo
    ) {
        GetFasstoDeliveryGoodDetailResult result = getFasstoDeliveryGoodDetailUseCase.getDeliveryGoodDetail(
                FasstoDeliveryRequestToCommandMapper.mapToDeliveryGoodDetailCommand(
                        customerCode,
                        accessToken,
                        startDate,
                        endDate,
                        ordNo
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFasstoDeliveryGoodDetailResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 출고 상품 상세 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
