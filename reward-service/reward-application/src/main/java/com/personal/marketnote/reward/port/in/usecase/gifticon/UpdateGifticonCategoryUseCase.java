package com.personal.marketnote.reward.port.in.usecase.gifticon;

import com.personal.marketnote.reward.port.in.command.gifticon.UpdateGifticonCategoryCommand;

/**
 * 관리자 기프티콘 카테고리 수정 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-05
 * @Description 관리자가 기프티콘 카테고리의 표시명과 아이콘 URL을 수정합니다.
 */
public interface UpdateGifticonCategoryUseCase {

    /**
     * @param command 카테고리 수정 커맨드 {@link UpdateGifticonCategoryCommand}
     * @Date 2026-04-05
     * @Author 성효빈
     * @Description 기프티콘 카테고리를 수정합니다.
     */
    void updateGifticonCategory(UpdateGifticonCategoryCommand command);
}
