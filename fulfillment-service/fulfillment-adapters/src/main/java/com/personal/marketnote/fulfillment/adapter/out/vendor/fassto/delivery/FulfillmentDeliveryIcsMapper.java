package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentDeliveryIcsMapper {
    private String customerCode;
    private String accessToken;
    private List<FulfillmentDeliveryIcsItemMapper> deliveryRequests;

    public static FulfillmentDeliveryIcsMapper register(
            String customerCode,
            String accessToken,
            List<FulfillmentDeliveryIcsItemMapper> deliveryRequests
    ) {
        FulfillmentDeliveryIcsMapper mapper = FulfillmentDeliveryIcsMapper.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .deliveryRequests(deliveryRequests)
                .build();
        mapper.validate();
        return mapper;
    }

    public List<Map<String, Object>> toPayload() {
        return deliveryRequests.stream()
                .map(FulfillmentDeliveryIcsItemMapper::toPayload)
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
            throw new FulfillmentQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FulfillmentQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(deliveryRequests)) {
            throw new FulfillmentQueryParameterNoValueException("deliveryRequests");
        }
    }
}
