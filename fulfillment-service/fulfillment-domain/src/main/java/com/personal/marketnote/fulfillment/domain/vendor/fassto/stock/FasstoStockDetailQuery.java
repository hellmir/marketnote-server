package com.personal.marketnote.fulfillment.domain.vendor.fassto.stock;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoStockDetailQuery {
    private String customerCode;
    private String accessToken;
    private String cstGodCd;
    private String outOfStockYn;

    public static FasstoStockDetailQuery of(
            String customerCode,
            String accessToken,
            String cstGodCd,
            String outOfStockYn
    ) {
        FasstoStockDetailQuery query = FasstoStockDetailQuery.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .cstGodCd(cstGodCd)
                .outOfStockYn(outOfStockYn)
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
        if (FormatValidator.hasNoValue(cstGodCd)) {
            throw new FasstoQueryParameterNoValueException("cstGodCd");
        }
    }
}
