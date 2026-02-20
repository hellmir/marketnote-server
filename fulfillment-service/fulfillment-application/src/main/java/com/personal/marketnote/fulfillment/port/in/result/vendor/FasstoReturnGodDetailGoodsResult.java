package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record FasstoReturnGodDetailGoodsResult(
        String cstGodCd,
        String godNm,
        String makeDt,
        String distTermDt,
        String inQty,
        String remark,
        String rtnGodCheckStat,
        String rtnGodCheckStatNm
) {
    public static FasstoReturnGodDetailGoodsResult of(
            String cstGodCd,
            String godNm,
            String makeDt,
            String distTermDt,
            String inQty,
            String remark,
            String rtnGodCheckStat,
            String rtnGodCheckStatNm
    ) {
        return new FasstoReturnGodDetailGoodsResult(
                cstGodCd, godNm, makeDt, distTermDt, inQty, remark, rtnGodCheckStat, rtnGodCheckStatNm
        );
    }
}
