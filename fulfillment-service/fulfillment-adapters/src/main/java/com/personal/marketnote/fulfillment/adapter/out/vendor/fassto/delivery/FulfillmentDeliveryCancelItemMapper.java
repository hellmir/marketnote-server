package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentDeliveryCancelItemMapper {
    private String slipNo;
    private String ordNo;
    private String cstGodCd;

    public static FulfillmentDeliveryCancelItemMapper of(
            String slipNo,
            String ordNo
    ) {
        return of(slipNo, ordNo, null);
    }

    public static FulfillmentDeliveryCancelItemMapper of(
            String slipNo,
            String ordNo,
            String cstGodCd
    ) {
        FulfillmentDeliveryCancelItemMapper mapper = FulfillmentDeliveryCancelItemMapper.builder()
                .slipNo(slipNo)
                .ordNo(ordNo)
                .cstGodCd(cstGodCd)
                .build();
        mapper.validate();
        return mapper;
    }

    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("slipNo", slipNo);
        payload.put("ordNo", ordNo);
        if (FormatValidator.hasValue(cstGodCd)) {
            payload.put("cstGodCd", cstGodCd);
        }
        return payload;
    }

    private void validate() {
        if (FormatValidator.hasNoValue(slipNo)) {
            throw new FulfillmentQueryParameterNoValueException("slipNo", "delivery cancel request");
        }
        if (FormatValidator.hasNoValue(ordNo)) {
            throw new FulfillmentQueryParameterNoValueException("ordNo", "delivery cancel request");
        }
    }
}
