package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.warehousing.*;
import com.personal.marketnote.fulfillment.port.in.command.vendor.*;

import java.util.List;

public class FasstoWarehousingCommandToRequestMapper {
    public static FulfillmentWarehousingMapper mapToRegisterRequest(RegisterFulfillmentWarehousingCommand command) {
        List<FulfillmentWarehousingItemMapper> requests = command.warehousingRequests().stream()
                .map(FasstoWarehousingCommandToRequestMapper::mapItem)
                .toList();

        return FulfillmentWarehousingMapper.register(
                command.customerCode(),
                command.accessToken(),
                requests
        );
    }

    public static FulfillmentWarehousingQuery mapToQuery(GetFulfillmentWarehousingCommand command) {
        return FulfillmentWarehousingQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.startDate(),
                command.endDate(),
                command.warehousingMethod(),
                command.orderNumber(),
                command.workStatus()
        );
    }

    public static FulfillmentWarehousingDetailQuery mapToDetailQuery(GetFulfillmentWarehousingDetailCommand command) {
        return FulfillmentWarehousingDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.slipNumber(),
                command.orderNumber()
        );
    }

    public static FulfillmentWarehousingAbnormalQuery mapToAbnormalQuery(GetFulfillmentWarehousingAbnormalCommand command) {
        return FulfillmentWarehousingAbnormalQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.warehouseCode(),
                command.slipNumber()
        );
    }

    public static FulfillmentWarehousingAbnormalImageQuery mapToAbnormalImageQuery(
            GetFulfillmentWarehousingAbnormalImageCommand command
    ) {
        return FulfillmentWarehousingAbnormalImageQuery.of(
                command.accessToken(),
                command.slipNumber(),
                command.productCode(),
                command.goodsSerialNo(),
                command.fileSeq(),
                command.imageNumber()
        );
    }

    public static FulfillmentWarehousingInspecDetailQuery mapToInspecDetailQuery(
            GetFulfillmentWarehousingInspecDetailCommand command
    ) {
        return FulfillmentWarehousingInspecDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.slipNumber(),
                command.warehouseCode()
        );
    }

    public static FulfillmentWarehousingMapper mapToUpdateRequest(UpdateFulfillmentWarehousingCommand command) {
        List<FulfillmentWarehousingItemMapper> requests = command.warehousingRequests().stream()
                .map(FasstoWarehousingCommandToRequestMapper::mapUpdateItem)
                .toList();

        return FulfillmentWarehousingMapper.update(
                command.customerCode(),
                command.accessToken(),
                requests
        );
    }

    private static FulfillmentWarehousingItemMapper mapItem(RegisterFulfillmentWarehousingItemCommand item) {
        List<FulfillmentWarehousingGoodsMapper> goods = item.products().stream()
                .map(FasstoWarehousingCommandToRequestMapper::mapGoods)
                .toList();

        return FulfillmentWarehousingItemMapper.of(
                item.orderDate(),
                item.orderNumber(),
                item.warehousingMethod(),
                item.slipNumber(),
                item.courierCompany(),
                item.trackingNumber(),
                item.remark(),
                item.supplierCode(),
                item.expirationDate(),
                item.manufacturingDate(),
                item.preArrival(),
                goods
        );
    }

    private static FulfillmentWarehousingItemMapper mapUpdateItem(UpdateFulfillmentWarehousingItemCommand item) {
        List<FulfillmentWarehousingGoodsMapper> goods = item.products().stream()
                .map(FasstoWarehousingCommandToRequestMapper::mapUpdateGoods)
                .toList();

        return FulfillmentWarehousingItemMapper.update(
                item.orderDate(),
                item.orderNumber(),
                item.warehousingMethod(),
                item.slipNumber(),
                item.courierCompany(),
                item.trackingNumber(),
                item.remark(),
                item.supplierCode(),
                item.expirationDate(),
                item.manufacturingDate(),
                item.preArrival(),
                goods
        );
    }

    private static FulfillmentWarehousingGoodsMapper mapGoods(RegisterFulfillmentWarehousingGoodsCommand item) {
        return FulfillmentWarehousingGoodsMapper.of(
                item.productCode(),
                item.expirationDate(),
                item.orderQuantity()
        );
    }

    private static FulfillmentWarehousingGoodsMapper mapUpdateGoods(UpdateFulfillmentWarehousingGoodsCommand item) {
        return FulfillmentWarehousingGoodsMapper.of(
                item.productCode(),
                item.expirationDate(),
                item.orderQuantity()
        );
    }
}
