package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.warehousing;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentWarehousingGoodsMapper {
    private String cstGodCd;
    private String distTermDt;
    private Integer ordQty;

    public static FulfillmentWarehousingGoodsMapper of(
            String cstGodCd,
            String distTermDt,
            Integer ordQty
    ) {
        FulfillmentWarehousingGoodsMapper mapper = FulfillmentWarehousingGoodsMapper.builder()
                .cstGodCd(cstGodCd)
                .distTermDt(distTermDt)
                .ordQty(ordQty)
                .build();
        mapper.validate();
        return mapper;
    }

    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfNotNull(payload, "cstGodCd", cstGodCd);
        putIfNotNull(payload, "distTermDt", distTermDt);
        if (FormatValidator.hasValue(ordQty)) {
            payload.put("ordQty", ordQty);
        }
        return payload;
    }

    private void validate() {
        if (FormatValidator.hasNoValue(cstGodCd)) {
            throw new FulfillmentQueryParameterNoValueException("cstGodCd", "warehousing goods request");
        }
        if (FormatValidator.hasNoValue(ordQty)) {
            throw new FulfillmentQueryParameterNoValueException("ordQty", "warehousing goods request");
        }
    }

    private void putIfNotNull(Map<String, Object> payload, String key, String value) {
        if (FormatValidator.hasValue(value)) {
            payload.put(key, value);
        }
    }
}
