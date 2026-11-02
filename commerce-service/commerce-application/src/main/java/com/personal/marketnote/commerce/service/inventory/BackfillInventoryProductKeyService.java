package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.port.in.command.inventory.BackfillInventoryProductKeyCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.BackfillInventoryProductKeyUseCase;
import com.personal.marketnote.commerce.port.out.inventory.FindInventoryPort;
import com.personal.marketnote.commerce.port.out.inventory.UpdateInventoryPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class BackfillInventoryProductKeyService implements BackfillInventoryProductKeyUseCase {
    private final FindInventoryPort findInventoryPort;
    private final UpdateInventoryPort updateInventoryPort;

    @Override
    public void backfillProductKey(BackfillInventoryProductKeyCommand command) {
        Set<Inventory> inventories = findInventoryPort.findByProductIds(Set.of(command.productId()));
        if (inventories.isEmpty()) {
            return;
        }

        Set<Inventory> targets = inventories.stream()
                .filter(inventory -> !inventory.hasProductKey())
                .collect(Collectors.toSet());
        if (targets.isEmpty()) {
            return;
        }

        targets.forEach(inventory -> inventory.assignProductKey(command.productKey()));
        updateInventoryPort.updateProductKey(targets);

        log.info("인벤토리 productKey 백필 완료. productId={}, count={}", command.productId(), targets.size());
    }
}
