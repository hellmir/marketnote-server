package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryDetailResult;

/**
 * 풀필먼트 출고 상세 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-12
 * @Description 풀필먼트 출고 상세 조회 기능을 제공합니다.
 */
public interface GetFulfillmentDeliveryDetailPort {

    /**
     * @param command 풀필먼트 출고 상세 조회 커맨드
     * @return 풀필먼트 출고 상세 조회 결과 {@link GetFulfillmentDeliveryDetailResult}
     * @Date 2026-02-12
     * @Author 성효빈
     * @Description 풀필먼트 출고 상세 정보를 조회합니다.
     */
    GetFulfillmentDeliveryDetailResult getDeliveryDetail(GetFulfillmentDeliveryDetailCommand command);
}
