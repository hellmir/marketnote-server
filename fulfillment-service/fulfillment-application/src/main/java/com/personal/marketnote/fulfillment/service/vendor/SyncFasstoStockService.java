package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.FasstoAccessToken;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoStockDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoStocksCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.SyncFasstoAllStockCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.SyncFasstoStockCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FasstoStockInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoStocksResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.*;
import com.personal.marketnote.fulfillment.port.out.commerce.UpdateCommerceInventoryCommand;
import com.personal.marketnote.fulfillment.port.out.commerce.UpdateCommerceInventoryItemCommand;
import com.personal.marketnote.fulfillment.port.out.commerce.UpdateCommerceInventoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class SyncFasstoStockService implements SyncFasstoStockUseCase, SyncFasstoAllStockUseCase {
    private static final String DEFAULT_OUT_OF_STOCK_INCLUDED = "Y";

    private final RequestFasstoAuthUseCase requestFasstoAuthUseCase;
    private final GetFasstoStockDetailUseCase getFasstoStockDetailUseCase;
    private final GetFasstoStocksUseCase getFasstoStocksUseCase;
    private final UpdateCommerceInventoryPort updateCommerceInventoryPort;

    @Override
    public void sync(SyncFasstoStockCommand command) {
        if (FormatValidator.hasNoValue(command)) {
            throw new FasstoQueryParameterNoValueException("Sync Fassto stock command");
        }
        if (FormatValidator.hasNoValue(command.customerCode())) {
            throw new FasstoQueryParameterNoValueException("Customer code", "Fassto stock sync");
        }
        if (FormatValidator.hasNoValue(command.productIds())) {
            throw new FasstoQueryParameterNoValueException("Product ids", "Fassto stock sync");
        }

        List<Long> productIds = command.productIds().stream()
                .filter(productId -> FormatValidator.hasValue(productId))
                .distinct()
                .toList();
        if (FormatValidator.hasNoValue(productIds)) {
            throw new FasstoQueryParameterNoValueException("Valid product ids", "Fassto stock sync");
        }

        FasstoAccessToken accessToken = requestFasstoAuthUseCase.requestAccessToken();
        if (FormatValidator.hasNoValue(accessToken) || FormatValidator.hasNoValue(accessToken.getValue())) {
            throw new FasstoQueryParameterNoValueException("Fassto access token", "stock sync");
        }

        List<UpdateCommerceInventoryItemCommand> inventories = new ArrayList<>();
        for (Long productId : productIds) {
            GetFasstoStocksResult stockDetail = getFasstoStockDetailUseCase.getStockDetail(
                    GetFasstoStockDetailCommand.of(
                            command.customerCode(),
                            accessToken.getValue(),
                            String.valueOf(productId),
                            DEFAULT_OUT_OF_STOCK_INCLUDED
                    )
            );
            int stockQuantity = resolveStockQuantity(stockDetail);
            inventories.add(UpdateCommerceInventoryItemCommand.of(productId, stockQuantity));
        }

        updateCommerceInventoryPort.updateInventories(UpdateCommerceInventoryCommand.of(inventories));
    }

    @Override
    public void syncAll(SyncFasstoAllStockCommand command) {
        if (FormatValidator.hasNoValue(command)) {
            throw new FasstoQueryParameterNoValueException("Sync all Fassto stock command");
        }
        if (FormatValidator.hasNoValue(command.customerCode())) {
            throw new FasstoQueryParameterNoValueException("Customer code", "Fassto stock sync");
        }

        FasstoAccessToken accessToken = requestFasstoAuthUseCase.requestAccessToken();
        if (FormatValidator.hasNoValue(accessToken) || FormatValidator.hasNoValue(accessToken.getValue())) {
            throw new FasstoQueryParameterNoValueException("Fassto access token", "stock sync");
        }

        GetFasstoStocksResult stocksResult = getFasstoStocksUseCase.getStocks(
                GetFasstoStocksCommand.of(
                        command.customerCode(),
                        accessToken.getValue(),
                        null,
                        command.whCd()
                )
        );
        List<UpdateCommerceInventoryItemCommand> inventories = resolveInventories(stocksResult);
        if (FormatValidator.hasNoValue(inventories)) {
            return;
        }

        updateCommerceInventoryPort.updateInventories(UpdateCommerceInventoryCommand.of(inventories));
    }

    private int resolveStockQuantity(GetFasstoStocksResult result) {
        if (FormatValidator.hasNoValue(result) || FormatValidator.hasNoValue(result.stocks())) {
            return 0;
        }

        int totalStock = 0;
        for (FasstoStockInfoResult stockInfo : result.stocks()) {
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

    private Integer resolveAvailableQuantity(FasstoStockInfoResult stockInfo) {
        if (FormatValidator.hasNoValue(stockInfo)) {
            return null;
        }

        Integer canStockQty = stockInfo.canStockQty();
        if (FormatValidator.hasValue(canStockQty)) {
            return canStockQty;
        }

        return stockInfo.stockQty();
    }

    private List<UpdateCommerceInventoryItemCommand> resolveInventories(GetFasstoStocksResult result) {
        if (FormatValidator.hasNoValue(result) || FormatValidator.hasNoValue(result.stocks())) {
            return List.of();
        }

        Map<Long, Integer> stockByProductId = new LinkedHashMap<>();
        for (FasstoStockInfoResult stockInfo : result.stocks()) {
            if (FormatValidator.hasNoValue(stockInfo) || FormatValidator.hasNoValue(stockInfo.cstGodCd())) {
                continue;
            }

            // QA 서버 스케줄러 작동 테스트 위해 임시로 변경
            Long productId = 1L;
            // Long productId = resolveProductId(stockInfo.cstGodCd());
            if (FormatValidator.hasNoValue(productId)) {
                continue;
            }

            Integer quantity = resolveAvailableQuantity(stockInfo);
            if (FormatValidator.hasNoValue(quantity)) {
                continue;
            }

            int updatedStock = stockByProductId.getOrDefault(productId, 0) + Math.max(0, quantity);
            stockByProductId.put(productId, updatedStock);
        }

        if (stockByProductId.isEmpty()) {
            return List.of();
        }

        return stockByProductId.entrySet().stream()
                .map(entry -> UpdateCommerceInventoryItemCommand.of(entry.getKey(), entry.getValue()))
                .toList();
    }

    private Long resolveProductId(String cstGodCd) {
        if (FormatValidator.hasNoValue(cstGodCd)) {
            return null;
        }

        try {
            return Long.parseLong(cstGodCd);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
