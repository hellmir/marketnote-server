package com.personal.marketnote.commerce.domain.order;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ShippingAddress {
    private String recipientName;
    private String recipientPhoneNumber;
    private String zipCode;
    private String address;
    private String addressDetail;
    private String requestMessage;

    public static ShippingAddress of(
            String recipientName,
            String recipientPhoneNumber,
            String zipCode,
            String address,
            String addressDetail,
            String requestMessage
    ) {
        return ShippingAddress.builder()
                .recipientName(recipientName)
                .recipientPhoneNumber(recipientPhoneNumber)
                .zipCode(zipCode)
                .address(address)
                .addressDetail(addressDetail)
                .requestMessage(requestMessage)
                .build();
    }

    public boolean hasRecipientName() {
        return FormatValidator.hasValue(recipientName);
    }

    public ShippingAddress withoutRequestMessage() {
        return ShippingAddress.builder()
                .recipientName(recipientName)
                .recipientPhoneNumber(recipientPhoneNumber)
                .zipCode(zipCode)
                .address(address)
                .addressDetail(addressDetail)
                .build();
    }
}
