package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingDetailResult;

/**
 * 파스토 입고 상세 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-14
 * @Description 파스토 입고 상세 조회 기능을 제공합니다.
 */
public interface GetFulfillmentWarehousingDetailPort {

    /**
     * @param command 파스토 입고 상세 조회 커맨드
     * @return 파스토 입고 상세 조회 결과 {@link GetFulfillmentWarehousingDetailResult}
     * @Date 2026-02-14
     * @Author 성효빈
     * @Description 파스토 입고 상세 정보를 조회합니다.
     */
    GetFulfillmentWarehousingDetailResult getWarehousingDetail(GetFulfillmentWarehousingDetailCommand command);
}
