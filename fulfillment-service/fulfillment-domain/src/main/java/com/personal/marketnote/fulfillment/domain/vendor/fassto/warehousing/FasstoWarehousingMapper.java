package com.personal.marketnote.fulfillment.domain.vendor.fassto.warehousing;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoWarehousingMapper {
    private String customerCode;
    private String accessToken;
    private List<FasstoWarehousingItemMapper> warehousingRequests;

    public static FasstoWarehousingMapper register(
            String customerCode,
            String accessToken,
            List<FasstoWarehousingItemMapper> warehousingRequests
    ) {
        return create(customerCode, accessToken, warehousingRequests);
    }

    public static FasstoWarehousingMapper update(
            String customerCode,
            String accessToken,
            List<FasstoWarehousingItemMapper> warehousingRequests
    ) {
        return create(customerCode, accessToken, warehousingRequests);
    }

    public List<Map<String, Object>> toPayload() {
        return warehousingRequests.stream()
                .map(FasstoWarehousingItemMapper::toPayload)
                .toList();
    }

    private void validate() {
        if (FormatValidator.hasNoValue(customerCode)) {
            throw new FasstoQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FasstoQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(warehousingRequests)) {
            throw new FasstoQueryParameterNoValueException("warehousingRequests");
        }
    }

    private static FasstoWarehousingMapper create(
            String customerCode,
            String accessToken,
            List<FasstoWarehousingItemMapper> warehousingRequests
    ) {
        FasstoWarehousingMapper mapper = FasstoWarehousingMapper.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .warehousingRequests(warehousingRequests)
                .build();
        mapper.validate();
        return mapper;
    }
}
