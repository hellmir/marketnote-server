package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.fassto.FasstoCustomerGoodsCodeEncoder;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.stock.FulfillmentStockDetailQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.stock.FulfillmentStockQuery;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStockDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStocksCommand;

import java.util.UUID;

public class FasstoStockCommandToRequestMapper {

    private static String resolveCustomerGoodsCode(UUID productKey, String fallbackCode, String fasstoProductCode) {
        if (FormatValidator.hasValue(fasstoProductCode)) {
            return fasstoProductCode;
        }
        return FasstoCustomerGoodsCodeEncoder.encodeOrFallback(productKey, fallbackCode);
    }
    public static FulfillmentStockQuery mapToQuery(GetFulfillmentStocksCommand command) {
        return FulfillmentStockQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.outOfStockYn(),
                command.warehouseCode()
        );
    }

    public static FulfillmentStockDetailQuery mapToDetailQuery(GetFulfillmentStockDetailCommand command) {
        return FulfillmentStockDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                resolveCustomerGoodsCode(command.productKey(), command.customerProductCode(), command.fasstoProductCode()),
                command.outOfStockYn()
        );
    }
}
