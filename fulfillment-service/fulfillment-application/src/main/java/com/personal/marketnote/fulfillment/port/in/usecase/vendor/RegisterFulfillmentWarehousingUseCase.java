package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentWarehousingResult;

/**
 * 파스토 입고 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 파스토 입고 등록 기능을 제공합니다.
 */
public interface RegisterFulfillmentWarehousingUseCase {
    /**
     * @param command 입고 요청 커맨드
     * @return 입고 요청 결과 {@link RegisterFulfillmentWarehousingResult}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 파스토 상품 입고 요청을 등록합니다.
     */
    RegisterFulfillmentWarehousingResult registerWarehousing(RegisterFulfillmentWarehousingCommand command);
}
