package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingAbnormalCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingAbnormalResult;

/**
 * 풀필먼트 입고 이상 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-14
 * @Description 풀필먼트 입고 이상 조회 기능을 제공합니다.
 */
public interface GetFulfillmentWarehousingAbnormalPort {

    /**
     * @param command 풀필먼트 입고 이상 조회 커맨드
     * @return 풀필먼트 입고 이상 조회 결과 {@link GetFulfillmentWarehousingAbnormalResult}
     * @Date 2026-02-14
     * @Author 성효빈
     * @Description 풀필먼트 입고 이상 정보를 조회합니다.
     */
    GetFulfillmentWarehousingAbnormalResult getWarehousingAbnormal(GetFulfillmentWarehousingAbnormalCommand command);
}
