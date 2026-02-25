package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.GetFasstoSuppliersApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.RegisterFasstoSupplierApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.UpdateFasstoSupplierApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper.FasstoSupplierRequestToCommandMapper;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFasstoSupplierRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFasstoSupplierRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFasstoSuppliersResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFasstoSupplierResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.UpdateFasstoSupplierResponse;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoSuppliersResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoSupplierResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFasstoSupplierResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFasstoSuppliersUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFasstoSupplierUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFasstoSupplierUseCase;
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
public class FasstoSupplierController {
    private final RegisterFasstoSupplierUseCase registerFasstoSupplierUseCase;
    private final GetFasstoSuppliersUseCase getFasstoSuppliersUseCase;
    private final UpdateFasstoSupplierUseCase updateFasstoSupplierUseCase;

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
    @RegisterFasstoSupplierApiDocs
    public ResponseEntity<BaseResponse<RegisterFasstoSupplierResponse>> registerSupplier(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody RegisterFasstoSupplierRequest request
    ) {
        RegisterFasstoSupplierResult result = registerFasstoSupplierUseCase.registerSupplier(
                FasstoSupplierRequestToCommandMapper.mapToRegisterCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFasstoSupplierResponse.from(result),
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
    @GetFasstoSuppliersApiDocs
    public ResponseEntity<BaseResponse<GetFasstoSuppliersResponse>> getSuppliers(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken
    ) {
        GetFasstoSuppliersResult result = getFasstoSuppliersUseCase.getSuppliers(
                FasstoSupplierRequestToCommandMapper.mapToSuppliersCommand(customerCode, accessToken)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFasstoSuppliersResponse.from(result),
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
    @UpdateFasstoSupplierApiDocs
    public ResponseEntity<BaseResponse<UpdateFasstoSupplierResponse>> updateSupplier(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody UpdateFasstoSupplierRequest request
    ) {
        UpdateFasstoSupplierResult result = updateFasstoSupplierUseCase.updateSupplier(
                FasstoSupplierRequestToCommandMapper.mapToUpdateCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        UpdateFasstoSupplierResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 공급사 수정 성공"
                ),
                HttpStatus.OK
        );
    }
}
