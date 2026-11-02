package com.personal.marketnote.reward.port.in.usecase.gifticon;

import com.personal.marketnote.reward.port.in.command.gifticon.GetAdminGifticonGoodsCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetAdminGifticonGoodsResult;

/**
 * 관리자 기프티콘 전체 상품 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-05
 * @Description 관리자가 기프티쇼에서 동기화된 전체 상품을 조회합니다.
 */
public interface GetAdminGifticonGoodsUseCase {

    /**
     * @param command 조회 조건 {@link GetAdminGifticonGoodsCommand}
     * @return 페이징된 상품 목록 {@link GetAdminGifticonGoodsResult}
     * @Date 2026-04-05
     * @Author 성효빈
     * @Description 관리자 기프티콘 전체 상품 목록을 조회합니다.
     */
    GetAdminGifticonGoodsResult getAdminGifticonGoods(GetAdminGifticonGoodsCommand command);
}
