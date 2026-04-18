package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentWarehousingResult;

/**
 * 파스토 입고 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-02-05
 * @Description 파스토 입고 수정 기능을 제공합니다.
 */
public interface UpdateFulfillmentWarehousingPort {

    /**
     * @param command 파스토 입고 수정 커맨드
     * @return 파스토 입고 수정 결과 {@link UpdateFulfillmentWarehousingResult}
     * @Date 2026-02-05
     * @Author 성효빈
     * @Description 파스토 입고를 수정합니다.
     */
    UpdateFulfillmentWarehousingResult updateWarehousing(UpdateFulfillmentWarehousingCommand command);
}
