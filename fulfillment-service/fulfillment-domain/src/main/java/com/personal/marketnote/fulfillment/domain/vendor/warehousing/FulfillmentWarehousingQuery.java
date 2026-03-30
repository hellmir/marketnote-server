package com.personal.marketnote.fulfillment.domain.vendor.warehousing;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentWarehousingQuery {
    private String customerCode;
    private String accessToken;
    private String startDate;
    private String endDate;
    private String inWay;
    private String ordNo;
    private String wrkStat;

    public static FulfillmentWarehousingQuery of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate
    ) {
        return FulfillmentWarehousingQuery.of(customerCode, accessToken, startDate, endDate, null, null, null);
    }

    public static FulfillmentWarehousingQuery of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String inWay,
            String ordNo,
            String wrkStat
    ) {
        FulfillmentWarehousingQuery query = FulfillmentWarehousingQuery.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .startDate(startDate)
                .endDate(endDate)
                .inWay(inWay)
                .ordNo(ordNo)
                .wrkStat(wrkStat)
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
        if (FormatValidator.hasNoValue(startDate)) {
            throw new FulfillmentQueryParameterNoValueException("startDate");
        }
        if (FormatValidator.hasNoValue(endDate)) {
            throw new FulfillmentQueryParameterNoValueException("endDate");
        }
    }
}
