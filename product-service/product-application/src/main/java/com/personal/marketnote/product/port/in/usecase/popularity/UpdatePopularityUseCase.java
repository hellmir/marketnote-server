package com.personal.marketnote.product.port.in.usecase.popularity;

/**
 * 인기도 갱신 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-03-21
 * @Description 상품 인기도 갱신 기능을 제공합니다.
 */
public interface UpdatePopularityUseCase {
    /**
     * @Date 2026-03-21
     * @Author 성효빈
     * @Description 최근 7일 구매확정 수량 기준으로 인기도를 갱신합니다.
     */
    void updateWeeklyPopularity();
}
