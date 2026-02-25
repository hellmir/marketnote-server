package com.personal.marketnote.commerce.port.out.reward;

import java.util.List;

public interface ModifyUserPointPort {
    /**
     * @param sharerIds 공유자 ID 목록
     * @Date 2026-01-18
     * @Author 성효빈
     * @Description 공유 구매 포인트를 적립합니다.
     */
    void accrueSharedPurchasePoints(List<Long> sharerIds);
}
