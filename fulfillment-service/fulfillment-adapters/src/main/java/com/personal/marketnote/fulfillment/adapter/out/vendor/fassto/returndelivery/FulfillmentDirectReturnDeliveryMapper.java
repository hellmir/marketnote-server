package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.returndelivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentDirectReturnDeliveryMapper {
    private String customerCode;
    private String accessToken;
    private List<FulfillmentDirectReturnDeliveryItemMapper> directReturnDeliveryRequests;

    public static FulfillmentDirectReturnDeliveryMapper register(
            String customerCode,
            String accessToken,
            List<FulfillmentDirectReturnDeliveryItemMapper> directReturnDeliveryRequests
    ) {
        FulfillmentDirectReturnDeliveryMapper mapper = FulfillmentDirectReturnDeliveryMapper.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .directReturnDeliveryRequests(directReturnDeliveryRequests)
                .build();
        mapper.validate();
        return mapper;
    }

    public List<Map<String, Object>> toPayload() {
        return directReturnDeliveryRequests.stream()
                .map(FulfillmentDirectReturnDeliveryItemMapper::toPayload)
                .toList();
    }

    private void validate() {
        if (FormatValidator.hasNoValue(customerCode)) {
            throw new FulfillmentQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FulfillmentQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(directReturnDeliveryRequests)) {
            throw new FulfillmentQueryParameterNoValueException("directReturnDeliveryRequests");
        }
    }
}
