package com.personal.marketnote.product.port.in.usecase.pricepolicy;

import com.personal.marketnote.product.port.in.command.RegisterPricePolicyCommand;
import com.personal.marketnote.product.port.in.result.pricepolicy.RegisterPricePolicyResult;

/**
 * 가격 정책 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-01
 * @Description 가격 정책 등록 기능을 제공합니다.
 */
public interface RegisterPricePolicyUseCase {
    /**
     * @param userId  사용자 ID
     * @param isAdmin 관리자 여부
     * @param command 가격 정책 등록 커맨드
     * @return 가격 정책 등록 결과 {@link RegisterPricePolicyResult}
     * @Date 2026-02-13
     * @Author 성효빈
     * @Description 가격 정책을 등록합니다.
     */
    RegisterPricePolicyResult registerPricePolicy(
            Long userId, boolean isAdmin, RegisterPricePolicyCommand command
    );
}
