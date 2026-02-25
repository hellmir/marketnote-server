package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.warehousing.FasstoWarehousingMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFasstoWarehousingResult;

/**
 * 파스토 입고 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-02-05
 * @Description 파스토 입고 수정 기능을 제공합니다.
 */
public interface UpdateFasstoWarehousingPort {

    /**
     * @param request 파스토 입고 수정 요청 매퍼
     * @return 파스토 입고 수정 결과 {@link UpdateFasstoWarehousingResult}
     * @Date 2026-02-05
     * @Author 성효빈
     * @Description 파스토 입고를 수정합니다.
     */
    UpdateFasstoWarehousingResult updateWarehousing(FasstoWarehousingMapper request);
}
