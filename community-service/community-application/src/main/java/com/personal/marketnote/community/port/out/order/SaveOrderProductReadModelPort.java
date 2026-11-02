package com.personal.marketnote.community.port.out.order;

public interface SaveOrderProductReadModelPort {

    void upsert(Long orderId, Long pricePolicyId, Long buyerId, Long unitAmount);
}
