package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentGoodsResult;

/**
 * 풀필먼트 상품 상세 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-30
 * @Description 풀필먼트 상품 상세 조회 기능을 제공합니다.
 */
public interface GetFulfillmentGoodsDetailPort {

    /**
     * @param command 풀필먼트 상품 상세 조회 커맨드
     * @return 풀필먼트 상품 상세 조회 결과 {@link GetFulfillmentGoodsResult}
     * @Date 2026-01-30
     * @Author 성효빈
     * @Description 풀필먼트 상품 상세 정보를 조회합니다.
     */
    GetFulfillmentGoodsResult getGoodsDetail(GetFulfillmentGoodsDetailCommand command);
}
