package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoReturnGodDetailResult;

/**
 * 파스토 반품 상품 상세 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-20
 * @Description 파스토 반품 상품 상세 조회 기능을 제공합니다.
 */
public interface GetFasstoReturnGodDetailUseCase {
    GetFasstoReturnGodDetailResult getReturnGodDetail(GetFasstoReturnGodDetailCommand command);
}
