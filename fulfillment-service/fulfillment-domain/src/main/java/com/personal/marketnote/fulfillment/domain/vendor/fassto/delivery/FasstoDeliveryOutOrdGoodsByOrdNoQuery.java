package com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoDeliveryOutOrdGoodsByOrdNoQuery {
    private String customerCode;
    private String accessToken;
    private String startDate;
    private String endDate;
    private String ordNo;

    public static FasstoDeliveryOutOrdGoodsByOrdNoQuery of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String ordNo
    ) {
        FasstoDeliveryOutOrdGoodsByOrdNoQuery query = FasstoDeliveryOutOrdGoodsByOrdNoQuery.builder()
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
            throw new IllegalArgumentException("customerCode is required.");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new IllegalArgumentException("accessToken is required.");
        }
        if (FormatValidator.hasNoValue(startDate)) {
            throw new IllegalArgumentException("startDate is required.");
        }
        if (FormatValidator.hasNoValue(endDate)) {
            throw new IllegalArgumentException("endDate is required.");
        }
    }
}
