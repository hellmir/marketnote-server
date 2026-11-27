package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

import java.util.UUID;

@Builder
public record GetFulfillmentStockDetailCommand(
    String customerCode,
    String accessToken,
    String customerProductCode,
    UUID productKey,
    String fasstoProductCode,
    String outOfStockYn
) {
    /**
     * Admin/cstGodCd 경로 Command 생성.
     * <p>
     * cstGodCd는 운영자가 Fassto에 직접 등록한 운영 코드이므로 customerProductCode와 fasstoProductCode가 같다.
     */
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
            .fasstoProductCode(customerProductCode)
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
        String fasstoProductCode,
        String outOfStockYn
    ) {
        return GetFulfillmentStockDetailCommand.builder()
            .customerCode(customerCode)
            .accessToken(accessToken)
            .customerProductCode(productKey.toString())
            .productKey(productKey)
            .fasstoProductCode(fasstoProductCode)
            .outOfStockYn(outOfStockYn)
            .build();
    }

    public static GetFulfillmentStockDetailCommand forFasstoProductCode(
        String customerCode,
        String accessToken,
        UUID productKey,
        String fasstoProductCode,
        String outOfStockYn
    ) {
        return GetFulfillmentStockDetailCommand.builder()
            .customerCode(customerCode)
            .accessToken(accessToken)
            .customerProductCode(fasstoProductCode)
            .productKey(productKey)
            .fasstoProductCode(fasstoProductCode)
            .outOfStockYn(outOfStockYn)
            .build();
    }

}
