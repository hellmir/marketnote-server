package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.RegisterFasstoDirectReturnDeliveryApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper.FasstoDirectReturnDeliveryRequestToCommandMapper;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFasstoDirectReturnDeliveryRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFasstoDirectReturnDeliveryResponse;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFasstoDirectReturnDeliveryUseCase;
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
@RequestMapping("/api/v1/vendors/fassto/direct-return-deliveries")
@Tag(name = "파스토 반품(택배사 미지정) API", description = "파스토 반품 택배사 미지정 관련 API")
@RequiredArgsConstructor
public class FasstoDirectReturnDeliveryController {
    private final RegisterFasstoDirectReturnDeliveryUseCase registerFasstoDirectReturnDeliveryUseCase;

    /**
     * (관리자) 파스토 반품 택배사 미지정 등록 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      반품 택배사 미지정 등록 요청 정보
     * @Author Claude
     * @Date 2026-02-20
     * @Description 파스토 반품 택배사 미지정 등록(택배사 예약 없거나 다른 택배사 사용 시)을 요청합니다.
     */
    @PostMapping("/{customerCode}")
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
}
