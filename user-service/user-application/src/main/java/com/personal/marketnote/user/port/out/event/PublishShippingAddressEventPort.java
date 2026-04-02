package com.personal.marketnote.user.port.out.event;

import com.personal.marketnote.common.kafka.event.ShippingAddressChangeAction;

public interface PublishShippingAddressEventPort {

    void publishShippingAddressChangedEvent(Long shippingAddressId, Long userId, String recipientName, String recipientPhoneNumber, String address, ShippingAddressChangeAction action);
}
