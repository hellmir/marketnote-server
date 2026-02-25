package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoWarehousingDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoWarehousingDetailResult;

/**
 * 파스토 입고 상세 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-14
 * @Description 파스토 입고 상세 조회 기능을 제공합니다.
 */
public interface GetFasstoWarehousingDetailUseCase {
    GetFasstoWarehousingDetailResult getWarehousingDetail(GetFasstoWarehousingDetailCommand command);
}
