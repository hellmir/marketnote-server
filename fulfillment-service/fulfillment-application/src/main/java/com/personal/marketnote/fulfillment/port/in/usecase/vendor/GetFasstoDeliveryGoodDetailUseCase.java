package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoDeliveryGoodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoDeliveryGoodDetailResult;

/**
 * 파스토 출고 상품 상세 목록 조회 UseCase
 *
 * @Author 성효빈
 * @Date 2026-02-18
 */
public interface GetFasstoDeliveryGoodDetailUseCase {
    GetFasstoDeliveryGoodDetailResult getDeliveryGoodDetail(GetFasstoDeliveryGoodDetailCommand command);
}
