package com.personal.marketnote.commerce.mapper;

import com.personal.marketnote.commerce.domain.order.OrderProductCreateState;
import com.personal.marketnote.commerce.port.in.command.order.OrderProductItemCommand;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCommandToStateMapperTest {

    @Test
    @DisplayName("OrderCommandToStateMapper가 OrderProductItemCommand.productId를 OrderProductCreateState.productId로 매핑한다")
    void shouldMapProductIdFromCommandToCreateState() {
        // given
        OrderProductItemCommand command = OrderProductItemCommand.builder()
                .productId(42L)
                .sellerId(10L)
                .pricePolicyId(100L)
                .quantity(1)
                .unitAmount(50000L)
                .build();

        // when
        List<OrderProductCreateState> states = OrderCommandToStateMapper.mapToOrderProductStates(
                List.of(command), Map.of()
        );

        // then
        assertThat(states).hasSize(1);
        assertThat(states.get(0).getProductId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("productInfoMap에 productKey가 존재하면 OrderProductCreateState.productKey에 반영된다")
    void shouldMapProductKeyFromProductInfoMap() {
        // given
        UUID productKey = UUID.fromString("01900000-0000-7000-8000-000000000042");
        OrderProductItemCommand command = OrderProductItemCommand.builder()
                .productId(1L)
                .sellerId(10L)
                .pricePolicyId(100L)
                .quantity(1)
                .unitAmount(50000L)
                .build();
        ProductInfoResult productInfo = new ProductInfoResult(
                1L, productKey, 10L, "상품", "브랜드", 50000L, null, 100L, List.of()
        );

        // when
        List<OrderProductCreateState> states = OrderCommandToStateMapper.mapToOrderProductStates(
                List.of(command), Map.of(100L, productInfo)
        );

        // then
        assertThat(states.get(0).getProductKey()).isEqualTo(productKey);
    }

    @Test
    @DisplayName("productInfoMap이 비어있으면 OrderProductCreateState.productKey는 null이다")
    void shouldMapProductKeyAsNullWhenProductInfoMapIsEmpty() {
        // given
        OrderProductItemCommand command = OrderProductItemCommand.builder()
                .productId(1L)
                .sellerId(10L)
                .pricePolicyId(100L)
                .quantity(1)
                .unitAmount(50000L)
                .build();

        // when
        List<OrderProductCreateState> states = OrderCommandToStateMapper.mapToOrderProductStates(
                List.of(command), Map.of()
        );

        // then
        assertThat(states.get(0).getProductKey()).isNull();
    }
}
