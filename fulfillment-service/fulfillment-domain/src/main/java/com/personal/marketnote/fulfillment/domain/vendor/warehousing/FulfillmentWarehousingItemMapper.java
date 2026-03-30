package com.personal.marketnote.fulfillment.domain.vendor.warehousing;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentWarehousingItemMapper {
    private String ordDt;
    private String ordNo;
    private String inWay;
    private String slipNo;
    private String parcelComp;
    private String parcelInvoiceNo;
    private String remark;
    private String cstSupCd;
    private String distTermDt;
    private String makeDt;
    private String preArv;
    private List<FulfillmentWarehousingGoodsMapper> godCds;

    public static FulfillmentWarehousingItemMapper of(
            String ordDt,
            String ordNo,
            String inWay,
            String slipNo,
            String parcelComp,
            String parcelInvoiceNo,
            String remark,
            String cstSupCd,
            String distTermDt,
            String makeDt,
            String preArv,
            List<FulfillmentWarehousingGoodsMapper> godCds
    ) {
        FulfillmentWarehousingItemMapper mapper = FulfillmentWarehousingItemMapper.builder()
                .ordDt(ordDt)
                .ordNo(ordNo)
                .inWay(inWay)
                .slipNo(slipNo)
                .parcelComp(parcelComp)
                .parcelInvoiceNo(parcelInvoiceNo)
                .remark(remark)
                .cstSupCd(cstSupCd)
                .distTermDt(distTermDt)
                .makeDt(makeDt)
                .preArv(preArv)
                .godCds(godCds)
                .build();
        mapper.validate();
        return mapper;
    }

    public static FulfillmentWarehousingItemMapper update(
            String ordDt,
            String ordNo,
            String inWay,
            String slipNo,
            String parcelComp,
            String parcelInvoiceNo,
            String remark,
            String cstSupCd,
            String distTermDt,
            String makeDt,
            String preArv,
            List<FulfillmentWarehousingGoodsMapper> godCds
    ) {
        FulfillmentWarehousingItemMapper mapper = FulfillmentWarehousingItemMapper.builder()
                .ordDt(ordDt)
                .ordNo(ordNo)
                .inWay(inWay)
                .slipNo(slipNo)
                .parcelComp(parcelComp)
                .parcelInvoiceNo(parcelInvoiceNo)
                .remark(remark)
                .cstSupCd(cstSupCd)
                .distTermDt(distTermDt)
                .makeDt(makeDt)
                .preArv(preArv)
                .godCds(godCds)
                .build();
        mapper.validateForUpdate();
        return mapper;
    }

    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfNotNull(payload, "ordDt", ordDt);
        putIfNotNull(payload, "ordNo", ordNo);
        putIfNotNull(payload, "inWay", inWay);
        putIfNotNull(payload, "slipNo", slipNo);
        putIfNotNull(payload, "parcelComp", parcelComp);
        putIfNotNull(payload, "parcelInvoiceNo", parcelInvoiceNo);
        putIfNotNull(payload, "remark", remark);
        putIfNotNull(payload, "cstSupCd", cstSupCd);
        putIfNotNull(payload, "distTermDt", distTermDt);
        putIfNotNull(payload, "makeDt", makeDt);
        putIfNotNull(payload, "preArv", preArv);
        if (FormatValidator.hasValue(godCds)) {
            payload.put("godCds", godCds.stream()
                    .map(FulfillmentWarehousingGoodsMapper::toPayload)
                    .toList());
        }
        return payload;
    }

    private void validate() {
        if (FormatValidator.hasNoValue(ordDt)) {
            throw new FulfillmentQueryParameterNoValueException("ordDt", "warehousing request");
        }
        if (FormatValidator.hasNoValue(inWay)) {
            throw new FulfillmentQueryParameterNoValueException("inWay", "warehousing request");
        }
        if (FormatValidator.hasNoValue(godCds)) {
            throw new FulfillmentQueryParameterNoValueException("godCds", "warehousing request");
        }
    }

    private void validateForUpdate() {
        validate();
        if (FormatValidator.hasNoValue(slipNo)) {
            throw new FulfillmentQueryParameterNoValueException("slipNo", "warehousing update");
        }
    }

    private void putIfNotNull(Map<String, Object> payload, String key, String value) {
        if (FormatValidator.hasValue(value)) {
            payload.put(key, value);
        }
    }
}
