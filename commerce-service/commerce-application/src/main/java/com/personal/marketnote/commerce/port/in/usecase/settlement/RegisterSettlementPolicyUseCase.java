package com.personal.marketnote.commerce.port.in.usecase.settlement;

import com.personal.marketnote.commerce.port.in.command.settlement.RegisterSettlementPolicyCommand;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementPolicyResult;

/**
 * 정산 정책 등록 유스케이스
 *
 * @author 성효빈
 * @description 판매자별 정산 정책(수수료율, 정산 주기, 최소 지급 금액)을 등록합니다.
 * @since 2026-03-02
 */
public interface RegisterSettlementPolicyUseCase {
    GetSettlementPolicyResult registerPolicy(RegisterSettlementPolicyCommand command);
}
