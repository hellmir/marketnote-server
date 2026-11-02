package com.personal.marketnote.reward.port.in.usecase.gifticon;

import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsExposureCommand;

/**
 * 관리자 기프티콘 노출 상품 관리 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-05
 * @Description 관리자가 기프티콘 상품의 노출 여부를 변경합니다.
 */
public interface ManageGifticonGoodsExposureUseCase {

    /**
     * @param command 노출 관리 요청 {@link ManageGifticonGoodsExposureCommand}
     * @Date 2026-04-05
     * @Author 성효빈
     * @Description 기프티콘 상품의 노출 여부를 변경합니다.
     */
    void manageExposure(ManageGifticonGoodsExposureCommand command);
}
