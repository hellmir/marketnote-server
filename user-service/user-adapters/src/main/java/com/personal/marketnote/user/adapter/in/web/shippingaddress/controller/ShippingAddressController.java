package com.personal.marketnote.user.adapter.in.web.shippingaddress.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.user.adapter.in.web.shippingaddress.controller.apidocs.GetMyShippingAddressesApiDocs;
import com.personal.marketnote.user.adapter.in.web.shippingaddress.controller.apidocs.RegisterShippingAddressApiDocs;
import com.personal.marketnote.user.adapter.in.web.shippingaddress.request.RegisterShippingAddressRequest;
import com.personal.marketnote.user.adapter.in.web.shippingaddress.response.GetMyShippingAddressesResponse;
import com.personal.marketnote.user.port.in.command.shippingaddress.RegisterShippingAddressCommand;
import com.personal.marketnote.user.port.in.result.shippingaddress.GetMyShippingAddressesResult;
import com.personal.marketnote.user.port.in.result.shippingaddress.RegisterShippingAddressResult;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.GetMyShippingAddressesUseCase;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.RegisterShippingAddressUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 배송지 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/shipping-addresses")
@Tag(
        name = "배송지 API",
        description = "배송지 관련 API"
)
@RequiredArgsConstructor
@Slf4j
public class ShippingAddressController {
    private final RegisterShippingAddressUseCase registerShippingAddressUseCase;
    private final GetMyShippingAddressesUseCase getMyShippingAddressesUseCase;

    /**
     * 배송지 등록
     *
     * @param request   배송지 등록 요청
     * @param principal OAuth2 인증 정보
     * @return 배송지 등록 결과 {@link RegisterShippingAddressResult}
     */
    @PostMapping
    @RegisterShippingAddressApiDocs
    public ResponseEntity<BaseResponse<RegisterShippingAddressResult>> registerShippingAddress(
            @Valid @RequestBody RegisterShippingAddressRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long userId = ElementExtractor.extractUserId(principal);

        RegisterShippingAddressResult result = registerShippingAddressUseCase.registerShippingAddress(
                RegisterShippingAddressCommand.builder()
                        .userId(userId)
                        .addressType(request.getAddressType())
                        .address(request.getAddress())
                        .addressDetail(request.getAddressDetail())
                        .companyName(request.getCompanyName())
                        .addressAlias(request.getAddressAlias())
                        .recipientName(request.getRecipientName())
                        .recipientPhoneNumber(request.getRecipientPhoneNumber())
                        .deliveryRequestType(request.getDeliveryRequestType())
                        .deliveryRequestMessage(request.getDeliveryRequestMessage())
                        .isDefault(request.getIsDefault())
                        .build()
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        result,
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "배송지 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * 내 배송지 목록 조회
     *
     * @param principal OAuth2 인증 정보
     * @return 내 배송지 목록 {@link GetMyShippingAddressesResponse}
     */
    @GetMapping("/me")
    @GetMyShippingAddressesApiDocs
    public ResponseEntity<BaseResponse<GetMyShippingAddressesResponse>> getMyShippingAddresses(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long userId = ElementExtractor.extractUserId(principal);

        GetMyShippingAddressesResult result = getMyShippingAddressesUseCase.getMyShippingAddresses(userId);

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetMyShippingAddressesResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "내 배송지 목록 조회 성공"
                )
        );
    }
}
