package com.personal.marketnote.commerce.port.out.settlement;

public interface DeletePaymentAllocationPort {
    void deleteReturnAllocationsByOrderId(Long orderId);
}
