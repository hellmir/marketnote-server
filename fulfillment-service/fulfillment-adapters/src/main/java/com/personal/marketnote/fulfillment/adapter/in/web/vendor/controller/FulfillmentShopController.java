package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.GetFulfillmentShopsApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.RegisterFulfillmentShopApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.UpdateFulfillmentShopApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper.FulfillmentShopRequestToCommandMapper;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentShopRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFulfillmentShopRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentShopsResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFulfillmentShopResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.UpdateFulfillmentShopResponse;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentShopsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentShopResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentShopResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentShopsUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentShopUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentShopUseCase;
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
public class FulfillmentShopController {
    private final RegisterFulfillmentShopUseCase registerFulfillmentShopUseCase;
    private final GetFulfillmentShopsUseCase getFulfillmentShopsUseCase;
    private final UpdateFulfillmentShopUseCase updateFulfillmentShopUseCase;

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
    @RegisterFulfillmentShopApiDocs
    public ResponseEntity<BaseResponse<RegisterFulfillmentShopResponse>> registerShop(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody RegisterFulfillmentShopRequest request
    ) {
        RegisterFulfillmentShopResult result = registerFulfillmentShopUseCase.registerShop(
                FulfillmentShopRequestToCommandMapper.mapToRegisterCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFulfillmentShopResponse.from(result),
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
    @GetFulfillmentShopsApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentShopsResponse>> getShops(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken
    ) {
        GetFulfillmentShopsResult result = getFulfillmentShopsUseCase.getShops(
                FulfillmentShopRequestToCommandMapper.mapToShopsCommand(customerCode, accessToken)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentShopsResponse.from(result),
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
    @UpdateFulfillmentShopApiDocs
    public ResponseEntity<BaseResponse<UpdateFulfillmentShopResponse>> updateShop(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody UpdateFulfillmentShopRequest request
    ) {
        UpdateFulfillmentShopResult result = updateFulfillmentShopUseCase.updateShop(
                FulfillmentShopRequestToCommandMapper.mapToUpdateCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        UpdateFulfillmentShopResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 출고처 수정 성공"
                ),
                HttpStatus.OK
        );
    }
}
