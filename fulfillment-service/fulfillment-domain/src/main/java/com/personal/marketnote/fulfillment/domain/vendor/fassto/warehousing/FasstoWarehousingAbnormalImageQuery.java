package com.personal.marketnote.fulfillment.domain.vendor.fassto.warehousing;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoWarehousingAbnormalImageQuery {
    private String accessToken;
    private String slipNo;
    private String godCd;
    private String goodsSerialNo;
    private String fileSeq;
    private String imgNo;

    public static FasstoWarehousingAbnormalImageQuery of(
            String accessToken,
            String slipNo,
            String godCd,
            String goodsSerialNo,
            String fileSeq,
            String imgNo
    ) {
        FasstoWarehousingAbnormalImageQuery query = FasstoWarehousingAbnormalImageQuery.builder()
                .accessToken(accessToken)
                .slipNo(slipNo)
                .godCd(godCd)
                .goodsSerialNo(goodsSerialNo)
                .fileSeq(fileSeq)
                .imgNo(imgNo)
                .build();
        query.validate();
        return query;
    }

    private void validate() {
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new IllegalArgumentException("accessToken is required.");
        }
        if (FormatValidator.hasNoValue(slipNo)) {
            throw new IllegalArgumentException("slipNo is required.");
        }
        if (FormatValidator.hasNoValue(godCd)) {
            throw new IllegalArgumentException("godCd is required.");
        }
        if (FormatValidator.hasNoValue(goodsSerialNo)) {
            throw new IllegalArgumentException("goodsSerialNo is required.");
        }
        if (FormatValidator.hasNoValue(fileSeq)) {
            throw new IllegalArgumentException("fileSeq is required.");
        }
        if (FormatValidator.hasNoValue(imgNo)) {
            throw new IllegalArgumentException("imgNo is required.");
        }
    }
}
