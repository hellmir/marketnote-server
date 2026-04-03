package com.personal.marketnote.commerce.port.out.user;

public interface SaveShippingAddressReadModelPort {

    void upsert(Long shippingAddressId, Long userId, String recipientName, String recipientPhoneNumber, String address, String addressDetail);

    void deactivateByShippingAddressId(Long shippingAddressId);
}
