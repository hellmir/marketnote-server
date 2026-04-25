package com.personal.marketnote.commerce.port.in.usecase.order;

public interface AutoConfirmDeliveredOrdersUseCase {
    void autoConfirmDeliveredOrders(long autoConfirmDays);
}
