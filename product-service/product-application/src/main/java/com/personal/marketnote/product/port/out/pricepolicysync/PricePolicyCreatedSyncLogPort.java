package com.personal.marketnote.product.port.out.pricepolicysync;

import java.util.UUID;

public interface PricePolicyCreatedSyncLogPort {

    boolean isProcessed(UUID eventId);

    void markProcessed(UUID eventId);
}
