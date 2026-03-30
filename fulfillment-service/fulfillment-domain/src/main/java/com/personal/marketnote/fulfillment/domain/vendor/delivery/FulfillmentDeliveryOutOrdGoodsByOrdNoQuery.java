package com.personal.marketnote.fulfillment.domain.vendor.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentDeliveryOutOrdGoodsByOrdNoQuery {
    private String customerCode;
    private String accessToken;
    private String startDate;
    private String endDate;
    private String ordNo;

    public static FulfillmentDeliveryOutOrdGoodsByOrdNoQuery of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String ordNo
    ) {
        FulfillmentDeliveryOutOrdGoodsByOrdNoQuery query = FulfillmentDeliveryOutOrdGoodsByOrdNoQuery.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .startDate(startDate)
                .endDate(endDate)
                .ordNo(ordNo)
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
