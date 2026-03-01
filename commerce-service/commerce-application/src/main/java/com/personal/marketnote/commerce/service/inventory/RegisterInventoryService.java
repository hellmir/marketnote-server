package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.exception.InventoryAlreadyExistsException;
import com.personal.marketnote.commerce.port.in.command.inventory.RegisterInventoryCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RegisterInventoryUseCase;
import com.personal.marketnote.commerce.port.out.inventory.FindInventoryPort;
import com.personal.marketnote.commerce.port.out.inventory.SaveCacheStockPort;
import com.personal.marketnote.commerce.port.out.inventory.SaveInventoryPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import static org.hibernate.type.descriptor.java.IntegerJavaType.ZERO;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, propagation = REQUIRES_NEW)
public class RegisterInventoryService implements RegisterInventoryUseCase {
    private final SaveInventoryPort saveInventoryPort;
    private final SaveCacheStockPort saveCacheStockPort;
    private final FindInventoryPort findInventoryPort;

    @Override
    public void registerInventory(RegisterInventoryCommand command) {
        Long pricePolicyId = command.pricePolicyId();
        if (findInventoryPort.existsByPricePolicyId(pricePolicyId)) {
            throw new InventoryAlreadyExistsException(pricePolicyId);
        }

        Inventory inventory = Inventory.of(command.productId(), command.pricePolicyId());
        saveInventoryPort.save(inventory);

        saveCacheStockPort.save(command.pricePolicyId(), ZERO);
    }

    @Override
    public Set<Inventory> registerInventories(Set<RegisterInventoryCommand> commands) {
        Set<Inventory> inventories = commands.stream()
                .map(command -> Inventory.of(command.productId(), command.pricePolicyId()))
                .collect(Collectors.toSet());

        saveInventoryPort.save(inventories);
        saveCacheStockPort.save(inventories);

        return inventories;
    }
}
