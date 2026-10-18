package com.personal.marketnote.product.port.in.usecase.pricepolicysync;

import com.personal.marketnote.common.kafka.event.PricePolicyCreatedEvent;

import java.util.UUID;

public interface SyncPricePolicyCreatedUseCase {

    void syncPricePolicyCreated(UUID eventId, PricePolicyCreatedEvent event);
}
