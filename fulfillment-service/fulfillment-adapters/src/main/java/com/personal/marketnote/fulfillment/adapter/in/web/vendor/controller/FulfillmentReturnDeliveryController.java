package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.GetFulfillmentReturnGodDetailApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.RegisterFulfillmentDirectReturnDeliveryApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.RegisterFulfillmentReturnDeliveryApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper.FulfillmentDirectReturnDeliveryRequestToCommandMapper;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper.FulfillmentReturnDeliveryRequestToCommandMapper;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentDirectReturnDeliveryRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentReturnDeliveryRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentReturnGodDetailResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFulfillmentDirectReturnDeliveryResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFulfillmentReturnDeliveryResponse;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentReturnGodDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentReturnGodDetailUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentDirectReturnDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentReturnDeliveryUseCase;
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
 * 파스토 반품 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-02-20
 * @Description 파스토 반품 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/vendors/fassto/return-deliveries")
@Tag(name = "파스토 반품 API", description = "파스토 반품 관련 API")
@RequiredArgsConstructor
public class FulfillmentReturnDeliveryController {
    private final RegisterFulfillmentReturnDeliveryUseCase registerFulfillmentReturnDeliveryUseCase;
    private final RegisterFulfillmentDirectReturnDeliveryUseCase registerFulfillmentDirectReturnDeliveryUseCase;
    private final GetFulfillmentReturnGodDetailUseCase getFulfillmentReturnGodDetailUseCase;

    /**
     * (관리자) 파스토 반품 예약 등록 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      반품 예약 등록 요청 정보
     * @Author 성효빈
     * @Date 2026-02-20
     * @Description 파스토 반품 예약 등록(택배반품등록/예약진행)을 요청합니다.
     */
    @PostMapping("/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @RegisterFulfillmentReturnDeliveryApiDocs
    public ResponseEntity<BaseResponse<RegisterFulfillmentReturnDeliveryResponse>> registerReturnDelivery(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<RegisterFulfillmentReturnDeliveryRequest> request
    ) {
        RegisterFulfillmentDeliveryResult result = registerFulfillmentReturnDeliveryUseCase.registerReturnDelivery(
                FulfillmentReturnDeliveryRequestToCommandMapper.mapToRegisterCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFulfillmentReturnDeliveryResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 반품 예약 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * (관리자) 파스토 반품 택배사 미지정 등록 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      반품 택배사 미지정 등록 요청 정보
     * @Author 성효빈
     * @Date 2026-02-20
     * @Description 파스토 반품 택배사 미지정 등록(택배사 예약 없거나 다른 택배사 사용 시)을 요청합니다.
     */
    @PostMapping("/direct/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @RegisterFulfillmentDirectReturnDeliveryApiDocs
    public ResponseEntity<BaseResponse<RegisterFulfillmentDirectReturnDeliveryResponse>> registerDirectReturnDelivery(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<RegisterFulfillmentDirectReturnDeliveryRequest> request
    ) {
        RegisterFulfillmentDeliveryResult result = registerFulfillmentDirectReturnDeliveryUseCase.registerDirectReturnDelivery(
                FulfillmentDirectReturnDeliveryRequestToCommandMapper.mapToRegisterCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFulfillmentDirectReturnDeliveryResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 반품 택배사 미지정 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * (관리자) 파스토 반품 완료 상품 상세 목록 조회
     *
     * @param customerCode  파스토 고객사 코드
     * @param accessToken   파스토 액세스 토큰
     * @param strDt         반품예정일 검색 시작일(YYYY-MM-DD), rtnSlipNoList가 없으면 필수
     * @param endDt         반품예정일 검색 종료일(YYYY-MM-DD), rtnSlipNoList가 없으면 필수
     * @param rtnSlipNoList 반품요청번호(목록), 여러 개일 경우 ','로 연결, strDt/endDt가 없으면 필수
     * @param whCd          창고 코드
     * @Author 성효빈
     * @Date 2026-02-20
     * @Description 파스토 반품이 완료된 상품의 상세 목록을 조회합니다.
     */
    @GetMapping("/god-detail/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFulfillmentReturnGodDetailApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentReturnGodDetailResponse>> getReturnGodDetail(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam(required = false) String strDt,
            @RequestParam(required = false) String endDt,
            @RequestParam(required = false) String rtnSlipNoList,
            @RequestParam(required = false) String whCd
    ) {
        GetFulfillmentReturnGodDetailResult result = getFulfillmentReturnGodDetailUseCase.getReturnGodDetail(
                FulfillmentReturnDeliveryRequestToCommandMapper.mapToReturnGodDetailCommand(
                        customerCode, accessToken, strDt, endDt, rtnSlipNoList, whCd
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentReturnGodDetailResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 반품 완료 상품 상세 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
