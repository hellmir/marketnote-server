package com.personal.marketnote.commerce.adapter.in.web.ledger.controller;

import com.personal.marketnote.commerce.adapter.in.web.ledger.controller.apidocs.GetAccountBalancesApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.ledger.controller.apidocs.GetLedgerTransactionsApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.ledger.response.GetAccountBalanceResponse;
import com.personal.marketnote.commerce.adapter.in.web.ledger.response.GetLedgerTransactionResponse;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransactionType;
import com.personal.marketnote.commerce.port.in.command.ledger.GetLedgerTransactionsQuery;
import com.personal.marketnote.commerce.port.in.result.ledger.GetAccountBalanceResult;
import com.personal.marketnote.commerce.port.in.result.ledger.GetLedgerTransactionResult;
import com.personal.marketnote.commerce.port.in.usecase.ledger.GetAccountBalanceUseCase;
import com.personal.marketnote.commerce.port.in.usecase.ledger.GetLedgerTransactionsUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 회계 장부 컨트롤러 (관리자 전용)
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@RestController
@RequestMapping("/api/v1/admin/ledger")
@Tag(name = "(관리자) 회계 장부 API", description = "관리자 회계 거래/잔액 조회 API")
@RequiredArgsConstructor
public class LedgerController {
    private final GetLedgerTransactionsUseCase getLedgerTransactionsUseCase;
    private final GetAccountBalanceUseCase getAccountBalanceUseCase;

    /**
     * 회계 거래 목록 조회
     *
     * @param startDate       조회 시작일시
     * @param endDate         조회 종료일시
     * @param transactionType 거래 유형 필터
     * @return 거래 목록 (분개 포함)
     * @author 성효빈
     * @since 2026-03-02
     */
    @GetMapping("/transactions")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetLedgerTransactionsApiDocs
    public ResponseEntity<BaseResponse<List<GetLedgerTransactionResponse>>> getLedgerTransactions(
            @RequestParam(value = "start-date", required = false) LocalDateTime startDate,
            @RequestParam(value = "end-date", required = false) LocalDateTime endDate,
            @RequestParam(value = "transaction-type", required = false) LedgerTransactionType transactionType
    ) {
        GetLedgerTransactionsQuery query = GetLedgerTransactionsQuery.builder()
                .startDate(startDate)
                .endDate(endDate)
                .transactionType(transactionType)
                .build();

        List<GetLedgerTransactionResult> results = getLedgerTransactionsUseCase.getLedgerTransactions(query);
        List<GetLedgerTransactionResponse> responses = results.stream()
                .map(GetLedgerTransactionResponse::from)
                .toList();

        return new ResponseEntity<>(
                BaseResponse.of(
                        responses,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "거래 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 계정과목별 잔액 조회
     *
     * @param asOf 기준 시점 (미지정 시 현재 시점)
     * @return 계정별 잔액 목록
     * @author 성효빈
     * @since 2026-03-02
     */
    @GetMapping("/balances")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetAccountBalancesApiDocs
    public ResponseEntity<BaseResponse<List<GetAccountBalanceResponse>>> getAccountBalances(
            @RequestParam(value = "as-of", required = false) LocalDateTime asOf
    ) {
        LocalDateTime effectiveAsOf = (asOf != null) ? asOf : LocalDateTime.now();

        List<GetAccountBalanceResult> results = getAccountBalanceUseCase.getAccountBalances(effectiveAsOf);
        List<GetAccountBalanceResponse> responses = results.stream()
                .map(GetAccountBalanceResponse::from)
                .toList();

        return new ResponseEntity<>(
                BaseResponse.of(
                        responses,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "계정별 잔액 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
