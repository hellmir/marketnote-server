package com.personal.marketnote.fulfillment.port.in.result;

import java.util.List;

public record InternalReturnGodDetailInfoResult(
        String orderNumber,
        String inboundOrderSlipNumber,
        List<InternalReturnGodDetailGoodsResult> products
) {
    public static InternalReturnGodDetailInfoResult of(
            String orderNumber,
            String inboundOrderSlipNumber,
            List<InternalReturnGodDetailGoodsResult> products
    ) {
        return new InternalReturnGodDetailInfoResult(orderNumber, inboundOrderSlipNumber, products);
    }
}
