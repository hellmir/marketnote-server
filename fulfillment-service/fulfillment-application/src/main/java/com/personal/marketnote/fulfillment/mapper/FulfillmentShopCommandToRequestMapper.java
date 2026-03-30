package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.fulfillment.domain.vendor.shop.FulfillmentShopMapper;
import com.personal.marketnote.fulfillment.domain.vendor.shop.FulfillmentShopQuery;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentShopsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentShopCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentShopCommand;

public class FulfillmentShopCommandToRequestMapper {
    public static FulfillmentShopMapper mapToRegisterRequest(RegisterFulfillmentShopCommand command) {
        return FulfillmentShopMapper.register(
                command.customerCode(),
                command.accessToken(),
                command.shopNm(),
                command.cstShopCd(),
                command.dealStrDt(),
                command.dealEndDt(),
                command.zipNo(),
                command.addr1(),
                command.addr2(),
                command.ceoNm(),
                command.busNo(),
                command.telNo(),
                command.unloadWay(),
                command.checkWay(),
                command.standYn(),
                command.formType(),
                command.empNm(),
                command.empPosit(),
                command.empTelNo(),
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
                command.shopCd(),
                command.shopNm(),
                command.cstShopCd(),
                command.dealStrDt(),
                command.dealEndDt(),
                command.zipNo(),
                command.addr1(),
                command.addr2(),
                command.ceoNm(),
                command.busNo(),
                command.telNo(),
                command.unloadWay(),
                command.checkWay(),
                command.standYn(),
                command.formType(),
                command.empNm(),
                command.empPosit(),
                command.empTelNo(),
                command.useYn()
        );
    }
}
