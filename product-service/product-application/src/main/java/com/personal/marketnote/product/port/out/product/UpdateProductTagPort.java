package com.personal.marketnote.product.port.out.product;

import java.util.Map;

public interface UpdateProductTagPort {
    void updateOrderNums(Long productId, Map<Long, Long> tagIdToOrderNumMap);
}
