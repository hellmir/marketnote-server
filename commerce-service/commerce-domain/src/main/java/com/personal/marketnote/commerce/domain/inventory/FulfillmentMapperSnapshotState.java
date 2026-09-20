package com.personal.marketnote.commerce.domain.inventory;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FulfillmentMapperSnapshotState {
    private final Long productId;
    private final String wmsKey;
    private final String wmsProductKey;
    private final Stock stock;
}
