package com.personal.marketnote.user.adapter.in.web.shippingaddress.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.user.adapter.in.web.shippingaddress.request.UpdateDeliveryRequestRequest;
import com.personal.marketnote.user.adapter.in.web.shippingaddress.response.GetShippingAddressResponse;
import com.personal.marketnote.user.port.in.command.shippingaddress.UpdateDeliveryRequestCommand;
import com.personal.marketnote.user.port.in.result.shippingaddress.GetShippingAddressResult;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.GetShippingAddressUseCase;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.UpdateDeliveryRequestUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 내부 배송지 컨트롤러 (서비스 간 통신용)
 *
 * @Author 성효빈
 * @Date 2026-03-27
 * @Description HMAC 인증 기반 서비스 간 통신용 배송지 조회 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/internal/shipping-addresses")
@Tag(
        name = "내부 배송지 API",
        description = "서비스 간 통신용 배송지 API"
)
@RequiredArgsConstructor
@Slf4j
public class InternalShippingAddressController {
    private final GetShippingAddressUseCase getShippingAddressUseCase;
    private final UpdateDeliveryRequestUseCase updateDeliveryRequestUseCase;

    /**
     * 배송지 정보 조회 (서비스 간 통신용)
     *
     * @param id     배송지 ID
     * @param userId 회원 ID
     * @return 배송지 조회 결과 {@link GetShippingAddressResponse}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<GetShippingAddressResponse>> getShippingAddress(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        GetShippingAddressResult result = getShippingAddressUseCase.getShippingAddress(id, userId);

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetShippingAddressResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "배송지 조회 성공"
                )
        );
    }

    /**
     * 배송 요청사항 수정 (서비스 간 통신용)
     *
     * @param id      배송지 ID
     * @param userId  회원 ID
     * @param request 배송 요청사항 수정 요청
     */
    @PatchMapping("/{id}/delivery-request")
    public ResponseEntity<BaseResponse<Void>> updateDeliveryRequest(
            @PathVariable Long id,
            @RequestParam Long userId,
            @Valid @RequestBody UpdateDeliveryRequestRequest request
    ) {
        updateDeliveryRequestUseCase.updateDeliveryRequest(
                id,
                userId,
                UpdateDeliveryRequestCommand.builder()
                        .deliveryRequestType(request.getDeliveryRequestType())
                        .deliveryRequestMessage(request.getDeliveryRequestMessage())
                        .build()
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "배송 요청사항 수정 성공"
                ),
                HttpStatus.OK
        );
    }
}
