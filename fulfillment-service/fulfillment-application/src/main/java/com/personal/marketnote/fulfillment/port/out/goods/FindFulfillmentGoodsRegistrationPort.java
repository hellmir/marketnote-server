package com.personal.marketnote.fulfillment.port.out.goods;

import java.util.Optional;
import java.util.UUID;

public interface FindFulfillmentGoodsRegistrationPort {
    boolean existsByProductKey(UUID productKey);

    Optional<UUID> findProductKeyByFasstoProductCode(String fasstoProductCode);
}
