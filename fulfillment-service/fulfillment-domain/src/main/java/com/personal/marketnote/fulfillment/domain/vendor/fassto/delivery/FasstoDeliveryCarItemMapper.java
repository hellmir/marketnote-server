package com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoDeliveryCarItemMapper {
    private String ordDt;
    private String ordNo;
    private String slipNo;
    private String outWay;
    private String cstShopCd;
    private List<FasstoDeliveryGoodsMapper> godCds;
    private String remark;

    public static FasstoDeliveryCarItemMapper of(
            String ordDt,
            String ordNo,
            String slipNo,
            String outWay,
            String cstShopCd,
            List<FasstoDeliveryGoodsMapper> godCds,
            String remark
    ) {
        FasstoDeliveryCarItemMapper mapper = FasstoDeliveryCarItemMapper.builder()
                .ordDt(ordDt)
                .ordNo(ordNo)
                .slipNo(slipNo)
                .outWay(outWay)
                .cstShopCd(cstShopCd)
                .godCds(godCds)
                .remark(remark)
                .build();
        mapper.validate();
        return mapper;
    }

    public static FasstoDeliveryCarItemMapper update(
            String ordDt,
            String ordNo,
            String slipNo,
            String outWay,
            String cstShopCd,
            List<FasstoDeliveryGoodsMapper> godCds,
            String remark
    ) {
        FasstoDeliveryCarItemMapper mapper = FasstoDeliveryCarItemMapper.builder()
                .ordDt(ordDt)
                .ordNo(ordNo)
                .slipNo(slipNo)
                .outWay(outWay)
                .cstShopCd(cstShopCd)
                .godCds(godCds)
                .remark(remark)
                .build();
        mapper.validateForUpdate();
        return mapper;
    }

    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasValue(payload, "ordDt", ordDt);
        putIfHasValue(payload, "ordNo", ordNo);
        putIfHasValue(payload, "slipNo", slipNo);
        putIfHasValue(payload, "outWay", outWay);
        putIfHasValue(payload, "cstShopCd", cstShopCd);
        if (FormatValidator.hasValue(godCds)) {
            payload.put("godCds", godCds.stream()
                    .map(FasstoDeliveryGoodsMapper::toPayload)
                    .toList());
        }
        putIfHasValue(payload, "remark", remark);
        return payload;
    }

    private void validate() {
        if (FormatValidator.hasNoValue(ordDt)) {
            throw new IllegalArgumentException("ordDt is required for delivery car request.");
        }
        if (FormatValidator.hasNoValue(ordNo)) {
            throw new IllegalArgumentException("ordNo is required for delivery car request.");
        }
        if (FormatValidator.hasNoValue(outWay)) {
            throw new IllegalArgumentException("outWay is required for delivery car request.");
        }
        if (FormatValidator.hasNoValue(cstShopCd)) {
            throw new IllegalArgumentException("cstShopCd is required for delivery car request.");
        }
        if (FormatValidator.hasNoValue(godCds)) {
            throw new IllegalArgumentException("godCds is required for delivery car request.");
        }
    }

    private void validateForUpdate() {
        validate();
        if (FormatValidator.hasNoValue(slipNo)) {
            throw new IllegalArgumentException("slipNo is required for delivery car update request.");
        }
    }

    private void putIfHasValue(Map<String, Object> payload, String key, String value) {
        if (FormatValidator.hasValue(value)) {
            payload.put(key, value);
        }
    }
}
