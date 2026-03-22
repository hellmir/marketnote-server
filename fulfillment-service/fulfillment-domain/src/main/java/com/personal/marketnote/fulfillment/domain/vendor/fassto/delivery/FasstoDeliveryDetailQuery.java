package com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoDeliveryDetailQuery {
    private String customerCode;
    private String accessToken;
    private String slipNo;
    private String ordNo;

    public static FasstoDeliveryDetailQuery of(
            String customerCode,
            String accessToken,
            String slipNo
    ) {
        return FasstoDeliveryDetailQuery.of(customerCode, accessToken, slipNo, null);
    }

    public static FasstoDeliveryDetailQuery of(
            String customerCode,
            String accessToken,
            String slipNo,
            String ordNo
    ) {
        FasstoDeliveryDetailQuery query = FasstoDeliveryDetailQuery.builder()
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
            throw new FasstoQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FasstoQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(slipNo)) {
            throw new FasstoQueryParameterNoValueException("slipNo");
        }
    }
}
