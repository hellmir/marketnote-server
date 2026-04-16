package com.personal.marketnote.commerce.adapter.in.web.quickpayment.controller;

import com.personal.marketnote.commerce.adapter.in.web.quickpayment.controller.apidocs.RegisterQuickPaymentTransactionApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.quickpayment.response.RegisterQuickPaymentTransactionResponse;
import com.personal.marketnote.commerce.port.in.command.quickpayment.RegisterQuickPaymentTransactionCommand;
import com.personal.marketnote.commerce.port.in.result.quickpayment.RegisterQuickPaymentTransactionResult;
import com.personal.marketnote.commerce.port.in.usecase.quickpayment.RegisterQuickPaymentTransactionUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class QuickPaymentController {
    private final RegisterQuickPaymentTransactionUseCase registerQuickPaymentTransactionUseCase;

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
}
