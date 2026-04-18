package com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper;

import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentWarehousingGoodsRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentWarehousingRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFulfillmentWarehousingGoodsRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFulfillmentWarehousingRequest;
import com.personal.marketnote.fulfillment.port.in.command.vendor.*;

import java.util.List;

public class FulfillmentWarehousingRequestToCommandMapper {
    public static RegisterFulfillmentWarehousingCommand mapToRegisterCommand(
            String customerCode,
            String accessToken,
            List<RegisterFulfillmentWarehousingRequest> request
    ) {
        List<RegisterFulfillmentWarehousingItemCommand> warehousingRequests = request.stream()
                .map(FulfillmentWarehousingRequestToCommandMapper::mapItem)
                .toList();

        return RegisterFulfillmentWarehousingCommand.of(customerCode, accessToken, warehousingRequests);
    }

    public static GetFulfillmentWarehousingCommand mapToWarehousingQuery(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String inWay,
            String ordNo,
            String wrkStat
    ) {
        return GetFulfillmentWarehousingCommand.of(customerCode, accessToken, startDate, endDate, inWay, ordNo, wrkStat);
    }

    public static GetFulfillmentWarehousingDetailCommand mapToWarehousingDetailCommand(
            String customerCode,
            String accessToken,
            String slipNo,
            String ordNo
    ) {
        return GetFulfillmentWarehousingDetailCommand.of(customerCode, accessToken, slipNo, ordNo);
    }

    public static GetFulfillmentWarehousingAbnormalCommand mapToWarehousingAbnormalCommand(
            String customerCode,
            String accessToken,
            String whCd,
            String slipNo
    ) {
        return GetFulfillmentWarehousingAbnormalCommand.of(customerCode, accessToken, whCd, slipNo);
    }

    public static GetFulfillmentWarehousingAbnormalImageCommand mapToWarehousingAbnormalImageCommand(
            String accessToken,
            String slipNo,
            String godCd,
            String goodsSerialNo,
            String fileSeq,
            String imgNo
    ) {
        return GetFulfillmentWarehousingAbnormalImageCommand.of(
                accessToken,
                slipNo,
                godCd,
                goodsSerialNo,
                fileSeq,
                imgNo
        );
    }

    public static GetFulfillmentWarehousingInspecDetailCommand mapToWarehousingInspecDetailCommand(
            String customerCode,
            String accessToken,
            String slipNo,
            String whCd
    ) {
        return GetFulfillmentWarehousingInspecDetailCommand.of(customerCode, accessToken, slipNo, whCd);
    }

    public static UpdateFulfillmentWarehousingCommand mapToUpdateCommand(
            String customerCode,
            String accessToken,
            List<UpdateFulfillmentWarehousingRequest> request
    ) {
        List<UpdateFulfillmentWarehousingItemCommand> warehousingRequests = request.stream()
                .map(FulfillmentWarehousingRequestToCommandMapper::mapUpdateItem)
                .toList();

        return UpdateFulfillmentWarehousingCommand.of(customerCode, accessToken, warehousingRequests);
    }

    private static RegisterFulfillmentWarehousingItemCommand mapItem(RegisterFulfillmentWarehousingRequest item) {
        List<RegisterFulfillmentWarehousingGoodsCommand> goods = item.getGodCds().stream()
                .map(FulfillmentWarehousingRequestToCommandMapper::mapGoods)
                .toList();

        return RegisterFulfillmentWarehousingItemCommand.builder()
                .orderDate(item.getOrdDt())
                .orderNumber(item.getOrdNo())
                .warehousingMethod(item.getInWay())
                .slipNumber(item.getSlipNo())
                .courierCompany(item.getParcelComp())
                .trackingNumber(item.getParcelInvoiceNo())
                .remark(item.getRemark())
                .supplierCode(item.getCstSupCd())
                .expirationDate(item.getDistTermDt())
                .manufacturingDate(item.getMakeDt())
                .preArrival(item.getPreArv())
                .products(goods)
                .build();
    }

    private static UpdateFulfillmentWarehousingItemCommand mapUpdateItem(UpdateFulfillmentWarehousingRequest item) {
        List<UpdateFulfillmentWarehousingGoodsCommand> goods = item.getGodCds().stream()
                .map(FulfillmentWarehousingRequestToCommandMapper::mapUpdateGoods)
                .toList();

        return UpdateFulfillmentWarehousingItemCommand.of(
                item.getOrdDt(),
                item.getOrdNo(),
                item.getInWay(),
                item.getSlipNo(),
                item.getParcelComp(),
                item.getParcelInvoiceNo(),
                item.getRemark(),
                item.getCstSupCd(),
                item.getDistTermDt(),
                item.getMakeDt(),
                item.getPreArv(),
                goods
        );
    }

    private static RegisterFulfillmentWarehousingGoodsCommand mapGoods(RegisterFulfillmentWarehousingGoodsRequest item) {
        return RegisterFulfillmentWarehousingGoodsCommand.of(
                item.getCstGodCd(),
                item.getDistTermDt(),
                item.getOrdQty()
        );
    }

    private static UpdateFulfillmentWarehousingGoodsCommand mapUpdateGoods(UpdateFulfillmentWarehousingGoodsRequest item) {
        return UpdateFulfillmentWarehousingGoodsCommand.of(
                item.getCstGodCd(),
                item.getDistTermDt(),
                item.getOrdQty()
        );
    }
}
