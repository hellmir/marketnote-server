package com.personal.marketnote.reward.port.in.usecase.gifticon;

import com.personal.marketnote.reward.port.in.command.gifticon.GetGifticonGoodsDetailCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonGoodsDetailResult;

/**
 * 기프티콘 상품 상세 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-05
 * @Description 사용자가 기프티콘 상품 상세 정보를 조회합니다.
 */
public interface GetGifticonGoodsDetailUseCase {
    /**
     * @param command 상품 상세 조회 커맨드 (상품 코드, 사용자 ID)
     * @return 상품 상세 정보 + 사용자 캐시 잔액 {@link GetGifticonGoodsDetailResult}
     * @Date 2026-04-05
     * @Author 성효빈
     * @Description 노출 및 판매 중인 상품의 상세 정보와 사용자 캐시 잔액을 조회합니다.
     */
    GetGifticonGoodsDetailResult getGoodsDetail(GetGifticonGoodsDetailCommand command);
}
