package com.personal.marketnote.reward.port.in.usecase.gifticon;

import com.personal.marketnote.reward.port.in.result.gifticon.GetAdminGifticonCategoriesResult;

/**
 * 관리자 기프티콘 카테고리 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-05
 * @Description 관리자가 전체 기프티콘 카테고리 목록을 조회합니다.
 */
public interface GetAdminGifticonCategoriesUseCase {

    /**
     * @return 기프티콘 카테고리 목록 {@link GetAdminGifticonCategoriesResult}
     * @Date 2026-04-05
     * @Author 성효빈
     * @Description 전체 기프티콘 카테고리 목록을 조회합니다.
     */
    GetAdminGifticonCategoriesResult getAdminGifticonCategories();
}
