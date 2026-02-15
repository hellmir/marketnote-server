package com.personal.marketnote.commerce.domain.inventory;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import static org.hibernate.type.descriptor.java.IntegerJavaType.ZERO;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Inventory {
    private Long productId;
    private Long pricePolicyId;
    private Stock stock;
    private Long version;

    public static Inventory of(Long productId, Long pricePolicyId) {
        return Inventory.builder()
                .productId(productId)
                .pricePolicyId(pricePolicyId)
                .stock(Stock.of(
                        ZERO.toString()
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
                .build();
    }

    public void reduce(int stockToReduce) {
        Integer reducedStock = stock.reduce(stockToReduce);
        stock = Stock.of(
                String.valueOf(reducedStock)
        );
    }

    public Integer getStockValue() {
        return stock.getValue();
    }

    public boolean isMe(Long pricePolicyId) {
        return FormatValidator.equals(pricePolicyId, pricePolicyId);
    }
}
