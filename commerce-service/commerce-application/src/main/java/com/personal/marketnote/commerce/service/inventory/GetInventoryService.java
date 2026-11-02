package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.port.in.command.inventory.RegisterInventoryCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.GetInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RegisterInventoryUseCase;
import com.personal.marketnote.commerce.port.out.inventory.FindInventoryPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetInventoryService implements GetInventoryUseCase {
    private final RegisterInventoryUseCase registerInventoryUseCase;
    private final FindInventoryPort findInventoryPort;

    @Override
    public Set<Inventory> getInventories(List<Long> pricePolicyIds) {
        return findInventoryPort.findByPricePolicyIds(new HashSet<>(pricePolicyIds));
    }

    @Override
    public Set<Inventory> getOrCreateInventories(Map<Long, Long> productIdsByPricePolicyId) {
        Set<Long> pricePolicyIds = productIdsByPricePolicyId.keySet();
        Set<Inventory> inventories = findInventoryPort.findByPricePolicyIds(pricePolicyIds);

        if (inventories.size() != pricePolicyIds.size()) {
            Set<Long> existingPricePolicyIds = inventories.stream()
                    .map(Inventory::getPricePolicyId)
                    .collect(Collectors.toSet());

            Set<RegisterInventoryCommand> commands = pricePolicyIds.stream()
                    .filter(pricePolicyId -> !existingPricePolicyIds.contains(pricePolicyId))
                    .map(pricePolicyId -> RegisterInventoryCommand.of(
                            productIdsByPricePolicyId.get(pricePolicyId), pricePolicyId
                    ))
                    .collect(Collectors.toSet());

            inventories.addAll(registerInventoryUseCase.registerInventories(commands));
        }

        return inventories;
    }

    @Override
    public boolean existsInventory(Long pricePolicyId) {
        return findInventoryPort.existsByPricePolicyId(pricePolicyId);
    }
}
