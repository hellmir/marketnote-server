package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.shop.FulfillmentShopMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.shop.FulfillmentShopQuery;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentShopsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentShopCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentShopCommand;

public class FasstoShopCommandToRequestMapper {
    public static FulfillmentShopMapper mapToRegisterRequest(RegisterFulfillmentShopCommand command) {
        return FulfillmentShopMapper.register(
                command.customerCode(),
                command.accessToken(),
                command.shopName(),
                command.customerShopCode(),
                command.dealStartDate(),
                command.dealEndDate(),
                command.zipCode(),
                command.address1(),
                command.address2(),
                command.ceoName(),
                command.businessNumber(),
                command.phoneNumber(),
                command.unloadMethod(),
                command.inspectionMethod(),
                command.standbyYn(),
                command.formType(),
                command.managerName(),
                command.managerPosition(),
                command.managerPhoneNumber(),
                command.useYn()
        );
    }

    public static FulfillmentShopQuery mapToShopsQuery(GetFulfillmentShopsCommand command) {
        return FulfillmentShopQuery.of(
                command.customerCode(),
                command.accessToken()
        );
    }

    public static FulfillmentShopMapper mapToUpdateRequest(UpdateFulfillmentShopCommand command) {
        return FulfillmentShopMapper.update(
                command.customerCode(),
                command.accessToken(),
                command.shopCode(),
                command.shopName(),
                command.customerShopCode(),
                command.dealStartDate(),
                command.dealEndDate(),
                command.zipCode(),
                command.address1(),
                command.address2(),
                command.ceoName(),
                command.businessNumber(),
                command.phoneNumber(),
                command.unloadMethod(),
                command.inspectionMethod(),
                command.standbyYn(),
                command.formType(),
                command.managerName(),
                command.managerPosition(),
                command.managerPhoneNumber(),
                command.useYn()
        );
    }
}
