package com.personal.marketnote.product.port.out.tagsync;

import java.util.UUID;

public interface ProductTagChangeSyncLogPort {

    boolean isProcessed(UUID eventId);

    void markProcessed(UUID eventId);
}
