package com.personal.marketnote.product.port.in.usecase.tagsync;

import com.personal.marketnote.common.kafka.event.ProductTagChangedEvent;

import java.util.UUID;

public interface SyncProductTagChangeUseCase {

    void syncProductTagChange(UUID eventId, ProductTagChangedEvent event);
}
