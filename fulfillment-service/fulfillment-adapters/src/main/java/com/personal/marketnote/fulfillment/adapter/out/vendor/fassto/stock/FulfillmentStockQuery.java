package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.stock;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentStockQuery {
    private String customerCode;
    private String accessToken;
    private String outOfStockYn;
    private String whCd;

    public static FulfillmentStockQuery of(
            String customerCode,
            String accessToken,
            String outOfStockYn
    ) {
        return FulfillmentStockQuery.of(customerCode, accessToken, outOfStockYn, null);
    }

    public static FulfillmentStockQuery of(
            String customerCode,
            String accessToken,
            String outOfStockYn,
            String whCd
    ) {
        FulfillmentStockQuery query = FulfillmentStockQuery.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .outOfStockYn(outOfStockYn)
                .whCd(whCd)
                .build();
        query.validate();
        return query;
    }

    private void validate() {
        if (FormatValidator.hasNoValue(customerCode)) {
            throw new FulfillmentQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FulfillmentQueryParameterNoValueException("accessToken");
        }
    }
}
