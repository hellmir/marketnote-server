package com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoDeliveryMapper {
    private String customerCode;
    private String accessToken;
    private List<FasstoDeliveryItemMapper> deliveryRequests;

    public static FasstoDeliveryMapper register(
            String customerCode,
            String accessToken,
            List<FasstoDeliveryItemMapper> deliveryRequests
    ) {
        FasstoDeliveryMapper mapper = FasstoDeliveryMapper.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .deliveryRequests(deliveryRequests)
                .build();
        mapper.validate();
        return mapper;
    }

    public static FasstoDeliveryMapper update(
            String customerCode,
            String accessToken,
            List<FasstoDeliveryItemMapper> deliveryRequests
    ) {
        FasstoDeliveryMapper mapper = FasstoDeliveryMapper.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .deliveryRequests(deliveryRequests)
                .build();
        mapper.validate();
        return mapper;
    }

    public List<Map<String, Object>> toPayload() {
        return deliveryRequests.stream()
                .map(FasstoDeliveryItemMapper::toPayload)
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
            throw new FasstoQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FasstoQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(deliveryRequests)) {
            throw new FasstoQueryParameterNoValueException("deliveryRequests");
        }
    }
}
