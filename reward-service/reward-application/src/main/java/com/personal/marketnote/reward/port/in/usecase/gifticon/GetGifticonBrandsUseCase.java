package com.personal.marketnote.reward.port.in.usecase.gifticon;

import com.personal.marketnote.reward.port.in.command.gifticon.GetGifticonBrandsCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonBrandsResult;

/**
 * 기프티콘 브랜드 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-05
 * @Description 특정 카테고리 내 기프티콘 브랜드 목록을 조회합니다.
 */
public interface GetGifticonBrandsUseCase {
    /**
     * @param command 브랜드 조회 커맨드 (카테고리 코드)
     * @return 브랜드 목록 {@link GetGifticonBrandsResult}
     * @Date 2026-04-05
     * @Author 성효빈
     * @Description 해당 카테고리에 노출된 판매 중 상품이 있는 브랜드를 조회합니다.
     */
    GetGifticonBrandsResult getBrands(GetGifticonBrandsCommand command);
}
