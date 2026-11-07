package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.UUID;

import lombok.Builder;

@Builder
public record GetFulfillmentStockDetailCommand(
        String customerCode,
        String accessToken,
        String customerProductCode,
        UUID productKey,
        String outOfStockYn
) {
    public static GetFulfillmentStockDetailCommand of(
            String customerCode,
            String accessToken,
            String customerProductCode,
            String outOfStockYn
    ) {
        return GetFulfillmentStockDetailCommand.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .customerProductCode(customerProductCode)
                .productKey(null)
                .outOfStockYn(outOfStockYn)
                .build();
    }

    /**
     * productKey 기반 재고 상세 조회 Command 생성.
     * <p>
     * <b>productKey는 non-null 전제</b>다. 호출자가 사전 검증해야 하며,
     * 현재 호출자는 {@code SyncFulfillmentStockService}로,
     * {@code FormatValidator::hasValue} 필터링 후 진입하므로 NPE 위험이 없다.
     */
    public static GetFulfillmentStockDetailCommand forProductKey(
            String customerCode,
            String accessToken,
            UUID productKey,
            String outOfStockYn
    ) {
        return GetFulfillmentStockDetailCommand.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .customerProductCode(productKey.toString())
                .productKey(productKey)
                .outOfStockYn(outOfStockYn)
                .build();
    }

}
