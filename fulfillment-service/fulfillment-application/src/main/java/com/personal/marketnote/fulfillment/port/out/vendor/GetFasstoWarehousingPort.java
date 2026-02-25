package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.warehousing.FasstoWarehousingQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoWarehousingResult;

/**
 * 파스토 입고 목록 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-03
 * @Description 파스토 입고 목록 조회 기능을 제공합니다.
 */
public interface GetFasstoWarehousingPort {

    /**
     * @param query 파스토 입고 조회 쿼리
     * @return 파스토 입고 목록 조회 결과 {@link GetFasstoWarehousingResult}
     * @Date 2026-02-03
     * @Author 성효빈
     * @Description 파스토 입고 목록을 조회합니다.
     */
    GetFasstoWarehousingResult getWarehousing(FasstoWarehousingQuery query);
}
