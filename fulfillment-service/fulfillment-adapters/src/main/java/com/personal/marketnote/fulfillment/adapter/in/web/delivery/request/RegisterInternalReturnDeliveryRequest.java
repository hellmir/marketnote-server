package com.personal.marketnote.fulfillment.adapter.in.web.delivery.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RegisterInternalReturnDeliveryRequest(
        @NotNull(message = "주문 ID는 필수입니다")
        Long orderId,
        @NotBlank(message = "주문일은 필수입니다")
        String orderDate,
        @NotBlank(message = "수령인명은 필수입니다")
        String recipientName,
        @NotBlank(message = "수령인 전화번호는 필수입니다")
        String recipientPhoneNumber,
        String recipientAddress,
        @NotBlank(message = "회수지 수령인명은 필수입니다")
        String pickupRecipientName,
        @NotBlank(message = "회수지 전화번호는 필수입니다")
        String pickupRecipientPhoneNumber,
        @NotBlank(message = "회수지 우편번호는 필수입니다")
        String pickupZipCode,
        @NotBlank(message = "회수지 주소는 필수입니다")
        String pickupAddress,
        String pickupAddressDetail,
        String returnReason,
        String returnDetailReason,
        String returnShippingRequest,
        @NotEmpty(message = "반품 상품 목록은 필수입니다")
        @Valid
        List<ProductItem> products
) {
    public record ProductItem(
            @NotBlank(message = "상품 코드는 필수입니다")
            String productCode,
            @NotNull(message = "수량은 필수입니다")
            @Min(value = 1, message = "수량은 1 이상이어야 합니다")
            Integer quantity
    ) {
    }
}
