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
public class FasstoDeliveryCancelMapper {
    private String customerCode;
    private String accessToken;
    private List<FasstoDeliveryCancelItemMapper> cancelRequests;

    public static FasstoDeliveryCancelMapper of(
            String customerCode,
            String accessToken,
            List<FasstoDeliveryCancelItemMapper> cancelRequests
    ) {
        FasstoDeliveryCancelMapper mapper = FasstoDeliveryCancelMapper.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .cancelRequests(cancelRequests)
                .build();
        mapper.validate();
        return mapper;
    }

    public List<Map<String, Object>> toPayload() {
        return cancelRequests.stream()
                .map(FasstoDeliveryCancelItemMapper::toPayload)
                .toList();
    }

    private void validate() {
        if (FormatValidator.hasNoValue(customerCode)) {
            throw new FasstoQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FasstoQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(cancelRequests)) {
            throw new FasstoQueryParameterNoValueException("cancelRequests");
        }
    }
}
