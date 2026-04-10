package com.personal.marketnote.commerce.port.out.inventory;

import com.personal.marketnote.commerce.domain.inventory.InventoryReservation;

import java.util.List;

public interface SaveInventoryReservationPort {
    void save(List<InventoryReservation> inventoryReservations);
}
