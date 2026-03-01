package com.personal.marketnote.commerce.port.in.usecase.settlement;

import com.personal.marketnote.commerce.port.in.command.settlement.GetSellerSettlementsQuery;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementsResult;

/**
 * 판매자 정산 내역 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-03-02
 * @Description 판매자가 본인의 정산 내역을 연도/월 기준으로 조회합니다.
 */
public interface GetSellerSettlementsUseCase {
    GetSettlementsResult getSellerSettlements(GetSellerSettlementsQuery query);
}
