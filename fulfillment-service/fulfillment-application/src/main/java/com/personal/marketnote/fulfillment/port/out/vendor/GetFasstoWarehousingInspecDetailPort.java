package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.warehousing.FasstoWarehousingInspecDetailQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoWarehousingInspecDetailResult;

/**
 * 파스토 입고 검수 상세 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-17
 * @Description 파스토 입고 검수 상세 조회 기능을 제공합니다.
 */
public interface GetFasstoWarehousingInspecDetailPort {

    /**
     * @param query 파스토 입고 검수 상세 조회 쿼리
     * @return 파스토 입고 검수 상세 조회 결과 {@link GetFasstoWarehousingInspecDetailResult}
     * @Date 2026-02-17
     * @Author 성효빈
     * @Description 파스토 입고 검수 상세 정보를 조회합니다.
     */
    GetFasstoWarehousingInspecDetailResult getWarehousingInspecDetail(FasstoWarehousingInspecDetailQuery query);
}
