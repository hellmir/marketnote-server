package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentDeliveryDetailQuery {
    private String customerCode;
    private String accessToken;
    private String slipNo;
    private String ordNo;

    public static FulfillmentDeliveryDetailQuery of(
            String customerCode,
            String accessToken,
            String slipNo
    ) {
        return FulfillmentDeliveryDetailQuery.of(customerCode, accessToken, slipNo, null);
    }

    public static FulfillmentDeliveryDetailQuery of(
            String customerCode,
            String accessToken,
            String slipNo,
            String ordNo
    ) {
        FulfillmentDeliveryDetailQuery query = FulfillmentDeliveryDetailQuery.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .slipNo(slipNo)
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
        if (FormatValidator.hasNoValue(slipNo)) {
            throw new FulfillmentQueryParameterNoValueException("slipNo");
        }
    }
}
