package com.personal.marketnote.fulfillment.domain.vendor.fassto.warehousing;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
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
            throw new FasstoQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(slipNo)) {
            throw new FasstoQueryParameterNoValueException("slipNo");
        }
        if (FormatValidator.hasNoValue(godCd)) {
            throw new FasstoQueryParameterNoValueException("godCd");
        }
        if (FormatValidator.hasNoValue(goodsSerialNo)) {
            throw new FasstoQueryParameterNoValueException("goodsSerialNo");
        }
        if (FormatValidator.hasNoValue(fileSeq)) {
            throw new FasstoQueryParameterNoValueException("fileSeq");
        }
        if (FormatValidator.hasNoValue(imgNo)) {
            throw new FasstoQueryParameterNoValueException("imgNo");
        }
    }
}
