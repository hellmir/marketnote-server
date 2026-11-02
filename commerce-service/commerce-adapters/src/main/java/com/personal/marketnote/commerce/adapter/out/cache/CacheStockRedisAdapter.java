package com.personal.marketnote.commerce.adapter.out.cache;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.port.out.inventory.SaveCacheStockPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class CacheStockRedisAdapter implements SaveCacheStockPort {
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void save(Set<Inventory> inventories) {
        inventories.forEach(inventory -> save(inventory.getPricePolicyId(), inventory.getStockValue()));
    }

    @Override
    public void save(Long pricePolicyId, int stock) {
        String key = buildKey(pricePolicyId);
        stringRedisTemplate.opsForValue().set(key, String.valueOf(stock));
    }

    private String buildKey(Long pricePolicyId) {
        return "pricePolicy:" + pricePolicyId + ":stock";
    }
}
