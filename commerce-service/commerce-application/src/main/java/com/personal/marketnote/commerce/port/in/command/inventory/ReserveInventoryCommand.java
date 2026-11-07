package com.personal.marketnote.commerce.port.in.command.inventory;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import lombok.Builder;

import java.util.List;

@Builder
public record ReserveInventoryCommand(
        Long orderId,
        List<OrderProductItem> orderProducts
) {
    @Builder
    public record OrderProductItem(
            Long pricePolicyId,
            int quantity
    ) {
        public static OrderProductItem from(OrderProduct orderProduct) {
            return new OrderProductItem(
                    orderProduct.getPricePolicyId(),
                    orderProduct.getQuantity().getValue()
            );
        }
    }

    public static ReserveInventoryCommand from(Order order) {
        List<OrderProductItem> items = order.getOrderProducts().stream()
                .map(OrderProductItem::from)
                .toList();
        return new ReserveInventoryCommand(order.getId(), items);
    }
}
