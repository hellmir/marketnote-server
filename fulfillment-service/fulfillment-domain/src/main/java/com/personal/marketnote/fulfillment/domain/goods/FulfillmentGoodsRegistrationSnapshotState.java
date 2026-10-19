package com.personal.marketnote.fulfillment.domain.goods;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class FulfillmentGoodsRegistrationSnapshotState {
    private final Long id;
    private final UUID productKey;
    private final LocalDateTime createdAt;
}
