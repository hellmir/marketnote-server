package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.port.out.aligo.GetAligoMessageDetailExternalPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AligoMessageDetailExternalAdapter implements GetAligoMessageDetailExternalPort {

    private static final String HISTORY_DETAIL_PATH = "/akv10/history/detail/";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AligoHttpClient aligoHttpClient;

    @Override
    public List<AligoRecipientDetailResult> getMessageDetail(String mid) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("mid", mid);
        params.add("page", "1");
        params.add("limit", "500");

        AligoMessageDetailApiResponse response = aligoHttpClient.post(
                HISTORY_DETAIL_PATH, params, AligoMessageDetailApiResponse.class
        );

        if (FormatValidator.hasNoValue(response.list())) {
            return List.of();
        }

        return response.list().stream()
                .map(this::toResult)
                .toList();
    }

    private AligoRecipientDetailResult toResult(AligoMessageDetailApiResponse.DetailInfo info) {
        return new AligoRecipientDetailResult(
                info.msgId(),
                info.receiver(),
                info.rslt(),
                info.rsltMessage(),
                parseDateTime(info.smsDate()),
                parseDateTime(info.rsltDate()),
                parseDateTime(info.reportDate())
        );
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (FormatValidator.hasNoValue(dateTimeStr)) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
    }
}
