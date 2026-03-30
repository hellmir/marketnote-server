package com.personal.marketnote.fulfillment.domain.vendor.warehousing;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentWarehousingAbnormalQuery {
    private String customerCode;
    private String accessToken;
    private String whCd;
    private String slipNo;

    public static FulfillmentWarehousingAbnormalQuery of(
            String customerCode,
            String accessToken,
            String whCd,
            String slipNo
    ) {
        FulfillmentWarehousingAbnormalQuery query = FulfillmentWarehousingAbnormalQuery.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .whCd(whCd)
                .slipNo(slipNo)
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
        if (FormatValidator.hasNoValue(whCd)) {
            throw new FulfillmentQueryParameterNoValueException("whCd");
        }
        if (FormatValidator.hasNoValue(slipNo)) {
            throw new FulfillmentQueryParameterNoValueException("slipNo");
        }
    }
}
