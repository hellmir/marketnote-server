package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

@Builder
public record RegisterFulfillmentShopCommand(
        String customerCode,
        String accessToken,
        String shopName,
        String customerShopCode,
        String dealStartDate,
        String dealEndDate,
        String zipCode,
        String address1,
        String address2,
        String ceoName,
        String businessNumber,
        String phoneNumber,
        String unloadMethod,
        String inspectionMethod,
        String standbyYn,
        String formType,
        String managerName,
        String managerPosition,
        String managerPhoneNumber,
        String useYn
) {
}
