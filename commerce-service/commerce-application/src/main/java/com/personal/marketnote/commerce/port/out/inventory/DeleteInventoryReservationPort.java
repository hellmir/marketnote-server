package com.personal.marketnote.commerce.port.out.inventory;

import java.util.Set;

public interface DeleteInventoryReservationPort {
    void deleteByOrderIdAndPricePolicyIds(Long orderId, Set<Long> pricePolicyIds);
}
