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
public class FasstoDeliveryGoodsMapper {
    private String cstGodCd;
    private String distTermDt;
    private Integer ordQty;

    public static FasstoDeliveryGoodsMapper of(
            String cstGodCd,
            String distTermDt,
            Integer ordQty
    ) {
        FasstoDeliveryGoodsMapper mapper = FasstoDeliveryGoodsMapper.builder()
                .cstGodCd(cstGodCd)
                .distTermDt(distTermDt)
                .ordQty(ordQty)
                .build();
        mapper.validate();
        return mapper;
    }

    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasValue(payload, "cstGodCd", cstGodCd);
        putIfHasValue(payload, "distTermDt", distTermDt);
        if (FormatValidator.hasValue(ordQty)) {
            payload.put("ordQty", ordQty);
        }
        return payload;
    }

    private void validate() {
        if (FormatValidator.hasNoValue(cstGodCd)) {
            throw new FasstoQueryParameterNoValueException("cstGodCd", "delivery goods request");
        }
        if (FormatValidator.hasNoValue(ordQty)) {
            throw new FasstoQueryParameterNoValueException("ordQty", "delivery goods request");
        }
    }

    private void putIfHasValue(Map<String, Object> payload, String key, String value) {
        if (FormatValidator.hasValue(value)) {
            payload.put(key, value);
        }
    }
}
