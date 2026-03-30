package com.personal.marketnote.fulfillment.domain.vendor.warehousing;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentWarehousingAbnormalImageQuery {
    private String accessToken;
    private String slipNo;
    private String godCd;
    private String goodsSerialNo;
    private String fileSeq;
    private String imgNo;

    public static FulfillmentWarehousingAbnormalImageQuery of(
            String accessToken,
            String slipNo,
            String godCd,
            String goodsSerialNo,
            String fileSeq,
            String imgNo
    ) {
        FulfillmentWarehousingAbnormalImageQuery query = FulfillmentWarehousingAbnormalImageQuery.builder()
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
            throw new FulfillmentQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(slipNo)) {
            throw new FulfillmentQueryParameterNoValueException("slipNo");
        }
        if (FormatValidator.hasNoValue(godCd)) {
            throw new FulfillmentQueryParameterNoValueException("godCd");
        }
        if (FormatValidator.hasNoValue(goodsSerialNo)) {
            throw new FulfillmentQueryParameterNoValueException("goodsSerialNo");
        }
        if (FormatValidator.hasNoValue(fileSeq)) {
            throw new FulfillmentQueryParameterNoValueException("fileSeq");
        }
        if (FormatValidator.hasNoValue(imgNo)) {
            throw new FulfillmentQueryParameterNoValueException("imgNo");
        }
    }
}
