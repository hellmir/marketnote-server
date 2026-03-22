package com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoDeliveryOutOrdGoodsDetailQuery {
    private String customerCode;
    private String accessToken;
    private String outOrdSlipNo;

    public static FasstoDeliveryOutOrdGoodsDetailQuery of(
            String customerCode,
            String accessToken,
            String outOrdSlipNo
    ) {
        FasstoDeliveryOutOrdGoodsDetailQuery query = FasstoDeliveryOutOrdGoodsDetailQuery.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .outOrdSlipNo(outOrdSlipNo)
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
        if (FormatValidator.hasNoValue(outOrdSlipNo)) {
            throw new FasstoQueryParameterNoValueException("outOrdSlipNo");
        }
    }
}
