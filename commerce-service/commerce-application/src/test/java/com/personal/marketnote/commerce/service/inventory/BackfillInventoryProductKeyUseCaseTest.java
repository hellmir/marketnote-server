package com.personal.marketnote.commerce.service.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.inventory.InventorySnapshotState;
import com.personal.marketnote.commerce.port.in.command.inventory.BackfillInventoryProductKeyCommand;
import com.personal.marketnote.commerce.port.out.inventory.FindInventoryPort;
import com.personal.marketnote.commerce.port.out.inventory.UpdateInventoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackfillInventoryProductKeyUseCaseTest {

    @InjectMocks
    private BackfillInventoryProductKeyService service;

    @Mock
    private FindInventoryPort findInventoryPort;

    @Mock
    private UpdateInventoryPort updateInventoryPort;

    private Inventory buildInventory(Long productId, UUID productKey, Long pricePolicyId) {
        return Inventory.from(
                InventorySnapshotState.builder()
                        .productId(productId)
                        .productKey(productKey)
                        .pricePolicyId(pricePolicyId)
                        .stock(0)
                        .version(0L)
                        .reserved(0)
                        .build()
        );
    }

    @Test
    @DisplayName("productKey가 null인 인벤토리에만 productKey를 백필한다")
    void backfillsOnlyInventoriesWithNullProductKey() {
        // given
        UUID productKey = UUID.randomUUID();
        Inventory nullKeyInventory = buildInventory(1L, null, 100L);
        Inventory alreadyHasKey = buildInventory(1L, UUID.randomUUID(), 200L);
        when(findInventoryPort.findByProductIds(Set.of(1L)))
                .thenReturn(Set.of(nullKeyInventory, alreadyHasKey));

        // when
        service.backfillProductKey(BackfillInventoryProductKeyCommand.of(1L, productKey));

        // then
        ArgumentCaptor<Set<Inventory>> captor = ArgumentCaptor.forClass(Set.class);
        verify(updateInventoryPort).updateProductKey(captor.capture());
        Set<Inventory> targets = captor.getValue();
        assertThat(targets).hasSize(1);
        assertThat(targets.iterator().next().getPricePolicyId()).isEqualTo(100L);
        assertThat(targets.iterator().next().getProductKey()).isEqualTo(productKey);
    }

    @Test
    @DisplayName("조회된 인벤토리가 없으면 업데이트 Port를 호출하지 않는다")
    void doesNothingWhenNoInventoryFound() {
        // given
        when(findInventoryPort.findByProductIds(Set.of(1L))).thenReturn(Set.of());

        // when
        service.backfillProductKey(BackfillInventoryProductKeyCommand.of(1L, UUID.randomUUID()));

        // then
        verifyNoInteractions(updateInventoryPort);
    }

    @Test
    @DisplayName("모든 인벤토리가 이미 productKey를 가지고 있으면 업데이트 Port를 호출하지 않는다")
    void doesNothingWhenAllInventoriesAlreadyHaveProductKey() {
        // given
        Inventory hasKey1 = buildInventory(1L, UUID.randomUUID(), 100L);
        Inventory hasKey2 = buildInventory(1L, UUID.randomUUID(), 200L);
        when(findInventoryPort.findByProductIds(Set.of(1L))).thenReturn(Set.of(hasKey1, hasKey2));

        // when
        service.backfillProductKey(BackfillInventoryProductKeyCommand.of(1L, UUID.randomUUID()));

        // then
        verifyNoInteractions(updateInventoryPort);
    }
}
