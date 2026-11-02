package com.personal.marketnote.commerce.adapter.in.web.ledger.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "회계 거래 목록 조회",
        description = """
                기간별, 거래 유형별로 회계 거래(분개 포함) 목록을 조회합니다.
                
                - startDate, endDate: 조회 기간 (ISO 8601, 선택)
                - transactionType: 거래 유형 필터 (선택)
                  - PAYMENT_APPROVAL, PAYMENT_CANCELLATION, PAYMENT_PARTIAL_REFUND
                  - PG_SETTLEMENT, SELLER_SETTLEMENT, SETTLEMENT_CANCELLATION
                """,
        parameters = {
                @Parameter(name = "start-date", description = "조회 시작일시 (ISO 8601)", example = "2026-02-01T00:00:00"),
                @Parameter(name = "end-date", description = "조회 종료일시 (ISO 8601)", example = "2026-02-28T23:59:59"),
                @Parameter(name = "transaction-type", description = "거래 유형 필터", example = "PG_SETTLEMENT")
        },
        security = @SecurityRequirement(name = "bearer")
)
@ApiResponse(
        responseCode = "200",
        description = "거래 목록 조회 성공",
        content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                {
                  "data": [
                    {
                      "id": 1,
                      "transactionType": "PG_SETTLEMENT",
                      "transactionTypeDescription": "PG 정산 입금",
                      "targetType": "SETTLEMENT",
                      "targetId": 100,
                      "description": "PG 정산 입금 - 정산 ID: 100",
                      "idempotencyKey": "PG_SETTLEMENT:100",
                      "createdAt": "2026-03-02T10:00:00",
                      "entries": [
                        {
                          "id": 1,
                          "accountId": 1,
                          "transactionId": 1,
                          "amount": 9700,
                          "transactionType": "DEBIT",
                          "transactionTypeDescription": "차변",
                          "createdAt": "2026-03-02T10:00:00"
                        }
                      ]
                    }
                  ],
                  "status": 200,
                  "code": "SUC01",
                  "message": "거래 목록 조회 성공"
                }
                """))
)
public @interface GetLedgerTransactionsApiDocs {
}
