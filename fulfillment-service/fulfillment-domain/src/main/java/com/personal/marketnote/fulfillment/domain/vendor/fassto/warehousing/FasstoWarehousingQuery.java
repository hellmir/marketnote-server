package com.personal.marketnote.fulfillment.domain.vendor.fassto.warehousing;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoWarehousingQuery {
    private String customerCode;
    private String accessToken;
    private String startDate;
    private String endDate;
    private String inWay;
    private String ordNo;
    private String wrkStat;

    public static FasstoWarehousingQuery of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate
    ) {
        return FasstoWarehousingQuery.of(customerCode, accessToken, startDate, endDate, null, null, null);
    }

    public static FasstoWarehousingQuery of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String inWay,
            String ordNo,
            String wrkStat
    ) {
        FasstoWarehousingQuery query = FasstoWarehousingQuery.builder()
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
            throw new FasstoQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FasstoQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(startDate)) {
            throw new FasstoQueryParameterNoValueException("startDate");
        }
        if (FormatValidator.hasNoValue(endDate)) {
            throw new FasstoQueryParameterNoValueException("endDate");
        }
    }
}
