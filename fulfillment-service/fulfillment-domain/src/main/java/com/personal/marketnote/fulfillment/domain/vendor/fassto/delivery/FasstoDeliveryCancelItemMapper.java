package com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoDeliveryCancelItemMapper {
    private String slipNo;
    private String ordNo;

    public static FasstoDeliveryCancelItemMapper of(
            String slipNo,
            String ordNo
    ) {
        FasstoDeliveryCancelItemMapper mapper = FasstoDeliveryCancelItemMapper.builder()
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
            throw new FasstoQueryParameterNoValueException("slipNo", "delivery cancel request");
        }
        if (FormatValidator.hasNoValue(ordNo)) {
            throw new FasstoQueryParameterNoValueException("ordNo", "delivery cancel request");
        }
    }
}
