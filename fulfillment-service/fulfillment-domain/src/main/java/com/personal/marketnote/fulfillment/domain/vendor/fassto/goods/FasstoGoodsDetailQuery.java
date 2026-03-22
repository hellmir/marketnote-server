package com.personal.marketnote.fulfillment.domain.vendor.fassto.goods;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoGoodsDetailQuery {
    private String customerCode;
    private String accessToken;
    private String godNm;

    public static FasstoGoodsDetailQuery of(
            String customerCode,
            String accessToken,
            String godNm
    ) {
        FasstoGoodsDetailQuery query = FasstoGoodsDetailQuery.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .godNm(godNm)
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
        if (FormatValidator.hasNoValue(godNm)) {
            throw new FasstoQueryParameterNoValueException("godNm");
        }
    }
}
