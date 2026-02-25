package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.returndelivery.FasstoReturnGodDetailQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoReturnGodDetailResult;

/**
 * 파스토 반품 상품 상세 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-20
 * @Description 파스토 반품 상품 상세 조회 기능을 제공합니다.
 */
public interface GetFasstoReturnGodDetailPort {

    /**
     * @param query 파스토 반품 상품 상세 조회 쿼리
     * @return 파스토 반품 상품 상세 조회 결과 {@link GetFasstoReturnGodDetailResult}
     * @Date 2026-02-20
     * @Author 성효빈
     * @Description 파스토 반품 상품 상세 정보를 조회합니다.
     */
    GetFasstoReturnGodDetailResult getReturnGodDetail(FasstoReturnGodDetailQuery query);
}
