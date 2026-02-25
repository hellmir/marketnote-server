package com.personal.marketnote.commerce.port.out.reward;

import java.util.List;

/**
 * 회원 포인트 변경 포트
 *
 * @Author 성효빈
 * @Date 2026-01-18
 * @Description 회원 포인트 변경 관련 기능을 제공합니다.
 */
public interface ModifyUserPointPort {
    /**
     * @param sharerIds 공유자 ID 목록
     * @Date 2026-01-18
     * @Author 성효빈
     * @Description 공유 구매 포인트를 적립합니다.
     */
    void accrueSharedPurchasePoints(List<Long> sharerIds);
}
