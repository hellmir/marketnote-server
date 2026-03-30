package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentGoodsResult;

/**
 * 파스토 상품 수정 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-30
 * @Description 파스토 상품 수정 기능을 제공합니다.
 */
public interface UpdateFulfillmentGoodsUseCase {
    /**
     * @param command 상품 수정 커맨드
     * @return 상품 수정 결과 {@link UpdateFulfillmentGoodsResult}
     * @Date 2026-01-30
     * @Author 성효빈
     * @Description 파스토 상품 정보를 수정합니다.
     */
    UpdateFulfillmentGoodsResult updateGoods(UpdateFulfillmentGoodsCommand command);
}
