package com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper;

import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.*;
import com.personal.marketnote.fulfillment.port.in.command.vendor.*;

import java.util.List;

public class FasstoDeliveryRequestToCommandMapper {
    public static RegisterFasstoDeliveryCommand mapToRegisterCommand(
            String customerCode,
            String accessToken,
            List<RegisterFasstoDeliveryRequest> request
    ) {
        List<RegisterFasstoDeliveryItemCommand> deliveryRequests = request.stream()
                .map(FasstoDeliveryRequestToCommandMapper::mapItem)
                .toList();

        return RegisterFasstoDeliveryCommand.of(customerCode, accessToken, deliveryRequests);
    }

    public static UpdateFasstoDeliveryCommand mapToUpdateCommand(
            String customerCode,
            String accessToken,
            List<UpdateFasstoDeliveryRequest> request
    ) {
        List<UpdateFasstoDeliveryItemCommand> deliveryRequests = request.stream()
                .map(FasstoDeliveryRequestToCommandMapper::mapUpdateItem)
                .toList();

        return UpdateFasstoDeliveryCommand.of(customerCode, accessToken, deliveryRequests);
    }

    public static RegisterFasstoDeliveryCarCommand mapToRegisterCarCommand(
            String customerCode,
            String accessToken,
            List<RegisterFasstoDeliveryCarRequest> request
    ) {
        List<RegisterFasstoDeliveryCarItemCommand> deliveryRequests = request.stream()
                .map(FasstoDeliveryRequestToCommandMapper::mapCarItem)
                .toList();

        return RegisterFasstoDeliveryCarCommand.of(customerCode, accessToken, deliveryRequests);
    }

    public static RegisterFasstoDeliveryIcsCommand mapToRegisterIcsCommand(
            String customerCode,
            String accessToken,
            List<RegisterFasstoDeliveryIcsRequest> request
    ) {
        List<RegisterFasstoDeliveryIcsItemCommand> deliveryRequests = request.stream()
                .map(FasstoDeliveryRequestToCommandMapper::mapIcsItem)
                .toList();

        return RegisterFasstoDeliveryIcsCommand.of(customerCode, accessToken, deliveryRequests);
    }

    public static UpdateFasstoDeliveryCarCommand mapToUpdateCarCommand(
            String customerCode,
            String accessToken,
            List<UpdateFasstoDeliveryCarRequest> request
    ) {
        List<UpdateFasstoDeliveryCarItemCommand> deliveryRequests = request.stream()
                .map(FasstoDeliveryRequestToCommandMapper::mapUpdateCarItem)
                .toList();

        return UpdateFasstoDeliveryCarCommand.of(customerCode, accessToken, deliveryRequests);
    }

    public static GetFasstoDeliveriesCommand mapToDeliveriesCommand(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String status,
            String outDiv,
            String ordNo
    ) {
        return GetFasstoDeliveriesCommand.of(
                customerCode,
                accessToken,
                startDate,
                endDate,
                status,
                outDiv,
                ordNo
        );
    }

    public static GetFasstoDeliveryDetailCommand mapToDeliveryDetailCommand(
            String customerCode,
            String accessToken,
            String slipNo,
            String ordNo
    ) {
        return GetFasstoDeliveryDetailCommand.of(customerCode, accessToken, slipNo, ordNo);
    }

    public static GetFasstoDeliveryStatusesCommand mapToDeliveryStatusesCommand(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String outDiv
    ) {
        return GetFasstoDeliveryStatusesCommand.of(customerCode, accessToken, startDate, endDate, outDiv);
    }

    public static CancelFasstoDeliveryCommand mapToCancelCommand(
            String customerCode,
            String accessToken,
            List<CancelFasstoDeliveryRequest> request
    ) {
        List<CancelFasstoDeliveryItemCommand> cancelRequests = request.stream()
                .map(item -> CancelFasstoDeliveryItemCommand.of(item.getSlipNo(), item.getOrdNo()))
                .toList();

        return CancelFasstoDeliveryCommand.of(customerCode, accessToken, cancelRequests);
    }

    public static GetFasstoDeliveryOutOrdGoodsDetailCommand mapToOutOrdGoodsDetailCommand(
            String customerCode,
            String accessToken,
            String outOrdSlipNo
    ) {
        return GetFasstoDeliveryOutOrdGoodsDetailCommand.of(customerCode, accessToken, outOrdSlipNo);
    }

    public static GetFasstoDeliveryOutOrdGoodsByOrdNoCommand mapToOutOrdGoodsByOrdNoCommand(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String ordNo
    ) {
        return GetFasstoDeliveryOutOrdGoodsByOrdNoCommand.of(
                customerCode,
                accessToken,
                startDate,
                endDate,
                ordNo
        );
    }

    public static GetFasstoDeliveryGoodDetailCommand mapToDeliveryGoodDetailCommand(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String ordNo
    ) {
        return GetFasstoDeliveryGoodDetailCommand.of(
                customerCode,
                accessToken,
                startDate,
                endDate,
                ordNo
        );
    }

    public static CompleteFasstoDeliveryIcsCommand mapToIcsCompletionCommand(
            String customerCode,
            String accessToken,
            CompleteFasstoDeliveryIcsRequest request
    ) {
        CompleteFasstoDeliveryIcsItemCommand itemCommand = CompleteFasstoDeliveryIcsItemCommand.of(request.getOrdNoList());
        return CompleteFasstoDeliveryIcsCommand.of(customerCode, accessToken, List.of(itemCommand));
    }

