package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.supplier.FulfillmentSupplierMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.supplier.FulfillmentSupplierQuery;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSuppliersCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentSupplierCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentSupplierCommand;

public class FasstoSupplierCommandToRequestMapper {
    public static FulfillmentSupplierMapper mapToRegisterRequest(RegisterFulfillmentSupplierCommand command) {
        return FulfillmentSupplierMapper.register(
                command.customerCode(),
                command.accessToken(),
                command.supplierName(),
                command.customerSupplierCode(),
                command.useYn(),
                command.dealStartDate(),
                command.dealEndDate(),
                command.zipCode(),
                command.address1(),
                command.address2(),
                command.ceoName(),
                command.businessNumber(),
                command.businessCategory(),
                command.businessType(),
                command.phoneNumber(),
                command.faxNumber(),
                command.primaryManagerName(),
                command.primaryManagerPosition(),
                command.primaryManagerPhoneNumber(),
                command.primaryManagerEmail(),
                command.secondaryManagerName(),
                command.secondaryManagerPosition(),
                command.secondaryManagerPhoneNumber(),
                command.secondaryManagerEmail()
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
                command.supplierCode(),
                command.supplierName(),
                command.customerSupplierCode(),
                command.useYn(),
                command.dealStartDate(),
                command.dealEndDate(),
                command.zipCode(),
                command.address1(),
                command.address2(),
                command.ceoName(),
                command.businessNumber(),
                command.businessCategory(),
                command.businessType(),
                command.phoneNumber(),
                command.faxNumber(),
                command.primaryManagerName(),
                command.primaryManagerPosition(),
                command.primaryManagerPhoneNumber(),
                command.primaryManagerEmail(),
                command.secondaryManagerName(),
                command.secondaryManagerPosition(),
                command.secondaryManagerPhoneNumber(),
                command.secondaryManagerEmail()
        );
    }
}
