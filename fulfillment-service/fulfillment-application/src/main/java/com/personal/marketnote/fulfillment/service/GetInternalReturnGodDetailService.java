package com.personal.marketnote.fulfillment.service;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.port.in.command.GetInternalReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.GetInternalReturnGodDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.InternalReturnGodDetailGoodsResult;
import com.personal.marketnote.fulfillment.port.in.result.InternalReturnGodDetailInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentReturnGodDetailGoodsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentReturnGodDetailInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentReturnGodDetailResult;
import com.personal.marketnote.fulfillment.port.in.usecase.GetInternalReturnGodDetailUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.DisconnectFulfillmentAuthPort;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentCustomerCodePort;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentReturnGodDetailPort;
import com.personal.marketnote.fulfillment.port.out.vendor.RequestFulfillmentAuthPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class GetInternalReturnGodDetailService implements GetInternalReturnGodDetailUseCase {
    private final GetFulfillmentCustomerCodePort getFulfillmentCustomerCodePort;
    private final RequestFulfillmentAuthPort requestFulfillmentAuthPort;
    private final DisconnectFulfillmentAuthPort disconnectFulfillmentAuthPort;
    private final GetFulfillmentReturnGodDetailPort getFulfillmentReturnGodDetailPort;

    @Override
    public GetInternalReturnGodDetailResult getReturnGodDetail(GetInternalReturnGodDetailCommand command) {
        FulfillmentAccessToken accessToken = requestFulfillmentAuthPort.requestAccessToken();
        try {
            String customerCode = getFulfillmentCustomerCodePort.getCustomerCode();
            GetFulfillmentReturnGodDetailCommand fasstoCommand = GetFulfillmentReturnGodDetailCommand.of(
                    customerCode,
                    accessToken.getValue(),
                    null,
                    null,
                    command.returnSlipNumbers(),
                    null
            );
            GetFulfillmentReturnGodDetailResult fasstoResult = getFulfillmentReturnGodDetailPort.getReturnGodDetail(fasstoCommand);

            return mapToResult(fasstoResult);
        } finally {
            disconnectFulfillmentAuthPort.disconnectAccessToken(accessToken.getValue());
        }
    }

    private GetInternalReturnGodDetailResult mapToResult(GetFulfillmentReturnGodDetailResult fasstoResult) {
        if (FormatValidator.hasNoValue(fasstoResult) || FormatValidator.hasNoValue(fasstoResult.returnGodInfos())) {
            return GetInternalReturnGodDetailResult.of(0, List.of());
        }

        List<InternalReturnGodDetailInfoResult> infos = fasstoResult.returnGodInfos().stream()
                .map(this::mapToInfoResult)
                .toList();

        return GetInternalReturnGodDetailResult.of(fasstoResult.dataCount(), infos);
    }

    private InternalReturnGodDetailInfoResult mapToInfoResult(FulfillmentReturnGodDetailInfoResult info) {
        if (FormatValidator.hasNoValue(info.products())) {
            return InternalReturnGodDetailInfoResult.of(info.orderNumber(), info.inboundOrderSlipNumber(), List.of());
        }

        List<InternalReturnGodDetailGoodsResult> goods = info.products().stream()
                .map(this::mapToGoodsResult)
                .toList();

        return InternalReturnGodDetailInfoResult.of(
                info.orderNumber(),
                info.inboundOrderSlipNumber(),
                goods
        );
    }

    private InternalReturnGodDetailGoodsResult mapToGoodsResult(FulfillmentReturnGodDetailGoodsResult goods) {
        return InternalReturnGodDetailGoodsResult.of(
                goods.customerProductCode(),
                goods.productName(),
                goods.returnProductCheckStatus(),
                goods.returnProductCheckStatusName()
        );
    }
}
