package com.personal.marketnote.reward.port.in.usecase.gifticon;

import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsOrderCommand;

/**
 * 관리자 기프티콘 상품 노출 순서 관리 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-05
 * @Description 관리자가 노출 상품의 정렬 순서를 설정합니다.
 */
public interface ManageGifticonGoodsOrderUseCase {

    /**
     * @param command 순서 관리 요청 {@link ManageGifticonGoodsOrderCommand}
     * @Date 2026-04-05
     * @Author 성효빈
     * @Description 기프티콘 상품의 노출 순서를 변경합니다.
     */
    void manageOrder(ManageGifticonGoodsOrderCommand command);
}
