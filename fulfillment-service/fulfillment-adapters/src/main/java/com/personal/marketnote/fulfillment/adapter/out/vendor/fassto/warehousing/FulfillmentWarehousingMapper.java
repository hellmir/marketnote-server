package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.warehousing;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentWarehousingMapper {
    private String customerCode;
    private String accessToken;
    private List<FulfillmentWarehousingItemMapper> warehousingRequests;

    public static FulfillmentWarehousingMapper register(
            String customerCode,
            String accessToken,
            List<FulfillmentWarehousingItemMapper> warehousingRequests
    ) {
        return create(customerCode, accessToken, warehousingRequests);
    }

    public static FulfillmentWarehousingMapper update(
            String customerCode,
            String accessToken,
            List<FulfillmentWarehousingItemMapper> warehousingRequests
    ) {
        return create(customerCode, accessToken, warehousingRequests);
    }

    public List<Map<String, Object>> toPayload() {
        return warehousingRequests.stream()
                .map(FulfillmentWarehousingItemMapper::toPayload)
                .toList();
    }

    private void validate() {
        if (FormatValidator.hasNoValue(customerCode)) {
            throw new FulfillmentQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FulfillmentQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(warehousingRequests)) {
            throw new FulfillmentQueryParameterNoValueException("warehousingRequests");
        }
    }

    private static FulfillmentWarehousingMapper create(
            String customerCode,
            String accessToken,
            List<FulfillmentWarehousingItemMapper> warehousingRequests
    ) {
        FulfillmentWarehousingMapper mapper = FulfillmentWarehousingMapper.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .warehousingRequests(warehousingRequests)
                .build();
        mapper.validate();
        return mapper;
    }
}
