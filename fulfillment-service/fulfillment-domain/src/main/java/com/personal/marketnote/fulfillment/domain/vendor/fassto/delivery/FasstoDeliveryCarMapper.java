package com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoDeliveryCarMapper {
    private String customerCode;
    private String accessToken;
    private List<FasstoDeliveryCarItemMapper> deliveryRequests;

    public static FasstoDeliveryCarMapper register(
            String customerCode,
            String accessToken,
            List<FasstoDeliveryCarItemMapper> deliveryRequests
    ) {
        FasstoDeliveryCarMapper mapper = FasstoDeliveryCarMapper.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .deliveryRequests(deliveryRequests)
                .build();
        mapper.validate();
        return mapper;
    }

    public List<Map<String, Object>> toPayload() {
        return deliveryRequests.stream()
                .map(FasstoDeliveryCarItemMapper::toPayload)
                .toList();
    }

    public String getOrdNo() {
        if (FormatValidator.hasNoValue(deliveryRequests)) {
            return null;
        }
        return deliveryRequests.getFirst().getOrdNo();
    }

    private void validate() {
        if (FormatValidator.hasNoValue(customerCode)) {
            throw new IllegalArgumentException("customerCode is required.");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new IllegalArgumentException("accessToken is required.");
        }
        if (FormatValidator.hasNoValue(deliveryRequests)) {
            throw new IllegalArgumentException("deliveryRequests is required.");
        }
    }
}
