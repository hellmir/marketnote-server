package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.fulfillment.domain.vendor.supplier.FulfillmentSupplierMapper;
import com.personal.marketnote.fulfillment.domain.vendor.supplier.FulfillmentSupplierQuery;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSuppliersCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentSupplierCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentSupplierCommand;

public class FulfillmentSupplierCommandToRequestMapper {
    public static FulfillmentSupplierMapper mapToRegisterRequest(RegisterFulfillmentSupplierCommand command) {
        return FulfillmentSupplierMapper.register(
                command.customerCode(),
                command.accessToken(),
                command.supNm(),
                command.cstSupCd(),
                command.useYn(),
                command.dealStrDt(),
                command.dealEndDt(),
                command.zipNo(),
                command.addr1(),
                command.addr2(),
                command.ceoNm(),
                command.busNo(),
                command.busSp(),
                command.busTp(),
                command.telNo(),
                command.faxNo(),
                command.empNm1(),
                command.empPosit1(),
                command.empTelNo1(),
                command.empEmail1(),
                command.empNm2(),
                command.empPosit2(),
                command.empTelNo2(),
                command.empEmail2()
        );
    }

    public static FulfillmentSupplierQuery mapToSuppliersQuery(GetFulfillmentSuppliersCommand command) {
        return FulfillmentSupplierQuery.of(
                command.customerCode(),
                command.accessToken()
        );
    }

    public static FulfillmentSupplierMapper mapToUpdateRequest(UpdateFulfillmentSupplierCommand command) {
        return FulfillmentSupplierMapper.update(
                command.customerCode(),
                command.accessToken(),
                command.supCd(),
                command.supNm(),
                command.cstSupCd(),
                command.useYn(),
                command.dealStrDt(),
                command.dealEndDt(),
                command.zipNo(),
                command.addr1(),
                command.addr2(),
                command.ceoNm(),
                command.busNo(),
                command.busSp(),
                command.busTp(),
                command.telNo(),
                command.faxNo(),
                command.empNm1(),
                command.empPosit1(),
                command.empTelNo1(),
                command.empEmail1(),
                command.empNm2(),
                command.empPosit2(),
                command.empTelNo2(),
                command.empEmail2()
        );
    }
}
