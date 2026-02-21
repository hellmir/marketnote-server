package com.personal.marketnote.fulfillment.domain.vendor.fassto.warehousing;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoWarehousingGoodsMapper {
    private String cstGodCd;
    private String distTermDt;
    private Integer ordQty;

    public static FasstoWarehousingGoodsMapper of(
            String cstGodCd,
            String distTermDt,
            Integer ordQty
    ) {
        FasstoWarehousingGoodsMapper mapper = FasstoWarehousingGoodsMapper.builder()
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
            throw new IllegalArgumentException("cstGodCd is required for warehousing goods request.");
        }
        if (FormatValidator.hasNoValue(ordQty)) {
            throw new IllegalArgumentException("ordQty is required for warehousing goods request.");
        }
    }

    private void putIfNotNull(Map<String, Object> payload, String key, String value) {
        if (FormatValidator.hasValue(value)) {
            payload.put(key, value);
        }
    }
}
