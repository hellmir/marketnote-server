package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentShopCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentShopResult;

/**
 * 풀필먼트 출고처 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-25
 * @Description 풀필먼트 출고처 등록 기능을 제공합니다.
 */
public interface RegisterFulfillmentShopUseCase {
    /**
     * @param command 출고처 등록 커맨드
     * @return 출고처 등록 결과 {@link RegisterFulfillmentShopResult}
     * @Date 2026-01-25
     * @Author 성효빈
     * @Description 풀필먼트 출고처를 등록합니다.
     */
    RegisterFulfillmentShopResult registerShop(RegisterFulfillmentShopCommand command);
}
