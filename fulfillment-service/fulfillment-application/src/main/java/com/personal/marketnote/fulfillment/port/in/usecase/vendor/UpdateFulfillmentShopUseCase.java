package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentShopCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentShopResult;

/**
 * 파스토 출고처 수정 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-26
 * @Description 파스토 출고처 수정 기능을 제공합니다.
 */
public interface UpdateFulfillmentShopUseCase {
    /**
     * @param command 출고처 수정 커맨드
     * @return 출고처 수정 결과 {@link UpdateFulfillmentShopResult}
     * @Date 2026-01-26
     * @Author 성효빈
     * @Description 파스토 출고처를 수정합니다.
     */
    UpdateFulfillmentShopResult updateShop(UpdateFulfillmentShopCommand command);
}
