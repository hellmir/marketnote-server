package com.personal.marketnote.fulfillment.domain.vendor.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentDeliveryQuery {
    private String customerCode;
    private String accessToken;
    private String startDate;
    private String endDate;
    private String status;
    private String outDiv;
    private String ordNo;

    public static FulfillmentDeliveryQuery of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String status,
            String outDiv
    ) {
        return FulfillmentDeliveryQuery.of(customerCode, accessToken, startDate, endDate, status, outDiv, null);
    }

    public static FulfillmentDeliveryQuery of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String status,
            String outDiv,
            String ordNo
    ) {
        FulfillmentDeliveryQuery query = FulfillmentDeliveryQuery.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .startDate(startDate)
                .endDate(endDate)
                .status(status)
                .outDiv(outDiv)
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
        if (FormatValidator.hasNoValue(status)) {
            throw new FulfillmentQueryParameterNoValueException("status");
        }
        if (FormatValidator.hasNoValue(outDiv)) {
            throw new FulfillmentQueryParameterNoValueException("outDiv");
        }
    }
}
