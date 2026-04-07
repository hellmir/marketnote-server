package com.personal.marketnote.reward.port.in.usecase.gifticon;

import com.personal.marketnote.reward.port.in.result.gifticon.GetPopularGifticonGoodsResult;

/**
 * 기프티콘 인기 상품 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-05
 * @Description 인기 설정된 기프티콘 상품 목록을 조회합니다.
 */
public interface GetPopularGifticonGoodsUseCase {
    /**
     * @return 인기 상품 목록 (최대 10개) {@link GetPopularGifticonGoodsResult}
     * @Date 2026-04-05
     * @Author 성효빈
     * @Description 인기 설정 + 노출 + 판매 중인 상품을 orderNum 오름차순으로 조회합니다.
     */
    GetPopularGifticonGoodsResult getPopularGoods();
}
