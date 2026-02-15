package com.personal.marketnote.commerce.domain.inventory;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidQuantityException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.QuantityNoValueException;
import com.personal.marketnote.common.utility.FormatConverter;
import com.personal.marketnote.common.utility.FormatValidator;

import java.util.Objects;
import java.util.regex.Pattern;

import static com.personal.marketnote.common.utility.RegularExpressionConstant.ZERO_OR_POSITIVE_INTEGER_PATTERN;

public class Stock {
    private static final String STOCK_NO_VALUE_EXCEPTION = "재고 수량은 필수값입니다.";
    private static final String STOCK_INVALID_EXCEPTION = "재고 수량은 0 또는 양의 정수(0 이상의 숫자값)여야 합니다. 전송된 수량: %s";

    private final int stock;

    private Stock(int stock) {
        this.stock = stock;
    }

    public static Stock of(String stock) {
        validate(stock);
        return new Stock(FormatConverter.parseToInteger(stock));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (FormatValidator.hasNoValue(o) || getClass() != o.getClass()) {
            return false;
        }

        Stock stock = (Stock) o;

        return this.stock == stock.getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(stock);
    }

    private static void validate(String stock) {
        checkStockIsNotBlank(stock);
        checkStockPattern(stock);
    }

    private static void checkStockIsNotBlank(String stock) {
        if (FormatValidator.hasNoValue(stock)) {
            throw new QuantityNoValueException(STOCK_NO_VALUE_EXCEPTION);
        }
    }

    private static void checkStockPattern(String stock) {
        if (!FormatValidator.isValid(stock, Pattern.compile(ZERO_OR_POSITIVE_INTEGER_PATTERN))) {
            throw new InvalidQuantityException(String.format(STOCK_INVALID_EXCEPTION, stock));
        }
    }

    int getValue() {
        return stock;
    }

    public Integer reduce(int stock) {
        return this.stock - stock;
    }
}
