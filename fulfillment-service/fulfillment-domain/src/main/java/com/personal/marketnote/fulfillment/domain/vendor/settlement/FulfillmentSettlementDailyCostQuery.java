package com.personal.marketnote.fulfillment.domain.vendor.settlement;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentSettlementDailyCostQuery {
    private String yearMonth;
    private String whCd;
    private String customerCode;
    private String accessToken;

    public static FulfillmentSettlementDailyCostQuery of(
            String yearMonth,
            String whCd,
            String customerCode,
            String accessToken
    ) {
        FulfillmentSettlementDailyCostQuery query = FulfillmentSettlementDailyCostQuery.builder()
                .yearMonth(yearMonth)
                .whCd(whCd)
                .customerCode(customerCode)
                .accessToken(accessToken)
                .build();
        query.validate();
        return query;
    }

    private void validate() {
        if (FormatValidator.hasNoValue(yearMonth)) {
            throw new FulfillmentQueryParameterNoValueException("yearMonth");
        }
        if (FormatValidator.hasNoValue(whCd)) {
            throw new FulfillmentQueryParameterNoValueException("whCd");
        }
        if (FormatValidator.hasNoValue(customerCode)) {
            throw new FulfillmentQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FulfillmentQueryParameterNoValueException("accessToken");
        }
    }
}
