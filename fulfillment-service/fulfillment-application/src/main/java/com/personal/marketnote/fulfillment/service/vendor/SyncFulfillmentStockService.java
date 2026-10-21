package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.kafka.event.FulfillmentInventorySyncedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStockDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStocksCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.SyncFulfillmentAllStockCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.SyncFulfillmentStockCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentStockInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentStocksResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.*;
import com.personal.marketnote.fulfillment.port.out.event.PublishFulfillmentInventorySyncedEventPort;
import com.personal.marketnote.fulfillment.utility.FulfillmentAuthenticatedCaller;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class SyncFulfillmentStockService implements SyncFulfillmentStockUseCase, SyncFulfillmentAllStockUseCase {
    private static final String DEFAULT_OUT_OF_STOCK_INCLUDED = "Y";

    private final FulfillmentAuthenticatedCaller fulfillmentAuthenticatedCaller;
    private final GetFulfillmentStockDetailUseCase getFulfillmentStockDetailUseCase;
    private final GetFulfillmentStocksUseCase getFulfillmentStocksUseCase;
    private final PublishFulfillmentInventorySyncedEventPort publishFulfillmentInventorySyncedEventPort;

    @Override
    public void sync(SyncFulfillmentStockCommand command) {
        if (FormatValidator.hasNoValue(command)) {
            throw new FulfillmentQueryParameterNoValueException("Sync Fulfillment stock command");
        }
        if (FormatValidator.hasNoValue(command.customerCode())) {
            throw new FulfillmentQueryParameterNoValueException("Customer code", "Fulfillment stock sync");
        }
        if (FormatValidator.hasNoValue(command.productKeys())) {
            throw new FulfillmentQueryParameterNoValueException("Product keys", "Fulfillment stock sync");
        }

        List<UUID> productKeys = command.productKeys().stream()
                .filter(FormatValidator::hasValue)
                .distinct()
                .toList();
        if (FormatValidator.hasNoValue(productKeys)) {
            throw new FulfillmentQueryParameterNoValueException("Valid product keys", "Fulfillment stock sync");
        }

        List<FulfillmentInventorySyncedEvent.InventoryItem> inventories = new ArrayList<>();
        for (UUID productKey : productKeys) {
            GetFulfillmentStocksResult stockDetail = fulfillmentAuthenticatedCaller.call(token ->
                    getFulfillmentStockDetailUseCase.getStockDetail(
                            GetFulfillmentStockDetailCommand.of(
                                    command.customerCode(),
                                    token,
                                    productKey.toString(),
                                    productKey,
                                    DEFAULT_OUT_OF_STOCK_INCLUDED
                            )
                    )
            );
            int stockQuantity = resolveStockQuantity(stockDetail);
            inventories.add(new FulfillmentInventorySyncedEvent.InventoryItem(productKey, stockQuantity));
        }

        publishFulfillmentInventorySyncedEventPort.publish(new FulfillmentInventorySyncedEvent(inventories));
    }

    @Override
    public void syncAll(SyncFulfillmentAllStockCommand command) {
        if (FormatValidator.hasNoValue(command)) {
            throw new FulfillmentQueryParameterNoValueException("Sync all Fulfillment stock command");
        }
        if (FormatValidator.hasNoValue(command.customerCode())) {
            throw new FulfillmentQueryParameterNoValueException("Customer code", "Fulfillment stock sync");
        }

        GetFulfillmentStocksResult stocksResult = fulfillmentAuthenticatedCaller.call(token ->
                getFulfillmentStocksUseCase.getStocks(
                        GetFulfillmentStocksCommand.of(
                                command.customerCode(),
                                token,
                                null,
                                command.warehouseCode()
                        )
                )
        );
        List<FulfillmentInventorySyncedEvent.InventoryItem> inventories = resolveInventories(stocksResult);
        if (FormatValidator.hasNoValue(inventories)) {
            return;
        }

        publishFulfillmentInventorySyncedEventPort.publish(new FulfillmentInventorySyncedEvent(inventories));
    }

    private int resolveStockQuantity(GetFulfillmentStocksResult result) {
        if (FormatValidator.hasNoValue(result) || FormatValidator.hasNoValue(result.stocks())) {
            return 0;
        }

        int totalStock = 0;
        for (FulfillmentStockInfoResult stockInfo : result.stocks()) {
            if (FormatValidator.hasNoValue(stockInfo)) {
                continue;
            }
            Integer quantity = resolveAvailableQuantity(stockInfo);
            if (FormatValidator.hasValue(quantity)) {
                totalStock += Math.max(0, quantity);
            }
        }

        return totalStock;
    }

    private Integer resolveAvailableQuantity(FulfillmentStockInfoResult stockInfo) {
        if (FormatValidator.hasNoValue(stockInfo)) {
            return null;
        }

        Integer availableStockQuantity = stockInfo.availableStockQuantity();
        if (FormatValidator.hasValue(availableStockQuantity)) {
            return availableStockQuantity;
        }

        return stockInfo.stockQuantity();
    }

    private List<FulfillmentInventorySyncedEvent.InventoryItem> resolveInventories(GetFulfillmentStocksResult result) {
        if (FormatValidator.hasNoValue(result) || FormatValidator.hasNoValue(result.stocks())) {
            return List.of();
        }

        Map<UUID, Integer> stockByKey = new LinkedHashMap<>();
        for (FulfillmentStockInfoResult stockInfo : result.stocks()) {
            if (FormatValidator.hasNoValue(stockInfo) || FormatValidator.hasNoValue(stockInfo.customerProductCode())) {
                continue;
            }

            UUID productKey = parseProductKey(stockInfo.customerProductCode());
            if (FormatValidator.hasNoValue(productKey)) {
                continue;
            }

            Integer quantity = resolveAvailableQuantity(stockInfo);
            if (FormatValidator.hasNoValue(quantity)) {
                continue;
            }

            int updatedStock = stockByKey.getOrDefault(productKey, 0) + Math.max(0, quantity);
            stockByKey.put(productKey, updatedStock);
        }

        if (stockByKey.isEmpty()) {
            return List.of();
        }

        return stockByKey.entrySet().stream()
                .map(entry -> new FulfillmentInventorySyncedEvent.InventoryItem(entry.getKey(), entry.getValue()))
                .toList();
    }

    private UUID parseProductKey(String customerProductCode) {
        try {
            return UUID.fromString(customerProductCode);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
