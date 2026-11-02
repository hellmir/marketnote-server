package com.personal.marketnote.product.port.out.pricepolicy;

import java.time.LocalDateTime;

/**
 * 인기도 갱신 포트
 *
 * @Author 성효빈
 * @Date 2026-03-21
 * @Description 가격 정책의 인기도 갱신 기능을 제공합니다.
 */
public interface UpdatePopularityPort {
    /**
     * @param since 인기도 집계 시작 시간 (이 시간 이후의 구매확정 수량을 집계)
     * @Date 2026-03-21
     * @Author 성효빈
     * @Description 최근 구매확정 수량 기준으로 가격 정책의 인기도를 갱신합니다.
     */
    void updateWeeklyPopularity(LocalDateTime since);
}
