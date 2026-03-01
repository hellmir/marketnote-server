package com.personal.marketnote.commerce.port.in.usecase.inventory;

import com.personal.marketnote.commerce.domain.order.OrderProduct;

import java.util.List;

public interface RestoreProductInventoryUseCase {
    void restore(List<OrderProduct> orderProducts, String reason);
}
