package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.port.in.usecase.product.GetProductInventoryUseCase;
import com.personal.marketnote.product.port.out.inventory.FindCacheStockPort;
import com.personal.marketnote.product.port.out.inventory.FindStockPort;
import com.personal.marketnote.product.port.out.result.GetInventoryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class GetProductInventoryService implements GetProductInventoryUseCase {
    private final FindCacheStockPort findCacheStockPort;
    private final FindStockPort findStockPort;

    @Override
    public Map<Long, Integer> getProductStocks(List<Long> pricePolicyIds) {
        // Cache Memory에서 상품 재고 수량 조회
        Map<Long, Integer> inventories = findCacheStockPort.findByPricePolicyIds(pricePolicyIds);

        List<Long> pricePolicyIdsWithoutStocks = pricePolicyIds.stream()
                .filter(pricePolicyId -> FormatValidator.hasNoValue(inventories.get(pricePolicyId)))
                .toList();

        if (FormatValidator.hasNoValue(pricePolicyIdsWithoutStocks)) {
            return inventories;
        }

        // Cache Memory에 저장돼 있지 않은 재고 수량 목록은 커머스 서비스 요청을 통해 조회
        Set<GetInventoryResult> restInventories
                = findStockPort.findByPricePolicyIds(pricePolicyIdsWithoutStocks);

        restInventories.forEach(
                restInventory -> inventories.put(restInventory.pricePolicyId(), restInventory.stock())
        );

        return inventories;
    }
}
