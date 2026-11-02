package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentWarehousingResult;

/**
 * 풀필먼트 입고 등록 포트
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 풀필먼트 입고 등록 기능을 제공합니다.
 */
public interface RegisterFulfillmentWarehousingPort {

    /**
     * @param command 풀필먼트 입고 등록 커맨드
     * @return 풀필먼트 입고 등록 결과 {@link RegisterFulfillmentWarehousingResult}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 풀필먼트 입고를 등록합니다.
     */
    RegisterFulfillmentWarehousingResult registerWarehousing(RegisterFulfillmentWarehousingCommand command);
}
