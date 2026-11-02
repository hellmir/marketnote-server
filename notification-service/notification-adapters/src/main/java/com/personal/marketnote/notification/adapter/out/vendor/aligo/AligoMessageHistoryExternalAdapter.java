package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.port.out.aligo.GetAligoMessageHistoryExternalPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AligoMessageHistoryExternalAdapter implements GetAligoMessageHistoryExternalPort {

    private static final String HISTORY_LIST_PATH = "/akv10/history/list/";

    private final AligoHttpClient aligoHttpClient;

    @Override
    public AligoMessageHistoryResult getMessageHistory(int page, int limit, String startDate, String endDate) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("page", String.valueOf(page));
        params.add("limit", String.valueOf(limit));
        if (FormatValidator.hasValue(startDate)) {
            params.add("startdate", startDate);
        }
        if (FormatValidator.hasValue(endDate)) {
            params.add("enddate", endDate);
        }

        AligoMessageHistoryApiResponse response = aligoHttpClient.post(
                HISTORY_LIST_PATH, params, AligoMessageHistoryApiResponse.class
        );

        if (FormatValidator.hasNoValue(response.list())) {
            return new AligoMessageHistoryResult(response.totalCount(), List.of());
        }

        List<AligoMessageHistorySummary> summaries = response.list().stream()
                .map(info -> new AligoMessageHistorySummary(
                        info.mid(), info.type(), info.sender(), info.content(),
                        info.scnt(), info.fcnt(), info.regDate()
                ))
                .toList();

        return new AligoMessageHistoryResult(response.totalCount(), summaries);
    }
}