    private static RegisterFasstoDeliveryItemCommand mapItem(RegisterFasstoDeliveryRequest item) {
        List<RegisterFasstoDeliveryGoodsCommand> goods = item.getGodCds().stream()
                .map(FasstoDeliveryRequestToCommandMapper::mapGoods)
                .toList();

        return RegisterFasstoDeliveryItemCommand.builder()
                .ordDt(item.getOrdDt())
                .ordNo(item.getOrdNo())
                .ordSeq(item.getOrdSeq())
                .slipNo(item.getSlipNo())
                .custNm(item.getCustNm())
                .custTelNo(item.getCustTelNo())
                .custAddr(item.getCustAddr())
                .outWay(item.getOutWay())
                .sendNm(item.getSendNm())
                .sendTelNo(item.getSendTelNo())
                .salChanel(item.getSalChanel())
                .shipReqTerm(item.getShipReqTerm())
                .godCds(goods)
                .oneDayDeliveryCd(item.getOneDayDeliveryCd())
                .remark(item.getRemark())
                .build();
    }

    private static UpdateFasstoDeliveryItemCommand mapUpdateItem(UpdateFasstoDeliveryRequest item) {
        List<RegisterFasstoDeliveryGoodsCommand> goods = item.getGodCds().stream()
                .map(FasstoDeliveryRequestToCommandMapper::mapGoods)
                .toList();

        return UpdateFasstoDeliveryItemCommand.builder()
                .ordDt(item.getOrdDt())
                .ordNo(item.getOrdNo())
                .ordSeq(item.getOrdSeq())
                .slipNo(item.getSlipNo())
                .custNm(item.getCustNm())
                .custTelNo(item.getCustTelNo())
                .custAddr(item.getCustAddr())
                .outWay(item.getOutWay())
                .sendNm(item.getSendNm())
                .sendTelNo(item.getSendTelNo())
                .salChanel(item.getSalChanel())
                .shipReqTerm(item.getShipReqTerm())
                .godCds(goods)
                .oneDayDeliveryCd(item.getOneDayDeliveryCd())
                .remark(item.getRemark())
                .build();
    }

    private static RegisterFasstoDeliveryCarItemCommand mapCarItem(RegisterFasstoDeliveryCarRequest item) {
        List<RegisterFasstoDeliveryGoodsCommand> goods = item.getGodCds().stream()
                .map(FasstoDeliveryRequestToCommandMapper::mapGoods)
                .toList();

        return RegisterFasstoDeliveryCarItemCommand.builder()
                .ordDt(item.getOrdDt())
                .ordNo(item.getOrdNo())
                .slipNo(item.getSlipNo())
                .outWay(item.getOutWay())
                .cstShopCd(item.getCstShopCd())
                .godCds(goods)
                .remark(item.getRemark())
                .build();
    }

    private static UpdateFasstoDeliveryCarItemCommand mapUpdateCarItem(UpdateFasstoDeliveryCarRequest item) {
        List<RegisterFasstoDeliveryGoodsCommand> goods = item.getGodCds().stream()
                .map(FasstoDeliveryRequestToCommandMapper::mapGoods)
                .toList();

        return UpdateFasstoDeliveryCarItemCommand.builder()
                .ordDt(item.getOrdDt())
                .ordNo(item.getOrdNo())
                .slipNo(item.getSlipNo())
                .outWay(item.getOutWay())
                .cstShopCd(item.getCstShopCd())
                .godCds(goods)
                .remark(item.getRemark())
                .build();
    }

    private static RegisterFasstoDeliveryIcsItemCommand mapIcsItem(RegisterFasstoDeliveryIcsRequest item) {
        List<RegisterFasstoDeliveryGoodsCommand> goods = item.getGodCds().stream()
                .map(FasstoDeliveryRequestToCommandMapper::mapIcsGoods)
                .toList();

        return RegisterFasstoDeliveryIcsItemCommand.builder()
                .ordDt(item.getOrdDt())
                .ordNo(item.getOrdNo())
                .platform(item.getPlatform())
                .logiCenter(item.getLogiCenter())
                .invoiceNo(item.getInvoiceNo())
                .custNm(item.getCustNm())
                .custTelNo(item.getCustTelNo())
                .custAddr(item.getCustAddr())
                .sendNm(item.getSendNm())
                .sendTelNo(item.getSendTelNo())
                .salChanel(item.getSalChanel())
                .shipReqTerm(item.getShipReqTerm())
                .remark(item.getRemark())
                .godCds(goods)
                .build();
    }

    private static RegisterFasstoDeliveryGoodsCommand mapGoods(RegisterFasstoDeliveryGoodsRequest item) {
        return RegisterFasstoDeliveryGoodsCommand.of(
                item.getCstGodCd(),
                item.getDistTermDt(),
                item.getOrdQty()
        );
    }

    private static RegisterFasstoDeliveryGoodsCommand mapIcsGoods(RegisterFasstoDeliveryIcsGoodsRequest item) {
        return RegisterFasstoDeliveryGoodsCommand.of(
                item.getCstGodCd(),
                null,
                item.getOrdQty()
        );
    }
}
