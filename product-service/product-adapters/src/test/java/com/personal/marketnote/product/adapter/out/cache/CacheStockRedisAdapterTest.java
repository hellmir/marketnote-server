package com.personal.marketnote.product.adapter.out.cache;

import com.personal.marketnote.common.domain.exception.illegalargument.numberformat.ParsingIntegerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheStockRedisAdapterTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private CacheStockRedisAdapter adapter;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("가격정책 ID와 재고를 Redis에 저장한다")
        void savesStockToRedis() {
            adapter.save(100L, 50);

            verify(valueOperations).set("pricePolicy:100:stock", "50");
        }
    }

    @Nested
    @DisplayName("findByPricePolicyId")
    class FindByPricePolicyId {

        @Test
        @DisplayName("가격정책 ID로 재고를 조회한다")
        void findsStockByPricePolicyId() {
            when(valueOperations.get("pricePolicy:100:stock")).thenReturn("50");

            int result = adapter.findByPricePolicyId(100L);

            assertThat(result).isEqualTo(50);
        }

        @Test
        @DisplayName("저장된 값이 숫자가 아니면 ParsingIntegerException을 던진다")
        void throwsWhenValueNotNumeric() {
            when(valueOperations.get("pricePolicy:100:stock")).thenReturn("invalid");

            assertThatThrownBy(() -> adapter.findByPricePolicyId(100L))
                    .isInstanceOf(ParsingIntegerException.class);
        }
    }

    @Nested
    @DisplayName("findByPricePolicyIds")
    class FindByPricePolicyIds {

        @Test
        @DisplayName("여러 가격정책 ID의 재고를 한 번에 조회한다")
        void findsMultipleStocks() {
            List<String> keys = List.of("pricePolicy:100:stock", "pricePolicy:200:stock");
            when(valueOperations.multiGet(keys)).thenReturn(List.of("50", "30"));

            Map<Long, Integer> result = adapter.findByPricePolicyIds(List.of(100L, 200L));

            assertThat(result).containsEntry(100L, 50).containsEntry(200L, 30);
        }

        @Test
        @DisplayName("일부 키의 값이 없으면 해당 키는 null로 매핑한다")
        void mapsMissingValuesAsNull() {
            List<String> keys = List.of("pricePolicy:100:stock", "pricePolicy:200:stock");
            when(valueOperations.multiGet(keys)).thenReturn(Arrays.asList("50", null));

            Map<Long, Integer> result = adapter.findByPricePolicyIds(List.of(100L, 200L));

            assertThat(result).containsEntry(100L, 50);
            assertThat(result.get(200L)).isNull();
        }

        @Test
        @DisplayName("multiGet 결과가 null이면 빈 맵을 반환한다")
        void returnsEmptyWhenMultiGetNull() {
            List<String> keys = List.of("pricePolicy:100:stock");
            when(valueOperations.multiGet(keys)).thenReturn(null);

            Map<Long, Integer> result = adapter.findByPricePolicyIds(List.of(100L));

            assertThat(result).isEmpty();
        }
    }
}
