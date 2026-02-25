package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.GetFasstoReturnGodDetailApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.RegisterFasstoDirectReturnDeliveryApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.RegisterFasstoReturnDeliveryApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper.FasstoDirectReturnDeliveryRequestToCommandMapper;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper.FasstoReturnDeliveryRequestToCommandMapper;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFasstoDirectReturnDeliveryRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFasstoReturnDeliveryRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFasstoReturnGodDetailResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFasstoDirectReturnDeliveryResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFasstoReturnDeliveryResponse;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoReturnGodDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFasstoReturnGodDetailUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFasstoDirectReturnDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFasstoReturnDeliveryUseCase;
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
public class FasstoReturnDeliveryController {
    private final RegisterFasstoReturnDeliveryUseCase registerFasstoReturnDeliveryUseCase;
    private final RegisterFasstoDirectReturnDeliveryUseCase registerFasstoDirectReturnDeliveryUseCase;
    private final GetFasstoReturnGodDetailUseCase getFasstoReturnGodDetailUseCase;

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
    @RegisterFasstoReturnDeliveryApiDocs
    public ResponseEntity<BaseResponse<RegisterFasstoReturnDeliveryResponse>> registerReturnDelivery(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<RegisterFasstoReturnDeliveryRequest> request
    ) {
        RegisterFasstoDeliveryResult result = registerFasstoReturnDeliveryUseCase.registerReturnDelivery(
                FasstoReturnDeliveryRequestToCommandMapper.mapToRegisterCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFasstoReturnDeliveryResponse.from(result),
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
    @RegisterFasstoDirectReturnDeliveryApiDocs
    public ResponseEntity<BaseResponse<RegisterFasstoDirectReturnDeliveryResponse>> registerDirectReturnDelivery(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<RegisterFasstoDirectReturnDeliveryRequest> request
    ) {
        RegisterFasstoDeliveryResult result = registerFasstoDirectReturnDeliveryUseCase.registerDirectReturnDelivery(
                FasstoDirectReturnDeliveryRequestToCommandMapper.mapToRegisterCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFasstoDirectReturnDeliveryResponse.from(result),
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
    @GetFasstoReturnGodDetailApiDocs
    public ResponseEntity<BaseResponse<GetFasstoReturnGodDetailResponse>> getReturnGodDetail(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam(required = false) String strDt,
            @RequestParam(required = false) String endDt,
            @RequestParam(required = false) String rtnSlipNoList,
            @RequestParam(required = false) String whCd
    ) {
        GetFasstoReturnGodDetailResult result = getFasstoReturnGodDetailUseCase.getReturnGodDetail(
                FasstoReturnDeliveryRequestToCommandMapper.mapToReturnGodDetailCommand(
                        customerCode, accessToken, strDt, endDt, rtnSlipNoList, whCd
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFasstoReturnGodDetailResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 반품 완료 상품 상세 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
