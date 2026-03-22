package com.personal.marketnote.fulfillment.domain.vendor.fassto.returndelivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoReturnGodDetailQuery {
    private String customerCode;
    private String accessToken;
    private String startDate;
    private String endDate;
    private String rtnSlipNoList;
    private String whCd;

    public static FasstoReturnGodDetailQuery of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String rtnSlipNoList,
            String whCd
    ) {
        FasstoReturnGodDetailQuery query = FasstoReturnGodDetailQuery.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .startDate(startDate)
                .endDate(endDate)
                .rtnSlipNoList(rtnSlipNoList)
                .whCd(whCd)
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
        if (FormatValidator.hasNoValue(rtnSlipNoList)) {
            if (FormatValidator.hasNoValue(startDate)) {
                throw new FasstoQueryParameterNoValueException("startDate", "rtnSlipNoList is not provided");
            }
            if (FormatValidator.hasNoValue(endDate)) {
                throw new FasstoQueryParameterNoValueException("endDate", "rtnSlipNoList is not provided");
            }
        }
    }
}
