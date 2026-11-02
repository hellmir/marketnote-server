package com.personal.marketnote.product.adapter.out.cache;

import com.personal.marketnote.common.domain.exception.illegalargument.numberformat.ParsingIntegerException;
import com.personal.marketnote.common.utility.FormatConverter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.port.out.inventory.FindCacheStockPort;
import com.personal.marketnote.product.port.out.inventory.SaveCacheStockPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CacheStockRedisAdapter implements SaveCacheStockPort, FindCacheStockPort {
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void save(Long pricePolicyId, int stock) {
        String key = buildKey(pricePolicyId);
        stringRedisTemplate.opsForValue().set(key, String.valueOf(stock));
    }

    @Override
    public int findByPricePolicyId(Long pricePolicyId) throws ParsingIntegerException {
        return FormatConverter.parseToInteger((
                stringRedisTemplate.opsForValue()
                        .get(buildKey(pricePolicyId))
        ));
    }

    @Override
    public Map<Long, Integer> findByPricePolicyIds(List<Long> pricePolicyIds) {
        List<String> stocks = stringRedisTemplate.opsForValue()
                .multiGet(
                        pricePolicyIds.stream()
                                .map(this::buildKey)
                                .toList()
                );

        int size = FormatValidator.hasValue(stocks)
                ? stocks.size()
                : 0;
        Map<Long, Integer> inventories = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            Integer parsedStock = null;
            try {
                parsedStock = FormatConverter.parseToInteger(stocks.get(i));
            } catch (ParsingIntegerException e) {
            }

            inventories.put(pricePolicyIds.get(i), parsedStock);
        }

        return inventories;
    }

    private String buildKey(Long pricePolicyId) {
        return "pricePolicy:" + pricePolicyId + ":stock";
    }
}
