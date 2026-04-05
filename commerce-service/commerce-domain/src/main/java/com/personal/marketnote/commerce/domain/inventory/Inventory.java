package com.personal.marketnote.commerce.domain.inventory;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidQuantityException;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Inventory {
    private Long productId;
    private Long pricePolicyId;
    private Stock stock;
    private Long version;
    private int reserved;

    public static Inventory of(Long productId, Long pricePolicyId) {
        return Inventory.builder()
                .productId(productId)
                .pricePolicyId(pricePolicyId)
                .stock(Stock.of(
                        "0"
                ))
                .build();
    }

    public static Inventory of(Long productId, Long pricePolicyId, Integer stock) {
        return Inventory.builder()
                .productId(productId)
                .pricePolicyId(pricePolicyId)
                .stock(Stock.of(
                        String.valueOf(stock)
                ))
                .build();
    }

    public static Inventory of(Long productId, Long pricePolicyId, Integer stock, Long version) {
        return Inventory.builder()
                .productId(productId)
                .pricePolicyId(pricePolicyId)
                .stock(Stock.of(
                        String.valueOf(stock)
                ))
                .version(version)
                .reserved(0)
                .build();
    }

    public static Inventory from(InventorySnapshotState state) {
        return Inventory.builder()
                .productId(state.getProductId())
                .pricePolicyId(state.getPricePolicyId())
                .stock(Stock.of(
                        String.valueOf(state.getStock())
                ))
                .version(state.getVersion())
                .reserved(state.getReserved())
                .build();
    }

    public void reduce(int stockToReduce) {
        Integer reducedStock = stock.reduce(stockToReduce);
        stock = Stock.of(
                String.valueOf(reducedStock)
        );
    }

    public void restore(int quantity) {
        Integer increasedStock = stock.increase(quantity);
        stock = Stock.of(
                String.valueOf(increasedStock)
        );
    }

    public Integer getStockValue() {
        return stock.getValue();
    }

    public boolean isMe(Long targetPricePolicyId) {
        return FormatValidator.equals(this.pricePolicyId, targetPricePolicyId);
    }

    public int availableStock() {
        return stock.getValue() - reserved;
    }

    public void reserve(int quantity) {
        validatePositiveQuantity(quantity);
        int available = availableStock();
        if (available < quantity) {
            throw new InsufficientAvailableStockException(available, quantity);
        }
        this.reserved = Math.addExact(this.reserved, quantity);
    }

    public void confirmReservation(int quantity) {
        validatePositiveQuantity(quantity);
        if (quantity > this.reserved) {
            throw new InvalidInventoryReservationQuantityException(this.reserved, quantity);
        }
        this.reserved -= quantity;
        reduce(quantity);
    }

    public void releaseReservation(int quantity) {
        validatePositiveQuantity(quantity);
        this.reserved = Math.max(0, this.reserved - quantity);
    }

    private void validatePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException(
                    String.format("수량은 1 이상이어야 합니다. 전송된 수량: %d", quantity)
            );
        }
    }

    public void validateIsSufficient(int orderQuantity) {
        int available = availableStock();
        if (available < orderQuantity) {
            throw new InsufficientAvailableStockException(available, orderQuantity);
        }
    }
}
