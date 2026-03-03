package com.personal.marketnote.commerce.port.in.usecase.settlement;

/**
 * 정산 정책 삭제(비활성화) 유스케이스
 *
 * @author 성효빈
 * @since 2026-03-02
 * @description 정산 정책을 소프트 삭제(비활성화)합니다.
 */
public interface DeleteSettlementPolicyUseCase {
    void deletePolicy(Long id);
}
