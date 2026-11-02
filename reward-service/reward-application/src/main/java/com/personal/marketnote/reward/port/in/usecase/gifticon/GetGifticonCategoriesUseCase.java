package com.personal.marketnote.reward.port.in.usecase.gifticon;

import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonCategoriesResult;

/**
 * 기프티콘 카테고리 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-05
 * @Description 사용자가 노출된 기프티콘 카테고리 목록을 조회합니다.
 */
public interface GetGifticonCategoriesUseCase {
    /**
     * @return 노출된 기프티콘 카테고리 목록 {@link GetGifticonCategoriesResult}
     * @Date 2026-04-05
     * @Author 성효빈
     * @Description 노출 설정된 카테고리 목록을 orderNum 오름차순으로 조회합니다.
     */
    GetGifticonCategoriesResult getCategories();
}
