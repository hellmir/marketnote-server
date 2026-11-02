package com.personal.marketnote.commerce.domain.inventory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FulfillmentMapperTest {

    @Test
    @DisplayName("CreateState로부터 Stock VO를 유지한 FulfillmentMapper를 생성한다")
    void shouldCreateFromCreateStateWithStockVo() {
        Stock stock = Stock.of("50");
        FulfillmentMapperCreateState state = FulfillmentMapperCreateState.builder()
                .productId(1L)
                .wmsKey("WMS_KEY_1")
                .wmsProductKey("WMS_PRODUCT_KEY_1")
                .stock(stock)
                .build();

        FulfillmentMapper mapper = FulfillmentMapper.from(state);

        assertThat(mapper.getProductId()).isEqualTo(1L);
        assertThat(mapper.getWmsKey()).isEqualTo("WMS_KEY_1");
        assertThat(mapper.getWmsProductKey()).isEqualTo("WMS_PRODUCT_KEY_1");
        assertThat(mapper.getStock()).isEqualTo(stock);
        assertThat(mapper.getStock().getValue()).isEqualTo(50);
    }

    @Test
    @DisplayName("SnapshotState로부터 Stock VO를 유지한 FulfillmentMapper를 복원한다")
    void shouldCreateFromSnapshotStateWithStockVo() {
        Stock stock = Stock.of("100");
        FulfillmentMapperSnapshotState state = FulfillmentMapperSnapshotState.builder()
                .productId(2L)
                .wmsKey("WMS_KEY_2")
                .wmsProductKey("WMS_PRODUCT_KEY_2")
                .stock(stock)
                .build();

        FulfillmentMapper mapper = FulfillmentMapper.from(state);

        assertThat(mapper.getProductId()).isEqualTo(2L);
        assertThat(mapper.getWmsKey()).isEqualTo("WMS_KEY_2");
        assertThat(mapper.getWmsProductKey()).isEqualTo("WMS_PRODUCT_KEY_2");
        assertThat(mapper.getStock()).isEqualTo(stock);
        assertThat(mapper.getStock().getValue()).isEqualTo(100);
    }

    @Test
    @DisplayName("0 재고 Stock VO로 FulfillmentMapper를 생성한다")
    void shouldCreateWithZeroStock() {
        Stock stock = Stock.of("0");
        FulfillmentMapperCreateState state = FulfillmentMapperCreateState.builder()
                .productId(3L)
                .wmsKey("WMS_KEY_3")
                .wmsProductKey("WMS_PRODUCT_KEY_3")
                .stock(stock)
                .build();

        FulfillmentMapper mapper = FulfillmentMapper.from(state);

        assertThat(mapper.getStock().getValue()).isEqualTo(0);
    }
}
