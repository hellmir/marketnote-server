package com.personal.marketnote.fulfillment.port.in.command.vendor;

import com.personal.marketnote.fulfillment.port.in.command.RegisterInternalReturnDeliveryCommand;
import lombok.Builder;

import java.util.List;

@Builder
public record RegisterFulfillmentReturnDeliveryItemCommand(
        String orderDate,
        String orderNumber,
        String courierCode,
        String invoiceNumber,
        String recipientName,
        String recipientPhoneNumber,
        String recipientAddress,
        String returnReceiverName,
        String returnReceiverPhoneNumber,
        String returnZipCode,
        String returnAddress1,
        String returnAddress2,
        String returnType,
        String returnReason,
        String returnDetailReason,
        String returnShippingRequest,
        List<RegisterFulfillmentDeliveryGoodsCommand> products
) {
    /**
     * Fassto 벤더 스펙 상수.
     * <p>
     * 본 Command는 {@code port.in.command.vendor} 패키지 하위로 벤더 영역에 한정되며,
     * 현재 단일 벤더(Fassto) 전용이다. 멀티 벤더 확장 시 상수를 어댑터 레이어
     * ({@code adapter.out.vendor.fassto}) 로 이동하고 Command/매퍼에서 주입받는 형태로 재정비한다.
     */
    private static final String RETURN_TYPE_NORMAL = "01";
    private static final String COURIER_CODE_EMPTY = "";
    private static final String INVOICE_NUMBER_EMPTY = "";

    public static RegisterFulfillmentReturnDeliveryItemCommand from(
            RegisterInternalReturnDeliveryCommand command,
            List<RegisterFulfillmentDeliveryGoodsCommand> goods
    ) {
        return RegisterFulfillmentReturnDeliveryItemCommand.builder()
                .orderDate(command.orderDate())
                .orderNumber(String.valueOf(command.orderId()))
                .courierCode(COURIER_CODE_EMPTY)
                .invoiceNumber(INVOICE_NUMBER_EMPTY)
                .recipientName(command.recipientName())
                .recipientPhoneNumber(command.recipientPhoneNumber())
                .recipientAddress(command.recipientAddress())
                .returnReceiverName(command.pickupRecipientName())
                .returnReceiverPhoneNumber(command.pickupRecipientPhoneNumber())
                .returnZipCode(command.pickupZipCode())
                .returnAddress1(command.pickupAddress())
                .returnAddress2(command.pickupAddressDetail())
                .returnType(RETURN_TYPE_NORMAL)
                .returnReason(command.returnReason())
                .returnDetailReason(command.returnDetailReason())
                .returnShippingRequest(command.returnShippingRequest())
                .products(goods)
                .build();
    }
}
