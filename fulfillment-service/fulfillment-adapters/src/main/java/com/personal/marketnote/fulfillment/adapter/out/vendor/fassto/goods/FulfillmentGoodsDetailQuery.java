package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.goods;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentGoodsDetailQuery {
    private String customerCode;
    private String accessToken;
    private String godNm;

    public static FulfillmentGoodsDetailQuery of(
            String customerCode,
            String accessToken,
            String godNm
    ) {
        FulfillmentGoodsDetailQuery query = FulfillmentGoodsDetailQuery.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .godNm(godNm)
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
        if (FormatValidator.hasNoValue(godNm)) {
            throw new FulfillmentQueryParameterNoValueException("godNm");
        }
    }
}
