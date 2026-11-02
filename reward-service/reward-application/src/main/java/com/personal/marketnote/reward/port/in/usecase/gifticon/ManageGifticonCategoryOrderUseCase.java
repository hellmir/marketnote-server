package com.personal.marketnote.reward.port.in.usecase.gifticon;

import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonCategoryOrderCommand;

/**
 * 관리자 기프티콘 카테고리 노출 순서 관리 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-05
 * @Description 관리자가 기프티콘 카테고리의 노출 순서를 설정합니다.
 */
public interface ManageGifticonCategoryOrderUseCase {

    /**
     * @param command 노출 순서 관리 커맨드 {@link ManageGifticonCategoryOrderCommand}
     * @Date 2026-04-05
     * @Author 성효빈
     * @Description 기프티콘 카테고리의 노출 순서를 설정합니다.
     */
    void manageOrder(ManageGifticonCategoryOrderCommand command);
}
