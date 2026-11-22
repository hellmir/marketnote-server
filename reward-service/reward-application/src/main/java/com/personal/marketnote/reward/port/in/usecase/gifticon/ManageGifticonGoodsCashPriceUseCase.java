package com.personal.marketnote.reward.port.in.usecase.gifticon;

import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsCashPriceCommand;

/**
 * 관리자 기프티콘 상품 사용자 판매가 관리 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-11-21
 * @Description 관리자가 기프티콘 상품의 사용자 판매가(cashPrice)를 변경합니다.
 */
public interface ManageGifticonGoodsCashPriceUseCase {

    /**
     * @param command 사용자 판매가 관리 요청 {@link ManageGifticonGoodsCashPriceCommand}
     * @Date 2026-11-21
     * @Author 성효빈
     * @Description 기프티콘 상품의 사용자 판매가(cashPrice)를 변경합니다.
     */
    void manageCashPrice(ManageGifticonGoodsCashPriceCommand command);
}
