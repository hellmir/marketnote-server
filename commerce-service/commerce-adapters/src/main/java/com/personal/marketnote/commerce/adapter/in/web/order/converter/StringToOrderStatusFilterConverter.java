package com.personal.marketnote.commerce.adapter.in.web.order.converter;

import com.personal.marketnote.commerce.domain.order.OrderStatusFilter;
import com.personal.marketnote.common.utility.FormatValidator;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StringToOrderStatusFilterConverter implements Converter<String, OrderStatusFilter> {

    private static final Map<String, OrderStatusFilter> LEGACY_MAPPINGS = Map.of(
            "CANCEL_EXCHANGE_REFUND", OrderStatusFilter.CANCEL_RETURN
    );

    @Override
    public OrderStatusFilter convert(String source) {
        OrderStatusFilter legacy = LEGACY_MAPPINGS.get(source);
        if (FormatValidator.hasValue(legacy)) {
            return legacy;
        }
        return OrderStatusFilter.valueOf(source);
    }
}
