package com.personal.marketnote.fulfillment.domain.vendor.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentDeliveryIcsCompletionItemMapper {
    private List<String> ordNoList;

    public static FulfillmentDeliveryIcsCompletionItemMapper of(List<String> ordNoList) {
        FulfillmentDeliveryIcsCompletionItemMapper mapper = FulfillmentDeliveryIcsCompletionItemMapper.builder()
                .ordNoList(ordNoList)
                .build();
        mapper.validate();
        return mapper;
    }

    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ordNoList", ordNoList);
        return payload;
    }

    private void validate() {
        if (FormatValidator.hasNoValue(ordNoList)) {
            throw new FulfillmentQueryParameterNoValueException("ordNoList", "delivery ics completion request");
        }
    }
}
