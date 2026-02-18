package com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoDeliveryIcsCompletionItemMapper {
    private List<String> ordNoList;

    public static FasstoDeliveryIcsCompletionItemMapper of(List<String> ordNoList) {
        FasstoDeliveryIcsCompletionItemMapper mapper = FasstoDeliveryIcsCompletionItemMapper.builder()
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
            throw new IllegalArgumentException("ordNoList is required for delivery ics completion request.");
        }
    }
}
