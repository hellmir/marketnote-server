package com.personal.marketnote.commerce.mapper;

import com.personal.marketnote.commerce.domain.order.OrderProductCreateState;
import com.personal.marketnote.commerce.port.in.command.order.OrderProductItemCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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
}
