package com.personal.marketnote.fulfillment.adapter.in.web.delivery.response;

import com.personal.marketnote.fulfillment.port.in.result.GetInternalReturnGodDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.InternalReturnGodDetailGoodsResult;
import com.personal.marketnote.fulfillment.port.in.result.InternalReturnGodDetailInfoResult;

import java.util.List;

public record GetInternalReturnGodDetailResponse(
        Integer dataCount,
        List<ReturnGodDetailInfo> returnGodInfos
) {
    public static GetInternalReturnGodDetailResponse from(GetInternalReturnGodDetailResult result) {
        List<ReturnGodDetailInfo> infos = result.returnGodInfos().stream()
                .map(ReturnGodDetailInfo::from)
                .toList();
        return new GetInternalReturnGodDetailResponse(result.dataCount(), infos);
    }

    public record ReturnGodDetailInfo(
            String orderNumber,
            String inboundOrderSlipNumber,
            List<ReturnGodDetailGoods> products
    ) {
        public static ReturnGodDetailInfo from(InternalReturnGodDetailInfoResult result) {
            List<ReturnGodDetailGoods> goods = result.products().stream()
                    .map(ReturnGodDetailGoods::from)
                    .toList();
            return new ReturnGodDetailInfo(result.orderNumber(), result.inboundOrderSlipNumber(), goods);
        }
    }

    public record ReturnGodDetailGoods(
            String customerProductCode,
            String productName,
            String returnProductCheckStatus,
            String returnProductCheckStatusName
    ) {
        public static ReturnGodDetailGoods from(InternalReturnGodDetailGoodsResult result) {
            return new ReturnGodDetailGoods(
                    result.customerProductCode(),
                    result.productName(),
                    result.returnProductCheckStatus(),
                    result.returnProductCheckStatusName()
            );
        }
    }
}
