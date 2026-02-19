package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.RegisterFasstoReturnDeliveryApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper.FasstoReturnDeliveryRequestToCommandMapper;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFasstoReturnDeliveryRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFasstoReturnDeliveryResponse;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;
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

@RestController
@RequestMapping("/api/v1/vendors/fassto/return-deliveries")
@Tag(name = "파스토 반품 API", description = "파스토 반품 관련 API")
@RequiredArgsConstructor
public class FasstoReturnDeliveryController {
    private final RegisterFasstoReturnDeliveryUseCase registerFasstoReturnDeliveryUseCase;

    /**
     * (관리자) 파스토 반품 예약 등록 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      반품 예약 등록 요청 정보
     * @Author Claude
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
}
