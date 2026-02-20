package com.personal.marketnote.fulfillment.domain.vendor.fassto.returndelivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery.FasstoDeliveryGoodsMapper;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoDirectReturnDeliveryItemMapper {
    private String ordDt;
    private String supCd;
    private String orgParcelCd;
    private String orgInvoiceNo;
    private String inWay;
    private String custNm;
    private String rtnParcelComp;
    private String rtnInvoiceNo;
    private String rtnGubun;
    private String rtnReason;
    private String rtnDetailReason;
    private String remark;
    private List<FasstoDeliveryGoodsMapper> godCds;

    public static FasstoDirectReturnDeliveryItemMapper of(
            String ordDt,
            String supCd,
            String orgParcelCd,
            String orgInvoiceNo,
            String inWay,
            String custNm,
            String rtnParcelComp,
            String rtnInvoiceNo,
            String rtnGubun,
            String rtnReason,
            String rtnDetailReason,
            String remark,
            List<FasstoDeliveryGoodsMapper> godCds
    ) {
        FasstoDirectReturnDeliveryItemMapper mapper = FasstoDirectReturnDeliveryItemMapper.builder()
                .ordDt(ordDt)
                .supCd(supCd)
                .orgParcelCd(orgParcelCd)
                .orgInvoiceNo(orgInvoiceNo)
                .inWay(inWay)
                .custNm(custNm)
                .rtnParcelComp(rtnParcelComp)
                .rtnInvoiceNo(rtnInvoiceNo)
                .rtnGubun(rtnGubun)
                .rtnReason(rtnReason)
                .rtnDetailReason(rtnDetailReason)
                .remark(remark)
                .godCds(godCds)
                .build();
        mapper.validate();
        return mapper;
    }

    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasValue(payload, "ordDt", ordDt);
        putIfHasValue(payload, "supCd", supCd);
        putIfHasValue(payload, "orgParcelCd", orgParcelCd);
        putIfHasValue(payload, "orgInvoiceNo", orgInvoiceNo);
        putIfHasValue(payload, "inWay", inWay);
        putIfHasValue(payload, "custNm", custNm);
        putIfHasValue(payload, "rtnParcelComp", rtnParcelComp);
        putIfHasValue(payload, "rtnInvoiceNo", rtnInvoiceNo);
        putIfHasValue(payload, "rtnGubun", rtnGubun);
        putIfHasValue(payload, "rtnReason", rtnReason);
        putIfHasValue(payload, "rtnDetailReason", rtnDetailReason);
        putIfHasValue(payload, "remark", remark);
        if (FormatValidator.hasValue(godCds)) {
            payload.put("godCds", godCds.stream()
                    .map(FasstoDeliveryGoodsMapper::toPayload)
                    .toList());
        }
        return payload;
    }

    private void validate() {
        if (FormatValidator.hasNoValue(ordDt)) {
            throw new IllegalArgumentException("ordDt is required for direct return delivery request.");
        }
        if (FormatValidator.hasNoValue(orgParcelCd)) {
            throw new IllegalArgumentException("orgParcelCd is required for direct return delivery request.");
        }
        if (FormatValidator.hasNoValue(orgInvoiceNo)) {
            throw new IllegalArgumentException("orgInvoiceNo is required for direct return delivery request.");
        }
        if (FormatValidator.hasNoValue(inWay)) {
            throw new IllegalArgumentException("inWay is required for direct return delivery request.");
        }
        if (FormatValidator.hasNoValue(custNm)) {
            throw new IllegalArgumentException("custNm is required for direct return delivery request.");
        }
        if (FormatValidator.hasNoValue(rtnGubun)) {
            throw new IllegalArgumentException("rtnGubun is required for direct return delivery request.");
        }
        if (FormatValidator.hasNoValue(rtnReason)) {
            throw new IllegalArgumentException("rtnReason is required for direct return delivery request.");
        }
    }

    private void putIfHasValue(Map<String, Object> payload, String key, String value) {
        if (FormatValidator.hasValue(value)) {
            payload.put(key, value);
        }
    }
}
