package com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoDeliveryQuery {
    private String customerCode;
    private String accessToken;
    private String startDate;
    private String endDate;
    private String status;
    private String outDiv;
    private String ordNo;

    public static FasstoDeliveryQuery of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String status,
            String outDiv
    ) {
        return FasstoDeliveryQuery.of(customerCode, accessToken, startDate, endDate, status, outDiv, null);
    }

    public static FasstoDeliveryQuery of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String status,
            String outDiv,
            String ordNo
    ) {
        FasstoDeliveryQuery query = FasstoDeliveryQuery.builder()
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
        if (FormatValidator.hasNoValue(status)) {
            throw new FasstoQueryParameterNoValueException("status");
        }
        if (FormatValidator.hasNoValue(outDiv)) {
            throw new FasstoQueryParameterNoValueException("outDiv");
        }
    }
}
