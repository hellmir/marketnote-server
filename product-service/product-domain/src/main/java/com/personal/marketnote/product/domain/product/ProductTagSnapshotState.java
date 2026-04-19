package com.personal.marketnote.product.domain.product;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductTagSnapshotState {
    private final Long id;
    private final Long productId;
    private final String name;
    private final Long orderNum;
    private final EntityStatus status;
}

