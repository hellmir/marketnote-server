package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentShopsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentShopsResult;

/**
 * 풀필먼트 출고처 목록 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-28
 * @Description 풀필먼트 출고처 목록 조회 기능을 제공합니다.
 */
public interface GetFulfillmentShopsPort {

    /**
     * @param command 출고처 목록 조회 커맨드
     * @return 출고처 목록 조회 결과 {@link GetFulfillmentShopsResult}
     * @Date 2026-01-28
     * @Author 성효빈
     * @Description 풀필먼트 출고처 목록을 조회합니다.
     */
    GetFulfillmentShopsResult getShops(GetFulfillmentShopsCommand command);
}
