package com.personal.marketnote.fulfillment.port.in.result.vendor;

import lombok.Builder;

@Builder
public record FulfillmentSupplierInfoResult(
        String supplierCode,
        String supplierName,
        String customerSupplierCode,
        String customerCode,
        String customerName,
        String dealStartDate,
        String dealEndDate,
        String zipCode,
        String address1,
        String address2,
        String ceoName,
        String businessNumber,
        String businessCategory,
        String businessType,
        String phoneNumber,
        String faxNumber,
        String primaryManagerName,
        String primaryManagerPosition,
        String primaryManagerPhoneNumber,
        String primaryManagerEmail,
        String secondaryManagerName,
        String secondaryManagerPosition,
        String secondaryManagerPhoneNumber,
        String secondaryManagerEmail,
        String useYn
) {
}
