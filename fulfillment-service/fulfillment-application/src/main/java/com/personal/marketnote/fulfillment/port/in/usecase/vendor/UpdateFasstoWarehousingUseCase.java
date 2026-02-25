package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFasstoWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFasstoWarehousingResult;

/**
 * 파스토 입고 수정 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-05
 * @Description 파스토 입고 수정 기능을 제공합니다.
 */
public interface UpdateFasstoWarehousingUseCase {
    /**
     * @param command 입고 수정 요청 커맨드
     * @return 입고 수정 결과 {@link UpdateFasstoWarehousingResult}
     * @Date 2026-02-05
     * @Author 성효빈
     * @Description 파스토 상품 입고 요청을 수정합니다.
     */
    UpdateFasstoWarehousingResult updateWarehousing(UpdateFasstoWarehousingCommand command);
}
