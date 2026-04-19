package com.personal.marketnote.product.domain.option;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductOptionSnapshotState {
    private final Long id;
    private final ProductOptionCategory category;
    private final String content;
    private final EntityStatus status;
}

