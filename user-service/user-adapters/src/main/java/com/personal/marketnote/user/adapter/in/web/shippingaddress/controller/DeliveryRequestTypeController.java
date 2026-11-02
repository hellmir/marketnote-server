package com.personal.marketnote.user.adapter.in.web.shippingaddress.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.user.adapter.in.web.shippingaddress.controller.apidocs.GetDeliveryRequestTypesApiDocs;
import com.personal.marketnote.user.adapter.in.web.shippingaddress.response.GetDeliveryRequestTypesResponse;
import com.personal.marketnote.user.port.in.result.shippingaddress.GetDeliveryRequestTypesResult;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.GetDeliveryRequestTypesUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 배송 요청사항 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-03-19
 * @Description 배송 요청사항 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/delivery-request-types")
@Tag(
        name = "배송 요청사항 API",
        description = "배송 요청사항 관련 API"
)
@RequiredArgsConstructor
public class DeliveryRequestTypeController {
    private final GetDeliveryRequestTypesUseCase getDeliveryRequestTypesUseCase;

    /**
     * 배송 요청사항 목록 조회
     *
     * @return 배송 요청사항 목록 {@link GetDeliveryRequestTypesResponse}
     */
    @GetMapping
    @GetDeliveryRequestTypesApiDocs
    public ResponseEntity<BaseResponse<List<GetDeliveryRequestTypesResponse>>> getDeliveryRequestTypes() {
        List<GetDeliveryRequestTypesResult> results = getDeliveryRequestTypesUseCase.getDeliveryRequestTypes();

        List<GetDeliveryRequestTypesResponse> response = results.stream()
                .map(GetDeliveryRequestTypesResponse::from)
                .toList();

        return ResponseEntity.ok(
                BaseResponse.of(
                        response,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "배송 요청사항 목록 조회 성공"
                )
        );
    }
}
