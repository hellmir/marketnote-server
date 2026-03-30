package com.personal.marketnote.fulfillment.domain.vendor.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentDeliveryOutOrdGoodsDetailQuery {
    private String customerCode;
    private String accessToken;
    private String outOrdSlipNo;

    public static FulfillmentDeliveryOutOrdGoodsDetailQuery of(
            String customerCode,
            String accessToken,
            String outOrdSlipNo
    ) {
        FulfillmentDeliveryOutOrdGoodsDetailQuery query = FulfillmentDeliveryOutOrdGoodsDetailQuery.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .outOrdSlipNo(outOrdSlipNo)
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
        if (FormatValidator.hasNoValue(outOrdSlipNo)) {
            throw new FulfillmentQueryParameterNoValueException("outOrdSlipNo");
        }
    }
}
