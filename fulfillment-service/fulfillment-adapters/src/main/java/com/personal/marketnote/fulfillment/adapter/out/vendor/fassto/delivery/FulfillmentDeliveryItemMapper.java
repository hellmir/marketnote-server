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
public class FulfillmentDeliveryItemMapper {
    private String ordDt;
    private String ordNo;
    private Integer ordSeq;
    private String slipNo;
    private String custNm;
    private String custTelNo;
    private String custAddr;
    private String outWay;
    private String sendNm;
    private String sendTelNo;
    private String salChanel;
    private String shipReqTerm;
    private List<FulfillmentDeliveryGoodsMapper> godCds;
    private String oneDayDeliveryCd;
    private String remark;

    public static FulfillmentDeliveryItemMapper of(
            String ordDt,
            String ordNo,
            Integer ordSeq,
            String slipNo,
            String custNm,
            String custTelNo,
            String custAddr,
            String outWay,
            String sendNm,
            String sendTelNo,
            String salChanel,
            String shipReqTerm,
            List<FulfillmentDeliveryGoodsMapper> godCds,
            String oneDayDeliveryCd,
            String remark
    ) {
        FulfillmentDeliveryItemMapper mapper = FulfillmentDeliveryItemMapper.builder()
                .ordDt(ordDt)
                .ordNo(ordNo)
                .ordSeq(ordSeq)
                .slipNo(slipNo)
                .custNm(custNm)
                .custTelNo(custTelNo)
                .custAddr(custAddr)
                .outWay(outWay)
                .sendNm(sendNm)
                .sendTelNo(sendTelNo)
                .salChanel(salChanel)
                .shipReqTerm(shipReqTerm)
                .godCds(godCds)
                .oneDayDeliveryCd(oneDayDeliveryCd)
                .remark(remark)
                .build();
        mapper.validate();
        return mapper;
    }

    public static FulfillmentDeliveryItemMapper update(
            String ordDt,
            String ordNo,
            Integer ordSeq,
            String slipNo,
            String custNm,
            String custTelNo,
            String custAddr,
            String outWay,
            String sendNm,
            String sendTelNo,
            String salChanel,
            String shipReqTerm,
            List<FulfillmentDeliveryGoodsMapper> godCds,
            String oneDayDeliveryCd,
            String remark
    ) {
        FulfillmentDeliveryItemMapper mapper = FulfillmentDeliveryItemMapper.builder()
                .ordDt(ordDt)
                .ordNo(ordNo)
                .ordSeq(ordSeq)
                .slipNo(slipNo)
                .custNm(custNm)
                .custTelNo(custTelNo)
                .custAddr(custAddr)
                .outWay(outWay)
                .sendNm(sendNm)
                .sendTelNo(sendTelNo)
                .salChanel(salChanel)
                .shipReqTerm(shipReqTerm)
                .godCds(godCds)
                .oneDayDeliveryCd(oneDayDeliveryCd)
                .remark(remark)
                .build();
        mapper.validateForUpdate();
        return mapper;
    }

    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasValue(payload, "ordDt", ordDt);
        putIfHasValue(payload, "ordNo", ordNo);
        if (FormatValidator.hasValue(ordSeq)) {
            payload.put("ordSeq", ordSeq);
        }
        putIfHasValue(payload, "slipNo", slipNo);
        putIfHasValue(payload, "custNm", custNm);
        putIfHasValue(payload, "custTelNo", custTelNo);
        putIfHasValue(payload, "custAddr", custAddr);
        putIfHasValue(payload, "outWay", outWay);
        putIfHasValue(payload, "sendNm", sendNm);
        putIfHasValue(payload, "sendTelNo", sendTelNo);
        putIfHasValue(payload, "salChanel", salChanel);
        putIfHasValue(payload, "shipReqTerm", shipReqTerm);
        if (FormatValidator.hasValue(godCds)) {
            payload.put("godCds", godCds.stream()
                    .map(FulfillmentDeliveryGoodsMapper::toPayload)
                    .toList());
        }
        putIfHasValue(payload, "oneDayDeliveryCd", oneDayDeliveryCd);
        putIfHasValue(payload, "remark", remark);
        return payload;
    }

    private void validate() {
        if (FormatValidator.hasNoValue(ordDt)) {
            throw new FulfillmentQueryParameterNoValueException("ordDt", "delivery request");
        }
        if (FormatValidator.hasNoValue(ordNo)) {
            throw new FulfillmentQueryParameterNoValueException("ordNo", "delivery request");
        }
        if (FormatValidator.hasNoValue(custNm)) {
            throw new FulfillmentQueryParameterNoValueException("custNm", "delivery request");
        }
        if (FormatValidator.hasNoValue(custTelNo)) {
            throw new FulfillmentQueryParameterNoValueException("custTelNo", "delivery request");
        }
        if (FormatValidator.hasNoValue(custAddr)) {
            throw new FulfillmentQueryParameterNoValueException("custAddr", "delivery request");
        }
        if (FormatValidator.hasNoValue(outWay)) {
            throw new FulfillmentQueryParameterNoValueException("outWay", "delivery request");
        }
        if (FormatValidator.hasNoValue(godCds)) {
            throw new FulfillmentQueryParameterNoValueException("godCds", "delivery request");
        }
    }

    private void validateForUpdate() {
        validate();
        if (FormatValidator.hasNoValue(slipNo)) {
            throw new FulfillmentQueryParameterNoValueException("slipNo", "delivery update request");
        }
    }

    private void putIfHasValue(Map<String, Object> payload, String key, String value) {
        if (FormatValidator.hasValue(value)) {
            payload.put(key, value);
        }
    }
}
