package com.personal.marketnote.fulfillment.domain.vendor.fassto.returndelivery;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoReturnDeliveryMapper {
    private String customerCode;
    private String accessToken;
    private List<FasstoReturnDeliveryItemMapper> returnDeliveryRequests;

    public static FasstoReturnDeliveryMapper register(
            String customerCode,
            String accessToken,
            List<FasstoReturnDeliveryItemMapper> returnDeliveryRequests
    ) {
        FasstoReturnDeliveryMapper mapper = FasstoReturnDeliveryMapper.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .returnDeliveryRequests(returnDeliveryRequests)
                .build();
        mapper.validate();
        return mapper;
    }

    public List<Map<String, Object>> toPayload() {
        return returnDeliveryRequests.stream()
                .map(FasstoReturnDeliveryItemMapper::toPayload)
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
            throw new IllegalArgumentException("customerCode is required.");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new IllegalArgumentException("accessToken is required.");
        }
        if (FormatValidator.hasNoValue(returnDeliveryRequests)) {
            throw new IllegalArgumentException("returnDeliveryRequests is required.");
        }
    }
}
