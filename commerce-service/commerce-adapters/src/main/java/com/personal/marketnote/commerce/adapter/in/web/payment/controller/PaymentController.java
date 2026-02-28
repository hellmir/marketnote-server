package com.personal.marketnote.commerce.adapter.in.web.payment.controller;

import com.personal.marketnote.commerce.adapter.in.web.payment.controller.apidocs.ApprovePaymentApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.payment.controller.apidocs.CancelPaymentApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.payment.controller.apidocs.GetPaymentApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.payment.controller.apidocs.ReadyPaymentApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.payment.mapper.PaymentRequestToCommandMapper;
import com.personal.marketnote.commerce.adapter.in.web.payment.request.ApprovePaymentRequest;
import com.personal.marketnote.commerce.adapter.in.web.payment.request.CancelPaymentRequest;
import com.personal.marketnote.commerce.adapter.in.web.payment.request.ReadyPaymentRequest;
import com.personal.marketnote.commerce.adapter.in.web.payment.response.ApprovePaymentResponse;
import com.personal.marketnote.commerce.adapter.in.web.payment.response.GetPaymentResponse;
import com.personal.marketnote.commerce.adapter.in.web.payment.response.ReadyPaymentResponse;
import com.personal.marketnote.commerce.port.in.result.payment.ApprovePaymentResult;
import com.personal.marketnote.commerce.port.in.result.payment.GetPaymentResult;
import com.personal.marketnote.commerce.port.in.result.payment.ReadyPaymentResult;
import com.personal.marketnote.commerce.port.in.usecase.payment.ApprovePaymentUseCase;
import com.personal.marketnote.commerce.port.in.usecase.payment.CancelPaymentUseCase;
import com.personal.marketnote.commerce.port.in.usecase.payment.GetPaymentUseCase;
import com.personal.marketnote.commerce.port.in.usecase.payment.ReadyPaymentUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 결제 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-02-25
 * @Description 결제 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "결제 API", description = "결제 관련 API")
@RequiredArgsConstructor
public class PaymentController {
    private final ReadyPaymentUseCase readyPaymentUseCase;
    private final ApprovePaymentUseCase approvePaymentUseCase;
    private final CancelPaymentUseCase cancelPaymentUseCase;
    private final GetPaymentUseCase getPaymentUseCase;

    /**
     * 거래 등록 (Mobile)
     *
     * @param request 거래등록 요청
     * @return 거래등록 응답 {@link ReadyPaymentResponse}
     * @Author 성효빈
     * @Date 2026-02-25
     * @Description KCP 거래등록 API를 호출하여 결제 준비를 수행합니다.
     */
    @PostMapping("/ready")
    @ReadyPaymentApiDocs
    public ResponseEntity<BaseResponse<ReadyPaymentResponse>> readyPayment(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @Valid @RequestBody ReadyPaymentRequest request
    ) {
        Long buyerId = ElementExtractor.extractUserId(principal);
        ReadyPaymentResult result = readyPaymentUseCase.ready(
                PaymentRequestToCommandMapper.mapToCommand(request, buyerId)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        ReadyPaymentResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "거래 등록 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 결제 승인
     *
     * @param request 결제 승인 요청
     * @return 결제 승인 응답 {@link ApprovePaymentResponse}
     * @Author 성효빈
     * @Date 2026-02-25
     * @Description KCP 결제 승인 API를 호출합니다.
     */
    @PostMapping("/approve")
    @ApprovePaymentApiDocs
    public ResponseEntity<BaseResponse<ApprovePaymentResponse>> approvePayment(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @Valid @RequestBody ApprovePaymentRequest request
    ) {
        Long buyerId = ElementExtractor.extractUserId(principal);
        ApprovePaymentResult result = approvePaymentUseCase.approve(
                PaymentRequestToCommandMapper.mapToCommand(request, buyerId)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        ApprovePaymentResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "결제 승인 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 결제 정보 조회
     *
     * @param orderKey 주문 키 (UUID)
     * @return 결제 정보 조회 응답 {@link GetPaymentResponse}
     * @Author 성효빈
     * @Date 2026-02-25
     * @Description 주문 키로 결제 정보를 조회합니다.
     */
    @GetMapping("/{orderKey}")
    @GetPaymentApiDocs
    public ResponseEntity<BaseResponse<GetPaymentResponse>> getPayment(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @PathVariable("orderKey") String orderKey
    ) {
        Long buyerId = ElementExtractor.extractUserId(principal);
        GetPaymentResult result = getPaymentUseCase.getPayment(buyerId, orderKey);

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetPaymentResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "결제 정보 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 결제 취소
     *
     * @param orderKey 주문 키 (UUID)
     * @param request  결제 취소 요청
     * @Author 성효빈
     * @Date 2026-02-25
     * @Description KCP 결제 취소 API를 호출합니다.
     */
    @PostMapping("/{orderKey}/cancel")
    @CancelPaymentApiDocs
    public ResponseEntity<BaseResponse<Void>> cancelPayment(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @PathVariable("orderKey") String orderKey,
            @Valid @RequestBody CancelPaymentRequest request
    ) {
        Long buyerId = ElementExtractor.extractUserId(principal);
        cancelPaymentUseCase.cancel(
                PaymentRequestToCommandMapper.mapToCommand(orderKey, request, buyerId)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "결제 취소 성공"
                ),
                HttpStatus.OK
        );
    }
}
