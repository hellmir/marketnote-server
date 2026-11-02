package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentGoodsResult;

/**
 * 풀필먼트 상품 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-01-30
 * @Description 풀필먼트 상품 수정 기능을 제공합니다.
 */
public interface UpdateFulfillmentGoodsPort {

    /**
     * @param command 풀필먼트 상품 수정 커맨드
     * @return 풀필먼트 상품 수정 결과 {@link UpdateFulfillmentGoodsResult}
     * @Date 2026-01-30
     * @Author 성효빈
     * @Description 풀필먼트 상품을 수정합니다.
     */
    UpdateFulfillmentGoodsResult updateGoods(UpdateFulfillmentGoodsCommand command);
}
