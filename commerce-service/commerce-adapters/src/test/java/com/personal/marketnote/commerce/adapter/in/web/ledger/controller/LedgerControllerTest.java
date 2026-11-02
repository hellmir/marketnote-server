package com.personal.marketnote.commerce.adapter.in.web.ledger.controller;

import com.personal.marketnote.commerce.adapter.in.web.ledger.response.GetAccountBalanceResponse;
import com.personal.marketnote.commerce.adapter.in.web.ledger.response.GetLedgerTransactionResponse;
import com.personal.marketnote.commerce.domain.ledger.AccountType;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransactionType;
import com.personal.marketnote.commerce.port.in.command.ledger.GetLedgerTransactionsQuery;
import com.personal.marketnote.commerce.port.in.result.ledger.GetAccountBalanceResult;
import com.personal.marketnote.commerce.port.in.result.ledger.GetLedgerTransactionResult;
import com.personal.marketnote.commerce.port.in.usecase.ledger.GetAccountBalanceUseCase;
import com.personal.marketnote.commerce.port.in.usecase.ledger.GetLedgerTransactionsUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LedgerController 테스트")
class LedgerControllerTest {

    @InjectMocks
    private LedgerController ledgerController;

    @Mock
    private GetLedgerTransactionsUseCase getLedgerTransactionsUseCase;

    @Mock
    private GetAccountBalanceUseCase getAccountBalanceUseCase;

    @Nested
    @DisplayName("getLedgerTransactions")
    class GetLedgerTransactions {

        @Test
        @DisplayName("회계 거래 목록 조회가 성공하면 200 OK와 거래 목록을 반환한다")
        void shouldReturnLedgerTransactions() {
            // given
            LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 1, 31, 23, 59);
            GetLedgerTransactionResult result = GetLedgerTransactionResult.builder()
                    .id(1L)
                    .transactionType(LedgerTransactionType.PAYMENT_APPROVAL)
                    .targetType("PAYMENT")
                    .targetId(100L)
                    .description("결제 승인")
                    .idempotencyKey("PAYMENT_APPROVAL:100")
                    .createdAt(LocalDateTime.of(2026, 1, 15, 0, 0))
                    .entries(List.of())
                    .build();
            when(getLedgerTransactionsUseCase.getLedgerTransactions(any(GetLedgerTransactionsQuery.class)))
                    .thenReturn(List.of(result));

            // when
            ResponseEntity<BaseResponse<List<GetLedgerTransactionResponse>>> response =
                    ledgerController.getLedgerTransactions(start, end, LedgerTransactionType.PAYMENT_APPROVAL);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            ArgumentCaptor<GetLedgerTransactionsQuery> captor =
                    ArgumentCaptor.forClass(GetLedgerTransactionsQuery.class);
            verify(getLedgerTransactionsUseCase).getLedgerTransactions(captor.capture());
            assertThat(captor.getValue().startDate()).isEqualTo(start);
            assertThat(captor.getValue().endDate()).isEqualTo(end);
            assertThat(captor.getValue().transactionType()).isEqualTo(LedgerTransactionType.PAYMENT_APPROVAL);
        }
    }

    @Nested
    @DisplayName("getAccountBalances")
    class GetAccountBalances {

        @Test
        @DisplayName("계정과목별 잔액 조회가 성공하면 200 OK와 잔액 목록을 반환한다")
        void shouldReturnAccountBalances() {
            // given
            LocalDateTime asOf = LocalDateTime.of(2026, 2, 1, 0, 0);
            GetAccountBalanceResult result = GetAccountBalanceResult.builder()
                    .accountId(1L)
                    .accountName("현금")
                    .accountType(AccountType.ASSET)
                    .debitTotal(100_000L)
                    .creditTotal(50_000L)
                    .balance(50_000L)
                    .build();
            when(getAccountBalanceUseCase.getAccountBalances(asOf)).thenReturn(List.of(result));

            // when
            ResponseEntity<BaseResponse<List<GetAccountBalanceResponse>>> response =
                    ledgerController.getAccountBalances(asOf);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).accountName()).isEqualTo("현금");
            verify(getAccountBalanceUseCase).getAccountBalances(asOf);
        }

        @Test
        @DisplayName("asOf 파라미터가 없으면 현재 시각 기준으로 조회한다")
        void shouldUseCurrentTimeWhenAsOfNull() {
            // given
            when(getAccountBalanceUseCase.getAccountBalances(any(LocalDateTime.class)))
                    .thenReturn(List.of());

            // when
            ResponseEntity<BaseResponse<List<GetAccountBalanceResponse>>> response =
                    ledgerController.getAccountBalances(null);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getAccountBalanceUseCase).getAccountBalances(any(LocalDateTime.class));
        }
    }
}
