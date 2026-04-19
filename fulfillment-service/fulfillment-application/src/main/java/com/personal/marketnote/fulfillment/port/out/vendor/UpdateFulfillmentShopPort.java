package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentShopCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentShopResult;

/**
 * 풀필먼트 출고처 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-01-28
 * @Description 풀필먼트 출고처 수정 기능을 제공합니다.
 */
public interface UpdateFulfillmentShopPort {

    /**
     * @param command 출고처 수정 커맨드
     * @return 출고처 수정 결과 {@link UpdateFulfillmentShopResult}
     * @Date 2026-01-28
     * @Author 성효빈
     * @Description 풀필먼트 출고처를 수정합니다.
     */
    UpdateFulfillmentShopResult updateShop(UpdateFulfillmentShopCommand command);
}
