package com.personal.marketnote.commerce.port.in.usecase.inventory;

import java.time.LocalDateTime;

public interface ExpireInventoryReservationUseCase {
    void expireTimedOutReservations(LocalDateTime cutoff);
}
