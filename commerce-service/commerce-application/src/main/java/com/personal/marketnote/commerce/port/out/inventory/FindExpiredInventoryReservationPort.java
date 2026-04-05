package com.personal.marketnote.commerce.port.out.inventory;

import com.personal.marketnote.commerce.domain.inventory.InventoryReservation;

import java.time.LocalDateTime;
import java.util.List;

public interface FindExpiredInventoryReservationPort {
    List<InventoryReservation> findExpiredBefore(LocalDateTime cutoff);
}
