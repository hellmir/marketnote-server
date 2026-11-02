package com.personal.marketnote.commerce.adapter.out.persistence.settlement;

import com.personal.marketnote.commerce.configuration.SettlementSchedulerProperties;
import com.personal.marketnote.commerce.port.out.settlement.DefaultSettlementPolicyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * SettlementSchedulerProperties에서 시스템 기본 수수료율을 읽어 제공하는 어댑터.
 */
@Component
@RequiredArgsConstructor
public class DefaultSettlementPolicyAdapter implements DefaultSettlementPolicyProvider {
    private final SettlementSchedulerProperties properties;

    @Override
    public Integer getDefaultPgFeeRate() {
        return properties.getDefaultPgFeeRate();
    }

    @Override
    public Integer getDefaultPlatformFeeRate() {
        return properties.getDefaultPlatformFeeRate();
    }
}
