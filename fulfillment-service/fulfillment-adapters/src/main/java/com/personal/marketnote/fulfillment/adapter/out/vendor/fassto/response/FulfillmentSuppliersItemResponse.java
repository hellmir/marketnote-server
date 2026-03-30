package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentSuppliersItemResponse(
        String supCd,
        String supNm,
        String cstSupCd,
        String cstCd,
        String cstNm,
        String dealStrDt,
        String dealEndDt,
        String zipNo,
        String addr1,
        String addr2,
        String ceoNm,
        String busNo,
        String busSp,
        String busTp,
        String telNo,
        String faxNo,
        String empNm1,
        String empPosit1,
        String empTelNo1,
        String empEmail1,
        String empNm2,
        String empPosit2,
        String empTelNo2,
        String empEmail2,
        String useYn
) {
}
