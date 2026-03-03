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
        summary = "계정과목별 잔액 조회",
        description = """
                특정 시점 기준으로 계정과목별 DEBIT/CREDIT 누적 합계와 잔액을 조회합니다.

                잔액 계산 규칙:
                - 자산(ASSET), 비용(EXPENSE): DEBIT 합계 - CREDIT 합계
                - 부채(LIABILITY), 자본(EQUITY), 수익(REVENUE): CREDIT 합계 - DEBIT 합계

                - asOf: 기준 시점 (ISO 8601, 선택 — 미지정 시 현재 시점)
                """,
        parameters = {
                @Parameter(name = "as-of", description = "기준 시점 (ISO 8601)", example = "2026-02-28T23:59:59")
        },
        security = @SecurityRequirement(name = "bearer")
)
@ApiResponse(
        responseCode = "200",
        description = "잔액 조회 성공",
        content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                {
                  "data": [
                    {
                      "accountId": 1,
                      "accountName": "매출채권_PG",
                      "accountType": "ASSET",
                      "accountTypeDescription": "자산",
                      "debitTotal": 100000,
                      "creditTotal": 50000,
                      "balance": 50000
                    },
                    {
                      "accountId": 2,
                      "accountName": "미지급금_판매자",
                      "accountType": "LIABILITY",
                      "accountTypeDescription": "부채",
                      "debitTotal": 30000,
                      "creditTotal": 80000,
                      "balance": 50000
                    }
                  ],
                  "status": 200,
                  "code": "SUC01",
                  "message": "계정별 잔액 조회 성공"
                }
                """))
)
public @interface GetAccountBalancesApiDocs {
}
