package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.returndelivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentReturnGodDetailQuery {
    private String customerCode;
    private String accessToken;
    private String startDate;
    private String endDate;
    private String rtnSlipNoList;
    private String whCd;

    public static FulfillmentReturnGodDetailQuery of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String rtnSlipNoList,
            String whCd
    ) {
        FulfillmentReturnGodDetailQuery query = FulfillmentReturnGodDetailQuery.builder()
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
            throw new FulfillmentQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FulfillmentQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(rtnSlipNoList)) {
            if (FormatValidator.hasNoValue(startDate)) {
                throw new FulfillmentQueryParameterNoValueException("startDate", "rtnSlipNoList is not provided");
            }
            if (FormatValidator.hasNoValue(endDate)) {
                throw new FulfillmentQueryParameterNoValueException("endDate", "rtnSlipNoList is not provided");
            }
        }
    }
}
