package com.personal.marketnote.fulfillment.domain.vendor.fassto.returndelivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoDirectReturnDeliveryMapper {
    private String customerCode;
    private String accessToken;
    private List<FasstoDirectReturnDeliveryItemMapper> directReturnDeliveryRequests;

    public static FasstoDirectReturnDeliveryMapper register(
            String customerCode,
            String accessToken,
            List<FasstoDirectReturnDeliveryItemMapper> directReturnDeliveryRequests
    ) {
        FasstoDirectReturnDeliveryMapper mapper = FasstoDirectReturnDeliveryMapper.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .directReturnDeliveryRequests(directReturnDeliveryRequests)
                .build();
        mapper.validate();
        return mapper;
    }

    public List<Map<String, Object>> toPayload() {
        return directReturnDeliveryRequests.stream()
                .map(FasstoDirectReturnDeliveryItemMapper::toPayload)
                .toList();
    }

    private void validate() {
        if (FormatValidator.hasNoValue(customerCode)) {
            throw new FasstoQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FasstoQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(directReturnDeliveryRequests)) {
            throw new FasstoQueryParameterNoValueException("directReturnDeliveryRequests");
        }
    }
}
