package com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoDeliveryStatusQuery {
    private String customerCode;
    private String accessToken;
    private String startDate;
    private String endDate;
    private String outDiv;

    public static FasstoDeliveryStatusQuery of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String outDiv
    ) {
        FasstoDeliveryStatusQuery query = FasstoDeliveryStatusQuery.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .startDate(startDate)
                .endDate(endDate)
                .outDiv(outDiv)
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
        if (FormatValidator.hasNoValue(outDiv)) {
            throw new FasstoQueryParameterNoValueException("outDiv");
        }
    }
}
