package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record FulfillmentReturnGodDetailGoodsResult(
        String cstGodCd,
        String godNm,
        String makeDt,
        String distTermDt,
        String inQty,
        String remark,
        String rtnGodCheckStat,
        String rtnGodCheckStatNm
) {
    public static FulfillmentReturnGodDetailGoodsResult of(
            String cstGodCd,
            String godNm,
            String makeDt,
            String distTermDt,
            String inQty,
            String remark,
            String rtnGodCheckStat,
            String rtnGodCheckStatNm
    ) {
        return new FulfillmentReturnGodDetailGoodsResult(
                cstGodCd, godNm, makeDt, distTermDt, inQty, remark, rtnGodCheckStat, rtnGodCheckStatNm
        );
    }
}
