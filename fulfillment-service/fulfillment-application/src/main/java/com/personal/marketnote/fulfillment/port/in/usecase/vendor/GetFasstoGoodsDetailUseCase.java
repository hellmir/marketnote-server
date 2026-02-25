package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoGoodsDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoGoodsResult;

/**
 * 파스토 상품 상세 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-05
 * @Description 파스토 상품 상세 조회 기능을 제공합니다.
 */
public interface GetFasstoGoodsDetailUseCase {
    /**
     * @param command 상품 조회 커맨드
     * @return 상품 조회 결과 {@link GetFasstoGoodsResult}
     * @Date 2026-02-05
     * @Author 성효빈
     * @Description 파스토 상품을 조회합니다.
     */
    GetFasstoGoodsResult getGoodsDetail(GetFasstoGoodsDetailCommand command);
}
