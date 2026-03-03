package com.personal.marketnote.commerce.port.out.settlement;

/**
 * 시스템 기본 수수료율을 제공하는 포트.
 * <p>
 * 판매자별 정산 정책이 등록되지 않은 경우 이 포트가 제공하는 기본 수수료율을 사용한다.
 * adapters 계층에서 SettlementSchedulerProperties를 읽어 구현한다.
 * </p>
 */
public interface DefaultSettlementPolicyProvider {
    Integer getDefaultPgFeeRate();

    Integer getDefaultPlatformFeeRate();
}
