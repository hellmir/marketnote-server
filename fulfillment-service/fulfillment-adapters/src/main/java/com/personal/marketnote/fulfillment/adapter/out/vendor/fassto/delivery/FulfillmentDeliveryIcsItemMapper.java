package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery;

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
public class FulfillmentDeliveryIcsItemMapper {
    private String ordDt;
    private String ordNo;
    private String platform;
    private String logiCenter;
    private String invoiceNo;
    private String custNm;
    private String custTelNo;
    private String custAddr;
    private String sendNm;
    private String sendTelNo;
    private String salChanel;
    private String shipReqTerm;
    private String remark;
    private List<FulfillmentDeliveryGoodsMapper> godCds;

    public static FulfillmentDeliveryIcsItemMapper of(
            String ordDt,
            String ordNo,
            String platform,
            String logiCenter,
            String invoiceNo,
            String custNm,
            String custTelNo,
            String custAddr,
            String sendNm,
            String sendTelNo,
            String salChanel,
            String shipReqTerm,
            String remark,
            List<FulfillmentDeliveryGoodsMapper> godCds
    ) {
        FulfillmentDeliveryIcsItemMapper mapper = FulfillmentDeliveryIcsItemMapper.builder()
                .ordDt(ordDt)
                .ordNo(ordNo)
                .platform(platform)
                .logiCenter(logiCenter)
                .invoiceNo(invoiceNo)
                .custNm(custNm)
                .custTelNo(custTelNo)
                .custAddr(custAddr)
                .sendNm(sendNm)
                .sendTelNo(sendTelNo)
                .salChanel(salChanel)
                .shipReqTerm(shipReqTerm)
                .remark(remark)
                .godCds(godCds)
                .build();
        mapper.validate();
        return mapper;
    }

    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasValue(payload, "ordDt", ordDt);
        putIfHasValue(payload, "ordNo", ordNo);
        putIfHasValue(payload, "platform", platform);
        putIfHasValue(payload, "logiCenter", logiCenter);
        putIfHasValue(payload, "invoiceNo", invoiceNo);
        putIfHasValue(payload, "custNm", custNm);
        putIfHasValue(payload, "custTelNo", custTelNo);
        putIfHasValue(payload, "custAddr", custAddr);
        putIfHasValue(payload, "sendNm", sendNm);
        putIfHasValue(payload, "sendTelNo", sendTelNo);
        putIfHasValue(payload, "salChanel", salChanel);
        putIfHasValue(payload, "shipReqTerm", shipReqTerm);
        putIfHasValue(payload, "remark", remark);
        if (FormatValidator.hasValue(godCds)) {
            payload.put("godCds", godCds.stream()
                    .map(FulfillmentDeliveryGoodsMapper::toPayload)
                    .toList());
        }
        return payload;
    }

    private void validate() {
        if (FormatValidator.hasNoValue(ordDt)) {
            throw new FulfillmentQueryParameterNoValueException("ordDt", "delivery ics request");
        }
        if (FormatValidator.hasNoValue(ordNo)) {
            throw new FulfillmentQueryParameterNoValueException("ordNo", "delivery ics request");
        }
        if (FormatValidator.hasNoValue(platform)) {
            throw new FulfillmentQueryParameterNoValueException("platform", "delivery ics request");
        }
        if (FormatValidator.hasNoValue(logiCenter)) {
            throw new FulfillmentQueryParameterNoValueException("logiCenter", "delivery ics request");
        }
        if (FormatValidator.hasNoValue(invoiceNo)) {
            throw new FulfillmentQueryParameterNoValueException("invoiceNo", "delivery ics request");
        }
        if (FormatValidator.hasNoValue(godCds)) {
            throw new FulfillmentQueryParameterNoValueException("godCds", "delivery ics request");
        }
    }

    private void putIfHasValue(Map<String, Object> payload, String key, String value) {
        if (FormatValidator.hasValue(value)) {
            payload.put(key, value);
        }
    }
}
