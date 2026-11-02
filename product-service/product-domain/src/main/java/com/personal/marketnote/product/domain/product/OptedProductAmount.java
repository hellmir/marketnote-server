package com.personal.marketnote.product.domain.product;

import com.personal.marketnote.common.domain.money.Money;
import lombok.Getter;

@Getter
public class OptedProductAmount {
    private Money totalOptionPrice = Money.zero();
    private Money totalOptionPoint = Money.zero();

    public void addAmount(Money price, Money point) {
        totalOptionPrice = totalOptionPrice.add(price);
        totalOptionPoint = totalOptionPoint.add(point);
    }
}
