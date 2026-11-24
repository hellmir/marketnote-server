package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentGoodsResult;

import java.util.UUID;

/**
 * 풀필먼트 상품 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-29
 * @Description 풀필먼트 상품 등록 기능을 제공합니다.
 */
public interface RegisterFulfillmentGoodsUseCase {
    /**
     * @param command 상품 등록 커맨드
     * @return 상품 등록 결과 {@link RegisterFulfillmentGoodsResult}
     * @Date 2026-01-29
     * @Author 성효빈
     * @Description 풀필먼트 상품을 등록합니다.
     */
    RegisterFulfillmentGoodsResult registerGoods(RegisterFulfillmentGoodsCommand command);

    RegisterFulfillmentGoodsResult registerGoodsIdempotent(RegisterFulfillmentGoodsCommand command);

    /**
     * import 케이스 운영자가 Fastto에 직접 등록한 상품의 (productKey, fasstoProductCode) 매핑을 로컬에 기록한다.
     * Fastto 송신은 수행하지 않는다 (이미 운영자가 직접 등록했으므로).
     * 재고 동기화 시 customerProductCode → productKey 역매핑 용도로 사용된다 (#5187).
     */
    void recordImportedGoodsRegistration(UUID productKey, String fasstoProductCode);
}
