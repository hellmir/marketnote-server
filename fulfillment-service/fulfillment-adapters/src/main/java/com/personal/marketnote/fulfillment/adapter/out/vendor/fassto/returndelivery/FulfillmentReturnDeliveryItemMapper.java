package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.returndelivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryGoodsMapper;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentReturnDeliveryItemMapper {
    private String ordDt;
    private String ordNo;
    private String parcelCd;
    private String invoiceNo;
    private String custNm;
    private String custTelNo;
    private String custAddr;
    private String rtnEmpNm;
    private String rtnTelNo;
    private String rtnZipCd;
    private String rtnAddr1;
    private String rtnAddr2;
    private String rtnGubun;
    private String rtnReason;
    private String getRtnDetailReason;
    private String rtnShipReqTerm;
    private List<FulfillmentDeliveryGoodsMapper> godCds;

    public static FulfillmentReturnDeliveryItemMapper of(
            String ordDt,
            String ordNo,
            String parcelCd,
            String invoiceNo,
            String custNm,
            String custTelNo,
            String custAddr,
            String rtnEmpNm,
            String rtnTelNo,
            String rtnZipCd,
            String rtnAddr1,
            String rtnAddr2,
            String rtnGubun,
            String rtnReason,
            String getRtnDetailReason,
            String rtnShipReqTerm,
            List<FulfillmentDeliveryGoodsMapper> godCds
    ) {
        FulfillmentReturnDeliveryItemMapper mapper = FulfillmentReturnDeliveryItemMapper.builder()
                .ordDt(ordDt)
                .ordNo(ordNo)
                .parcelCd(parcelCd)
                .invoiceNo(invoiceNo)
                .custNm(custNm)
                .custTelNo(custTelNo)
                .custAddr(custAddr)
                .rtnEmpNm(rtnEmpNm)
                .rtnTelNo(rtnTelNo)
                .rtnZipCd(rtnZipCd)
                .rtnAddr1(rtnAddr1)
                .rtnAddr2(rtnAddr2)
                .rtnGubun(rtnGubun)
                .rtnReason(rtnReason)
                .getRtnDetailReason(getRtnDetailReason)
                .rtnShipReqTerm(rtnShipReqTerm)
                .godCds(godCds)
                .build();
        mapper.validate();
        return mapper;
    }

    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasValue(payload, "ordDt", ordDt);
        putIfHasValue(payload, "ordNo", ordNo);
        putIfHasValue(payload, "parcelCd", parcelCd);
        putIfHasValue(payload, "invoiceNo", invoiceNo);
        putIfHasValue(payload, "custNm", custNm);
        putIfHasValue(payload, "custTelNo", custTelNo);
        putIfHasValue(payload, "custAddr", custAddr);
        putIfHasValue(payload, "rtnEmpNm", rtnEmpNm);
        putIfHasValue(payload, "rtnTelNo", rtnTelNo);
        putIfHasValue(payload, "rtnZipCd", rtnZipCd);
        putIfHasValue(payload, "rtnAddr1", rtnAddr1);
        putIfHasValue(payload, "rtnAddr2", rtnAddr2);
        putIfHasValue(payload, "rtnGubun", rtnGubun);
        putIfHasValue(payload, "rtnReason", rtnReason);
        putIfHasValue(payload, "getRtnDetailReason", getRtnDetailReason);
        putIfHasValue(payload, "rtnShipReqTerm", rtnShipReqTerm);
        if (FormatValidator.hasValue(godCds)) {
            payload.put("godCds", godCds.stream()
                    .map(FulfillmentDeliveryGoodsMapper::toPayload)
                    .toList());
        }
        return payload;
    }

    private void validate() {
        if (FormatValidator.hasNoValue(ordDt)) {
            throw new FulfillmentQueryParameterNoValueException("ordDt", "return delivery request");
        }
        if (FormatValidator.hasNoValue(parcelCd)) {
            throw new FulfillmentQueryParameterNoValueException("parcelCd", "return delivery request");
        }
        if (FormatValidator.hasNoValue(invoiceNo)) {
            throw new FulfillmentQueryParameterNoValueException("invoiceNo", "return delivery request");
        }
        if (FormatValidator.hasNoValue(custNm)) {
            throw new FulfillmentQueryParameterNoValueException("custNm", "return delivery request");
        }
        if (FormatValidator.hasNoValue(custTelNo)) {
            throw new FulfillmentQueryParameterNoValueException("custTelNo", "return delivery request");
        }
        if (FormatValidator.hasNoValue(custAddr)) {
            throw new FulfillmentQueryParameterNoValueException("custAddr", "return delivery request");
        }
    }

    private void putIfHasValue(Map<String, Object> payload, String key, String value) {
        if (FormatValidator.hasValue(value)) {
            payload.put(key, value);
        }
    }
}
