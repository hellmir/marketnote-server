package com.personal.marketnote.fulfillment.domain.vendor.returndelivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentReturnDeliveryMapper {
    private String customerCode;
    private String accessToken;
    private List<FulfillmentReturnDeliveryItemMapper> returnDeliveryRequests;

    public static FulfillmentReturnDeliveryMapper register(
            String customerCode,
            String accessToken,
            List<FulfillmentReturnDeliveryItemMapper> returnDeliveryRequests
    ) {
        FulfillmentReturnDeliveryMapper mapper = FulfillmentReturnDeliveryMapper.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .returnDeliveryRequests(returnDeliveryRequests)
                .build();
        mapper.validate();
        return mapper;
    }

    public List<Map<String, Object>> toPayload() {
        return returnDeliveryRequests.stream()
                .map(FulfillmentReturnDeliveryItemMapper::toPayload)
                .toList();
    }

    public String getOrdNo() {
        if (FormatValidator.hasNoValue(returnDeliveryRequests)) {
            return null;
        }
        return returnDeliveryRequests.getFirst().getOrdNo();
    }

    private void validate() {
        if (FormatValidator.hasNoValue(customerCode)) {
            throw new FulfillmentQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FulfillmentQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(returnDeliveryRequests)) {
            throw new FulfillmentQueryParameterNoValueException("returnDeliveryRequests");
        }
    }
}
