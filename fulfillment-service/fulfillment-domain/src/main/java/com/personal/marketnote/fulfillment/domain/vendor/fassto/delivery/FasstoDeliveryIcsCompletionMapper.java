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
public class FasstoDeliveryIcsCompletionMapper {
    private String customerCode;
    private String accessToken;
    private List<FasstoDeliveryIcsCompletionItemMapper> completionRequests;

    public static FasstoDeliveryIcsCompletionMapper of(
            String customerCode,
            String accessToken,
            List<FasstoDeliveryIcsCompletionItemMapper> completionRequests
    ) {
        FasstoDeliveryIcsCompletionMapper mapper = FasstoDeliveryIcsCompletionMapper.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .completionRequests(completionRequests)
                .build();
        mapper.validate();
        return mapper;
    }

    public List<Map<String, Object>> toPayload() {
        return completionRequests.stream()
                .map(FasstoDeliveryIcsCompletionItemMapper::toPayload)
                .toList();
    }

    private void validate() {
        if (FormatValidator.hasNoValue(customerCode)) {
            throw new FasstoQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FasstoQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(completionRequests)) {
            throw new FasstoQueryParameterNoValueException("completionRequests");
        }
    }
}
