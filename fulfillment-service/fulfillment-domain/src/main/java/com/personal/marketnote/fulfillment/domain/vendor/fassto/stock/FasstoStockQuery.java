package com.personal.marketnote.fulfillment.domain.vendor.fassto.stock;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoStockQuery {
    private String customerCode;
    private String accessToken;
    private String outOfStockYn;
    private String whCd;

    public static FasstoStockQuery of(
            String customerCode,
            String accessToken,
            String outOfStockYn
    ) {
        return FasstoStockQuery.of(customerCode, accessToken, outOfStockYn, null);
    }

    public static FasstoStockQuery of(
            String customerCode,
            String accessToken,
            String outOfStockYn,
            String whCd
    ) {
        FasstoStockQuery query = FasstoStockQuery.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .outOfStockYn(outOfStockYn)
                .whCd(whCd)
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
    }
}
