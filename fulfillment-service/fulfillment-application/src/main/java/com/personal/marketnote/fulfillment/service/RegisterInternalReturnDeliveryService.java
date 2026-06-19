package com.personal.marketnote.fulfillment.service;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;
import com.personal.marketnote.fulfillment.port.in.command.RegisterInternalReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.RegisterInternalReturnDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.RegisterInternalReturnDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.out.shipping.FindShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.shipping.UpdateShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.vendor.DisconnectFulfillmentAuthPort;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentCustomerCodePort;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentReturnDeliveryPort;
import com.personal.marketnote.fulfillment.port.out.vendor.RequestFulfillmentAuthPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class RegisterInternalReturnDeliveryService implements RegisterInternalReturnDeliveryUseCase {
    private static final String FASSTO_RETURN_TYPE_NORMAL = "01";

    private final GetFulfillmentCustomerCodePort getFulfillmentCustomerCodePort;
    private final RequestFulfillmentAuthPort requestFulfillmentAuthPort;
    private final DisconnectFulfillmentAuthPort disconnectFulfillmentAuthPort;
    private final RegisterFulfillmentReturnDeliveryPort registerFulfillmentReturnDeliveryPort;
    private final FindShippingTrackerPort findShippingTrackerPort;
    private final UpdateShippingTrackerPort updateShippingTrackerPort;

    @Override
    public RegisterInternalReturnDeliveryResult registerReturnDelivery(RegisterInternalReturnDeliveryCommand command) {
        FulfillmentAccessToken accessToken = requestFulfillmentAuthPort.requestAccessToken();
        try {
            String customerCode = getFulfillmentCustomerCodePort.getCustomerCode();
            RegisterFulfillmentReturnDeliveryCommand fasstoCommand = buildFasstoCommand(
                    customerCode, accessToken.getValue(), command
            );
            RegisterFulfillmentDeliveryResult fasstoResult = registerFulfillmentReturnDeliveryPort.registerReturnDelivery(fasstoCommand);

            RegisterInternalReturnDeliveryResult result = mapToResult(command.orderId(), fasstoResult);
            if (result.registered()) {
                startReturnShippingIfDelivered(command.orderId());
            }
            return result;
        } finally {
            disconnectFulfillmentAuthPort.disconnectAccessToken(accessToken.getValue());
        }
    }

    private void startReturnShippingIfDelivered(Long orderId) {
        findShippingTrackerPort.findByOrderId(orderId).ifPresent(tracker -> {
            if (!tracker.isDelivered()) {
                return;
            }
            tracker.startReturnShipping();
            updateShippingTrackerPort.update(tracker);
        });
    }

    private RegisterFulfillmentReturnDeliveryCommand buildFasstoCommand(
            String customerCode,
            String accessToken,
            RegisterInternalReturnDeliveryCommand command
    ) {
        String orderNumber = String.valueOf(command.orderId());

        List<RegisterFulfillmentDeliveryGoodsCommand> goods = command.products().stream()
                .map(product -> RegisterFulfillmentDeliveryGoodsCommand.of(
                        product.productCode(), product.quantity()
                ))
                .toList();

        RegisterFulfillmentReturnDeliveryItemCommand itemCommand = RegisterFulfillmentReturnDeliveryItemCommand.builder()
                .orderDate(command.orderDate())
                .orderNumber(orderNumber)
                .courierCode("")
                .invoiceNumber("")
                .recipientName(command.recipientName())
                .recipientPhoneNumber(command.recipientPhoneNumber())
                .recipientAddress(command.recipientAddress())
                .returnReceiverName(command.pickupRecipientName())
                .returnReceiverPhoneNumber(command.pickupRecipientPhoneNumber())
                .returnZipCode(command.pickupZipCode())
                .returnAddress1(command.pickupAddress())
                .returnAddress2(command.pickupAddressDetail())
                .returnType(FASSTO_RETURN_TYPE_NORMAL)
                .returnReason(command.returnReason())
                .returnDetailReason(command.returnDetailReason())
                .returnShippingRequest(command.returnShippingRequest())
                .products(goods)
                .build();

        return RegisterFulfillmentReturnDeliveryCommand.of(customerCode, accessToken, List.of(itemCommand));
    }

    private RegisterInternalReturnDeliveryResult mapToResult(Long orderId, RegisterFulfillmentDeliveryResult fasstoResult) {
        if (FormatValidator.hasNoValue(fasstoResult.deliveries()) || fasstoResult.deliveries().isEmpty()) {
            return RegisterInternalReturnDeliveryResult.of(orderId, null, false, "반품 등록 응답 없음");
        }

        RegisterFulfillmentDeliveryItemResult firstDelivery = fasstoResult.deliveries().get(0);
        return RegisterInternalReturnDeliveryResult.of(
                orderId,
                firstDelivery.fulfillmentSlipNumber(),
                true,
                firstDelivery.message()
        );
    }
}
