package com.personal.marketnote.fulfillment.domain.vendor.stock;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentStockDetailQuery {
    private String customerCode;
    private String accessToken;
    private String cstGodCd;
    private String outOfStockYn;

    public static FulfillmentStockDetailQuery of(
            String customerCode,
            String accessToken,
            String cstGodCd,
            String outOfStockYn
    ) {
        FulfillmentStockDetailQuery query = FulfillmentStockDetailQuery.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .cstGodCd(cstGodCd)
                .outOfStockYn(outOfStockYn)
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
        if (FormatValidator.hasNoValue(cstGodCd)) {
            throw new FulfillmentQueryParameterNoValueException("cstGodCd");
        }
    }
}
