package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoGoodsResult;

/**
 * 파스토 상품 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-29
 * @Description 파스토 상품 등록 기능을 제공합니다.
 */
public interface RegisterFasstoGoodsUseCase {
    /**
     * @param command 상품 등록 커맨드
     * @return 상품 등록 결과 {@link RegisterFasstoGoodsResult}
     * @Date 2026-01-29
     * @Author 성효빈
     * @Description 파스토 상품을 등록합니다.
     */
    RegisterFasstoGoodsResult registerGoods(RegisterFasstoGoodsCommand command);
}
