package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoReturnGodDetailResult;

/**
 * 파스토 반품 완료 상품 상세 목록 조회 UseCase
 *
 * @Author 성효빈
 * @Date 2026-02-20
 */
public interface GetFasstoReturnGodDetailUseCase {
    GetFasstoReturnGodDetailResult getReturnGodDetail(GetFasstoReturnGodDetailCommand command);
}
