package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.GetFasstoShopsApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.RegisterFasstoShopApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.UpdateFasstoShopApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper.FasstoShopRequestToCommandMapper;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFasstoShopRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFasstoShopRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFasstoShopsResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFasstoShopResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.UpdateFasstoShopResponse;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoShopsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoShopResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFasstoShopResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFasstoShopsUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFasstoShopUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFasstoShopUseCase;
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
 * 파스토 출고처 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-25
 * @Description 파스토 출고처 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/vendors/fassto/shops")
@Tag(name = "파스토 출고처 API", description = "파스토 출고처 관련 API")
@RequiredArgsConstructor
public class FasstoShopController {
    private final RegisterFasstoShopUseCase registerFasstoShopUseCase;
    private final GetFasstoShopsUseCase getFasstoShopsUseCase;
    private final UpdateFasstoShopUseCase updateFasstoShopUseCase;

    /**
     * (관리자) 파스토 출고처 등록 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      출고처 등록 요청 정보
     * @Author 성효빈
     * @Date 2026-01-25
     * @Description 파스토 출고처 등록을 요청합니다.
     */
    @PostMapping("/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @RegisterFasstoShopApiDocs
    public ResponseEntity<BaseResponse<RegisterFasstoShopResponse>> registerShop(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody RegisterFasstoShopRequest request
    ) {
        RegisterFasstoShopResult result = registerFasstoShopUseCase.registerShop(
                FasstoShopRequestToCommandMapper.mapToRegisterCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFasstoShopResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 출고처 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * (관리자) 파스토 출고처 목록 조회
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @Author 성효빈
     * @Date 2026-01-25
     * @Description 파스토 출고처 목록을 조회합니다.
     */
    @GetMapping("/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFasstoShopsApiDocs
    public ResponseEntity<BaseResponse<GetFasstoShopsResponse>> getShops(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken
    ) {
        GetFasstoShopsResult result = getFasstoShopsUseCase.getShops(
                FasstoShopRequestToCommandMapper.mapToShopsCommand(customerCode, accessToken)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFasstoShopsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 출고처 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 출고처 수정 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      출고처 수정 요청 정보
     * @Author 성효빈
     * @Date 2026-01-25
     * @Description 파스토 출고처를 수정합니다.
     */
    @PutMapping("/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @UpdateFasstoShopApiDocs
    public ResponseEntity<BaseResponse<UpdateFasstoShopResponse>> updateShop(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody UpdateFasstoShopRequest request
    ) {
        UpdateFasstoShopResult result = updateFasstoShopUseCase.updateShop(
                FasstoShopRequestToCommandMapper.mapToUpdateCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        UpdateFasstoShopResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 출고처 수정 성공"
                ),
                HttpStatus.OK
        );
    }
}
