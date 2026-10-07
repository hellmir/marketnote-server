package com.personal.marketnote.commerce.adapter.out.cache;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.inventory.InventorySnapshotState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CacheStockRedisAdapter 단위 테스트")
class CacheStockRedisAdapterTest {

    @InjectMocks
    private CacheStockRedisAdapter adapter;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("가격정책 ID와 재고 수량으로 키를 만들어 저장한다")
    void shouldSaveStockByPricePolicyId() {
        // given
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        adapter.save(10L, 50);

        // then
        verify(valueOperations).set("pricePolicy:10:stock", "50");
    }

    @Test
    @DisplayName("Inventory 컬렉션을 모두 저장한다")
    void shouldSaveAllInventories() {
        // given
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        Inventory inventory1 = Inventory.from(
                InventorySnapshotState.builder()
                        .productId(1L)
                        .pricePolicyId(10L)
                        .stock(50)
                        .version(0L)
                        .reserved(0)
                        .build()
        );
        Inventory inventory2 = Inventory.from(
                InventorySnapshotState.builder()
                        .productId(2L)
                        .pricePolicyId(20L)
                        .stock(100)
                        .version(0L)
                        .reserved(0)
                        .build()
        );

        // when
        adapter.save(Set.of(inventory1, inventory2));

        // then
        verify(valueOperations, times(1)).set(eq("pricePolicy:10:stock"), eq("50"));
        verify(valueOperations, times(1)).set(eq("pricePolicy:20:stock"), eq("100"));
    }

    @Test
    @DisplayName("빈 Inventory 컬렉션은 아무 작업도 하지 않는다")
    void shouldDoNothingForEmptySet() {
        // when
        adapter.save(Set.of());

        // then
        verifyNoInteractions(valueOperations);
    }
}
