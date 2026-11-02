package com.personal.marketnote.commerce.port.out.inventory;

import com.personal.marketnote.commerce.domain.inventory.InventoryReservation;

import java.util.List;
import java.util.Set;

public interface FindInventoryReservationPort {
    List<InventoryReservation> findByOrderIdAndPricePolicyIds(Long orderId, Set<Long> pricePolicyIds);
}
