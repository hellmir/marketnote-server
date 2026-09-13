package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.InvalidFulfillmentSyncCommandException;
import com.personal.marketnote.commerce.exception.InventoryProductNotFoundException;
import com.personal.marketnote.commerce.port.in.command.inventory.SyncFulfillmentVendorInventoryCommand;
import com.personal.marketnote.commerce.port.in.command.inventory.SyncFulfillmentVendorInventoryItemCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.SyncFulfillmentVendorInventoryUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.FulfillmentInventorySyncedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FulfillmentInventorySyncedConsumer {

    private final SyncFulfillmentVendorInventoryUseCase syncFulfillmentVendorInventoryUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.FULFILLMENT_INVENTORY_SYNCED,
            groupId = "commerce-fulfillment-inventory-sync"
    )
    public void handleFulfillmentInventorySyncedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.FULFILLMENT_INVENTORY_SYNCED)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            FulfillmentInventorySyncedEvent payload = envelope.getPayloadAs(
                    FulfillmentInventorySyncedEvent.class, objectMapper
            );

            log.info("풀필먼트 재고 동기화 이벤트 수신. eventId={}, inventoryCount={}",
                    envelope.eventId(), payload.inventories().size());

            List<SyncFulfillmentVendorInventoryItemCommand> items = payload.inventories().stream()
                    .map(item -> SyncFulfillmentVendorInventoryItemCommand.of(item.productId(), item.stock()))
                    .toList();

            SyncFulfillmentVendorInventoryCommand command = SyncFulfillmentVendorInventoryCommand.of(items);
            syncFulfillmentVendorInventoryUseCase.syncInventories(command);

            log.info("풀필먼트 재고 동기화 완료. eventId={}", envelope.eventId());
        } catch (InventoryProductNotFoundException | InvalidFulfillmentSyncCommandException e) {
            log.warn("풀필먼트 재고 동기화 실패 (재시도 불필요). eventId={}, error={}",
                    envelope.eventId(), e.getMessage());
        } catch (Exception e) {
            log.error("풀필먼트 재고 동기화 실패. eventId={}, error={}",
                    envelope.eventId(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }
}
