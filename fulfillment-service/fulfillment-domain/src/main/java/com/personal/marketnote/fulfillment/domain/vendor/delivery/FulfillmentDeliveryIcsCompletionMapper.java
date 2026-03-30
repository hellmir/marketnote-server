package com.personal.marketnote.fulfillment.domain.vendor.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentDeliveryIcsCompletionMapper {
    private String customerCode;
    private String accessToken;
    private List<FulfillmentDeliveryIcsCompletionItemMapper> completionRequests;

    public static FulfillmentDeliveryIcsCompletionMapper of(
            String customerCode,
            String accessToken,
            List<FulfillmentDeliveryIcsCompletionItemMapper> completionRequests
    ) {
        FulfillmentDeliveryIcsCompletionMapper mapper = FulfillmentDeliveryIcsCompletionMapper.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .completionRequests(completionRequests)
                .build();
        mapper.validate();
        return mapper;
    }

    public List<Map<String, Object>> toPayload() {
        return completionRequests.stream()
                .map(FulfillmentDeliveryIcsCompletionItemMapper::toPayload)
                .toList();
    }

    private void validate() {
        if (FormatValidator.hasNoValue(customerCode)) {
            throw new FulfillmentQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FulfillmentQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(completionRequests)) {
            throw new FulfillmentQueryParameterNoValueException("completionRequests");
        }
    }
}
