package com.personal.marketnote.fulfillment.domain.vendor.warehousing;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentWarehousingInspecDetailQuery {
    private String customerCode;
    private String accessToken;
    private String slipNo;
    private String whCd;

    public static FulfillmentWarehousingInspecDetailQuery of(
            String customerCode,
            String accessToken,
            String slipNo,
            String whCd
    ) {
        FulfillmentWarehousingInspecDetailQuery query = FulfillmentWarehousingInspecDetailQuery.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .slipNo(slipNo)
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
        if (FormatValidator.hasNoValue(slipNo)) {
            throw new FulfillmentQueryParameterNoValueException("slipNo");
        }
        if (FormatValidator.hasNoValue(whCd)) {
            throw new FulfillmentQueryParameterNoValueException("whCd");
        }
    }
}
