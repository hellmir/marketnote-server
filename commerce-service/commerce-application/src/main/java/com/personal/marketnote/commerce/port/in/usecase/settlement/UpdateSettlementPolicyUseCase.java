package com.personal.marketnote.commerce.port.in.usecase.settlement;

import com.personal.marketnote.commerce.port.in.command.settlement.UpdateSettlementPolicyCommand;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementPolicyResult;

/**
 * 정산 정책 수정 유스케이스
 *
 * @author 성효빈
 * @description 기존 정산 정책의 수수료율, 정산 주기, 최소 지급 금액을 수정합니다.
 * @since 2026-03-02
 */
public interface UpdateSettlementPolicyUseCase {
    GetSettlementPolicyResult updatePolicy(UpdateSettlementPolicyCommand command);
}
