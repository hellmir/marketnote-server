package com.personal.marketnote.reward.port.in.usecase.gifticon;

import com.personal.marketnote.reward.port.in.command.gifticon.GetGifticonGoodsCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonGoodsResult;

/**
 * 기프티콘 상품 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-05
 * @Description 사용자가 노출된 기프티콘 상품 목록을 조회합니다.
 */
public interface GetGifticonGoodsUseCase {
    /**
     * @param command 상품 목록 조회 커맨드
     * @return 상품 목록 (페이징) {@link GetGifticonGoodsResult}
     * @Date 2026-04-05
     * @Author 성효빈
     * @Description 노출 설정 및 판매 중인 상품 목록을 페이징으로 조회합니다.
     */
    GetGifticonGoodsResult getGoods(GetGifticonGoodsCommand command);
}
