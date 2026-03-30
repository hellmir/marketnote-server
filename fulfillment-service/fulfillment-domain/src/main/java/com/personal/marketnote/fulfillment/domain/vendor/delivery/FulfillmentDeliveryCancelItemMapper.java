package com.personal.marketnote.fulfillment.domain.vendor.delivery;

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

    public static FulfillmentDeliveryCancelItemMapper of(
            String slipNo,
            String ordNo
    ) {
        FulfillmentDeliveryCancelItemMapper mapper = FulfillmentDeliveryCancelItemMapper.builder()
                .slipNo(slipNo)
                .ordNo(ordNo)
                .build();
        mapper.validate();
        return mapper;
    }

    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("slipNo", slipNo);
        payload.put("ordNo", ordNo);
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
