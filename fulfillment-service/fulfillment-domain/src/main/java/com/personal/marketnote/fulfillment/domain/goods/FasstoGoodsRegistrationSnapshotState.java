package com.personal.marketnote.fulfillment.domain.goods;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FasstoGoodsRegistrationSnapshotState {
    private final Long id;
    private final Long productId;
    private final LocalDateTime createdAt;
}
