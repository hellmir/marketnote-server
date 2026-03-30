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
public class FulfillmentDeliveryCancelMapper {
    private String customerCode;
    private String accessToken;
    private List<FulfillmentDeliveryCancelItemMapper> cancelRequests;

    public static FulfillmentDeliveryCancelMapper of(
            String customerCode,
            String accessToken,
            List<FulfillmentDeliveryCancelItemMapper> cancelRequests
    ) {
        FulfillmentDeliveryCancelMapper mapper = FulfillmentDeliveryCancelMapper.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .cancelRequests(cancelRequests)
                .build();
        mapper.validate();
        return mapper;
    }

    public List<Map<String, Object>> toPayload() {
        return cancelRequests.stream()
                .map(FulfillmentDeliveryCancelItemMapper::toPayload)
                .toList();
    }

    private void validate() {
        if (FormatValidator.hasNoValue(customerCode)) {
            throw new FulfillmentQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FulfillmentQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(cancelRequests)) {
            throw new FulfillmentQueryParameterNoValueException("cancelRequests");
        }
    }
}
