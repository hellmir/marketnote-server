package com.personal.marketnote.commerce.adapter.in.web.quickpayment.controller;

import com.personal.marketnote.commerce.adapter.in.web.quickpayment.controller.apidocs.ApproveQuickPaymentApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.quickpayment.controller.apidocs.DeleteQuickPaymentCardApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.quickpayment.controller.apidocs.IssueBatchKeyApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.quickpayment.controller.apidocs.RegisterQuickPaymentTransactionApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.quickpayment.request.ApproveQuickPaymentRequest;
import com.personal.marketnote.commerce.adapter.in.web.quickpayment.request.IssueBatchKeyRequest;
import com.personal.marketnote.commerce.adapter.in.web.quickpayment.response.ApproveQuickPaymentResponse;
import com.personal.marketnote.commerce.adapter.in.web.quickpayment.response.IssueBatchKeyResponse;
import com.personal.marketnote.commerce.adapter.in.web.quickpayment.response.RegisterQuickPaymentTransactionResponse;
import com.personal.marketnote.commerce.port.in.command.quickpayment.ApproveQuickPaymentCommand;
import com.personal.marketnote.commerce.port.in.command.quickpayment.DeleteQuickPaymentCardCommand;
import com.personal.marketnote.commerce.port.in.command.quickpayment.IssueBatchKeyCommand;
import com.personal.marketnote.commerce.port.in.command.quickpayment.RegisterQuickPaymentTransactionCommand;
import com.personal.marketnote.commerce.port.in.result.quickpayment.ApproveQuickPaymentResult;
import com.personal.marketnote.commerce.port.in.result.quickpayment.IssueBatchKeyResult;
import com.personal.marketnote.commerce.port.in.result.quickpayment.RegisterQuickPaymentTransactionResult;
import com.personal.marketnote.commerce.port.in.usecase.quickpayment.ApproveQuickPaymentUseCase;
import com.personal.marketnote.commerce.port.in.usecase.quickpayment.DeleteQuickPaymentCardUseCase;
import com.personal.marketnote.commerce.port.in.usecase.quickpayment.IssueBatchKeyUseCase;
import com.personal.marketnote.commerce.port.in.usecase.quickpayment.RegisterQuickPaymentTransactionUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 빠른결제 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-04-16
 * @Description 빠른결제 관련 API를 제공합니다.
 */
@RestController
@Tag(name = "빠른결제 API", description = "빠른결제 관련 API")
@RequiredArgsConstructor
@Validated
public class QuickPaymentController {
    private final RegisterQuickPaymentTransactionUseCase registerQuickPaymentTransactionUseCase;
    private final IssueBatchKeyUseCase issueBatchKeyUseCase;
    private final ApproveQuickPaymentUseCase approveQuickPaymentUseCase;
    private final DeleteQuickPaymentCardUseCase deleteQuickPaymentCardUseCase;

    /**
     * 빠른결제 거래 등록 (Mobile)
     *
     * @return 거래등록 응답 {@link RegisterQuickPaymentTransactionResponse}
     * @Author 성효빈
     * @Date 2026-04-16
     * @Description 빠른결제 카드 등록을 위한 KCP 거래등록 API를 호출합니다.
     */
    @PostMapping("/api/v1/quick-payments/transactions")
    @RegisterQuickPaymentTransactionApiDocs
    public ResponseEntity<BaseResponse<RegisterQuickPaymentTransactionResponse>> registerTransaction(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long userId = ElementExtractor.extractUserId(principal);

        RegisterQuickPaymentTransactionResult result = registerQuickPaymentTransactionUseCase.register(
                RegisterQuickPaymentTransactionCommand.builder()
                        .userId(userId)
                        .build()
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterQuickPaymentTransactionResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "빠른결제 거래 등록 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 빠른결제 카드 배치키 발급
     *
     * @param request 배치키 발급 요청
     * @return 배치키 발급 응답 {@link IssueBatchKeyResponse}
     * @Author 성효빈
     * @Date 2026-04-16
     * @Description 결제창 인증 완료 후 enc_data/enc_info로 KCP 배치키를 발급합니다.
     */
    @PostMapping("/api/v1/quick-payments/cards")
    @IssueBatchKeyApiDocs
    public ResponseEntity<BaseResponse<IssueBatchKeyResponse>> issueBatchKey(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @Valid @RequestBody IssueBatchKeyRequest request
    ) {
        Long userId = ElementExtractor.extractUserId(principal);

        IssueBatchKeyResult result = issueBatchKeyUseCase.issueBatchKey(
                IssueBatchKeyCommand.builder()
                        .userId(userId)
                        .encData(request.getEncData())
                        .encInfo(request.getEncInfo())
                        .build()
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        IssueBatchKeyResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "빠른결제 카드 배치키 발급 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 빠른결제 결제 승인
     *
     * @param request 빠른결제 승인 요청
     * @return 승인 응답 {@link ApproveQuickPaymentResponse}
     * @Author 성효빈
     * @Date 2026-04-16
     * @Description 저장된 배치키로 KCP 서버 투 서버 결제 승인을 수행합니다.
     */
    @PostMapping("/api/v1/quick-payments/approve")
    @ApproveQuickPaymentApiDocs
    public ResponseEntity<BaseResponse<ApproveQuickPaymentResponse>> approveQuickPayment(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @Valid @RequestBody ApproveQuickPaymentRequest request
    ) {
        Long userId = ElementExtractor.extractUserId(principal);

        ApproveQuickPaymentResult result = approveQuickPaymentUseCase.approve(
                ApproveQuickPaymentCommand.builder()
                        .buyerId(userId)
                        .orderKey(request.getOrderKey())
                        .quickPaymentCardId(request.getQuickPaymentCardId())
                        .goodName(request.getGoodName())
                        .build()
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        ApproveQuickPaymentResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "빠른결제 승인 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 빠른결제 카드 삭제
     *
     * @param quickPaymentCardId 빠른결제 카드 ID
     * @Author 성효빈
     * @Date 2026-04-16
     * @Description KCP에 배치키 삭제 요청 후 DB에서 카드를 비활성화합니다.
     */
    @DeleteMapping("/api/v1/quick-payments/cards/{quickPaymentCardId}")
    @DeleteQuickPaymentCardApiDocs
    public ResponseEntity<BaseResponse<Void>> deleteQuickPaymentCard(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @PathVariable("quickPaymentCardId") @NotNull @Min(1) Long quickPaymentCardId
    ) {
        Long userId = ElementExtractor.extractUserId(principal);

        deleteQuickPaymentCardUseCase.delete(
                DeleteQuickPaymentCardCommand.builder()
                        .quickPaymentCardId(quickPaymentCardId)
                        .userId(userId)
                        .build()
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "빠른결제 카드 삭제 성공"
                ),
                HttpStatus.OK
        );
    }
}
