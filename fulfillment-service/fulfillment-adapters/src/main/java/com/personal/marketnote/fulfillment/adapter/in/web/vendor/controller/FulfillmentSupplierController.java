package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.GetFulfillmentSuppliersApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.RegisterFulfillmentSupplierApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.UpdateFulfillmentSupplierApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper.FulfillmentSupplierRequestToCommandMapper;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentSupplierRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFulfillmentSupplierRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentSuppliersResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFulfillmentSupplierResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.UpdateFulfillmentSupplierResponse;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSuppliersResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentSupplierResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentSupplierResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentSuppliersUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentSupplierUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentSupplierUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 파스토 공급사 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-26
 * @Description 파스토 공급사 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/vendors/fassto/suppliers")
@Tag(name = "파스토 공급사 API", description = "파스토 공급사 관련 API")
@RequiredArgsConstructor
public class FulfillmentSupplierController {
    private final RegisterFulfillmentSupplierUseCase registerFulfillmentSupplierUseCase;
    private final GetFulfillmentSuppliersUseCase getFulfillmentSuppliersUseCase;
    private final UpdateFulfillmentSupplierUseCase updateFulfillmentSupplierUseCase;

    /**
     * (관리자) 파스토 공급사 등록 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      공급사 등록 요청 정보
     * @Author 성효빈
     * @Date 2026-01-26
     * @Description 파스토 공급사 등록을 요청합니다.
     */
    @PostMapping("/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @RegisterFulfillmentSupplierApiDocs
    public ResponseEntity<BaseResponse<RegisterFulfillmentSupplierResponse>> registerSupplier(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody RegisterFulfillmentSupplierRequest request
    ) {
        RegisterFulfillmentSupplierResult result = registerFulfillmentSupplierUseCase.registerSupplier(
                FulfillmentSupplierRequestToCommandMapper.mapToRegisterCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFulfillmentSupplierResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 공급사 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * (관리자) 파스토 공급사 목록 조회
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @Author 성효빈
     * @Date 2026-01-26
     * @Description 파스토 공급사 목록을 조회합니다.
     */
    @GetMapping("/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFulfillmentSuppliersApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentSuppliersResponse>> getSuppliers(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken
    ) {
        GetFulfillmentSuppliersResult result = getFulfillmentSuppliersUseCase.getSuppliers(
                FulfillmentSupplierRequestToCommandMapper.mapToSuppliersCommand(customerCode, accessToken)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentSuppliersResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 공급사 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 공급사 수정 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      공급사 수정 요청 정보
     * @Author 성효빈
     * @Date 2026-01-28
     * @Description 파스토 공급사 정보를 수정합니다.
     */
    @PutMapping("/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @UpdateFulfillmentSupplierApiDocs
    public ResponseEntity<BaseResponse<UpdateFulfillmentSupplierResponse>> updateSupplier(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody UpdateFulfillmentSupplierRequest request
    ) {
        UpdateFulfillmentSupplierResult result = updateFulfillmentSupplierUseCase.updateSupplier(
                FulfillmentSupplierRequestToCommandMapper.mapToUpdateCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        UpdateFulfillmentSupplierResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 공급사 수정 성공"
                ),
                HttpStatus.OK
        );
    }
}
