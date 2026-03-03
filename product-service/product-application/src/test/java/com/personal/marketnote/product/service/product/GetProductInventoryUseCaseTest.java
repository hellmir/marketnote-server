package com.personal.marketnote.product.service.product;

import com.personal.marketnote.product.port.out.inventory.FindCacheStockPort;
import com.personal.marketnote.product.port.out.inventory.FindStockPort;
import com.personal.marketnote.product.port.out.result.GetInventoryResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProductInventoryUseCaseTest {
    @Mock
    private FindCacheStockPort findCacheStockPort;
    @Mock
    private FindStockPort findStockPort;

    @InjectMocks
    private GetProductInventoryService getProductInventoryService;

    @Test
    @DisplayName("상품 재고 목록 조회 시 캐시 데이터에 모든 재고가 존재하면 해당 결과를 반환한다")
    void getProductStocks_cacheHit_returnsCache() {
        List<Long> pricePolicyIds = List.of(1L, 2L);
        Map<Long, Integer> cacheInventories = Map.of(1L, 5, 2L, 0);

        when(findCacheStockPort.findByPricePolicyIds(pricePolicyIds)).thenReturn(cacheInventories);

        Map<Long, Integer> result = getProductInventoryService.getProductStocks(pricePolicyIds);

        assertThat(result).isSameAs(cacheInventories);
        verify(findCacheStockPort).findByPricePolicyIds(pricePolicyIds);
        verifyNoInteractions(findStockPort);
    }

    @Test
    @DisplayName("상품 재고 목록 조회 시 캐시에 데이터에 존재하지 않는 재고는 커머스 서비스 요청을 통해 조회한다")
    void getProductStocks_cacheMiss_fetchesFromRest() {
        List<Long> pricePolicyIds = List.of(1L, 2L, 3L);
        Map<Long, Integer> cacheInventories = new HashMap<>();
        cacheInventories.put(1L, 3);

        when(findCacheStockPort.findByPricePolicyIds(pricePolicyIds)).thenReturn(cacheInventories);
        when(findStockPort.findByPricePolicyIds(List.of(2L, 3L)))
                .thenReturn(Set.of(
                        GetInventoryResult.of(2L, 7),
                        GetInventoryResult.of(3L, 0)
                ));

        Map<Long, Integer> result = getProductInventoryService.getProductStocks(pricePolicyIds);

        assertThat(result.get(1L)).isEqualTo(3);
        assertThat(result.get(2L)).isEqualTo(7);
        assertThat(result.get(3L)).isEqualTo(0);

        ArgumentCaptor<List<Long>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(findStockPort).findByPricePolicyIds(idsCaptor.capture());
        assertThat(idsCaptor.getValue()).containsExactly(2L, 3L);
    }

    @Test
    @DisplayName("상품 재고 목록 조회 시 커머스 서비스 조회 결과가 없는 경우 캐시 데이터 결과만 반환한다")
    void getProductStocks_restEmpty_returnsCacheOnly() {
        List<Long> pricePolicyIds = List.of(1L, 2L);
        Map<Long, Integer> cacheInventories = new HashMap<>();
        cacheInventories.put(1L, 4);

        when(findCacheStockPort.findByPricePolicyIds(pricePolicyIds)).thenReturn(cacheInventories);
        when(findStockPort.findByPricePolicyIds(List.of(2L))).thenReturn(Set.of());

        Map<Long, Integer> result = getProductInventoryService.getProductStocks(pricePolicyIds);

        assertThat(result).hasSize(1);
        assertThat(result.get(1L)).isEqualTo(4);
        assertThat(result.get(2L)).isNull();
    }

    @Test
    @DisplayName("상품 재고 목록 조회 시 요청 목록이 비어 있으면 빈 결과를 반환한다")
    void getProductStocks_emptyRequest_returnsEmpty() {
        List<Long> pricePolicyIds = List.of();
        Map<Long, Integer> cacheInventories = Map.of();

        when(findCacheStockPort.findByPricePolicyIds(pricePolicyIds)).thenReturn(cacheInventories);

        Map<Long, Integer> result = getProductInventoryService.getProductStocks(pricePolicyIds);

        assertThat(result).isEmpty();
        verify(findCacheStockPort).findByPricePolicyIds(pricePolicyIds);
        verifyNoInteractions(findStockPort);
    }

    @Test
    @DisplayName("상품 재고 목록 조회 시 캐시 데이터 조회에 실패하면 예외를 전파한다")
    void getProductStocks_cacheFail_propagates() {
        List<Long> pricePolicyIds = List.of(1L);
        RuntimeException exception = new RuntimeException("cache fail");

        when(findCacheStockPort.findByPricePolicyIds(pricePolicyIds)).thenThrow(exception);

        assertThatThrownBy(() -> getProductInventoryService.getProductStocks(pricePolicyIds))
                .isSameAs(exception);

        verifyNoInteractions(findStockPort);
    }

    @Test
    @DisplayName("상품 재고 목록 조회 시 커머스 서비스 데이터 조회에 실패하면 예외를 전파한다")
    void getProductStocks_restFail_propagates() {
        List<Long> pricePolicyIds = List.of(1L, 2L);
        Map<Long, Integer> cacheInventories = new HashMap<>();
        cacheInventories.put(1L, 1);

        when(findCacheStockPort.findByPricePolicyIds(pricePolicyIds)).thenReturn(cacheInventories);
        when(findStockPort.findByPricePolicyIds(List.of(2L))).thenThrow(new RuntimeException("rest fail"));

        assertThatThrownBy(() -> getProductInventoryService.getProductStocks(pricePolicyIds))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("rest fail");
    }
}
