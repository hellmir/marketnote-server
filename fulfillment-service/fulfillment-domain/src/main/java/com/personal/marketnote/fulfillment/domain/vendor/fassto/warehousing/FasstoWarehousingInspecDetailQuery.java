package com.personal.marketnote.fulfillment.domain.vendor.fassto.warehousing;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoWarehousingInspecDetailQuery {
    private String customerCode;
    private String accessToken;
    private String slipNo;
    private String whCd;

    public static FasstoWarehousingInspecDetailQuery of(
            String customerCode,
            String accessToken,
            String slipNo,
            String whCd
    ) {
        FasstoWarehousingInspecDetailQuery query = FasstoWarehousingInspecDetailQuery.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .slipNo(slipNo)
                .whCd(whCd)
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
        if (FormatValidator.hasNoValue(slipNo)) {
            throw new IllegalArgumentException("slipNo is required.");
        }
        if (FormatValidator.hasNoValue(whCd)) {
            throw new IllegalArgumentException("whCd is required.");
        }
    }
}
